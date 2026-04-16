# 分片上传会话：DB 与 Redis（StringRedisTemplate）的取舍

## 背景

多 Pod 部署下，大文件分片上传会对**同一会话**产生极高频的「登记分片」与「读会话」操作。若热路径完全依赖关系库：

- 逐片 `INSERT` / `SELECT` 易成为瓶颈；
- 跨 Pod 无共享内存，会话态必须外置，但应优先选**低延迟、高吞吐**的存储。

## 原则

1. **热路径（uploading）**：分片登记与会话快照以 **`StringRedisTemplate`（Redis）** 为主，避免高并发下对 DB 的逐片写入与反复查询。
2. **冷数据与审计**：`kt_chunk_upload_session` 仍写入 DB（创建时即落库，资源桩、指纹等与业务一致）；`kt_chunk_upload_part` 在**合并校验前 / 完成会话前**由 Redis **批量刷入**（幂等），满足合并逻辑与历史查询。
3. **完成 / 取消**：删除 Redis 中会话与分片 Hash，避免长期占用；DB 行随业务状态更新。

## 实现要点（代码）

- `ChunkUploadSessionHotCache`：`kitty:mam:chunk:session:{id}`（会话 JSON）、`kitty:mam:chunk:parts:{id}`（Hash：分片索引 → **字节数**，对象存储为 **`byteSize|partEtag`**），TTL 约 48h。
- `ChunkUploadSessionServiceImpl#registerPart`：优先读 Redis 会话快照；分片写 Redis（对象存储需带 UploadPart 返回的 ETag；旧数据可回源 DB 做幂等判断）。
- `flushUploadPartsFromCacheToDb`：合并前、完成前调用，将 Hash 刷入 `kt_chunk_upload_part`（含 `part_etag`）。
- `ChunkUploadMergeService#mergeVerifyAndComplete`：flush 后按存储类型分流——**磁盘**：校验分片与指纹，经 `StorageDriver#writeSequentialLocalPartFilesToObject` 顺序拼接本地 staging 分片；**对象存储**：分片已在 `writePart` 时 `UploadPart`，此处 `CompleteMultipartUpload`（无本地合并临时文件）。空对象走 `putEmptyObject`。

## 运维注意

- Redis 不可用会导致上传热路径失败，需保证 MAM 与 Redis 同环境可用性策略（与现有 `StringRedisTemplateCacheOperator` 一致）。
- TTL 到期未完成的上传，DB 中可能残留 `uploading` 会话，需依赖既有清理策略或后续任务。
