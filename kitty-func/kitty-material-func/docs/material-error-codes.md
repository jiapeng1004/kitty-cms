## Material 错误码字典（阶段1）

Material 当前复用 `kitty-common` 通用错误码体系，接口错误统一由 `CommonErrorResult` 返回。

## 通用错误

- `10001` `operate.failed`：通用失败
- `10012` `param.error`：参数错误（含 scopeType 非法、私有目录越权访问）

## 返回体约定

- `state`: 错误码
- `message`: 国际化后的错误描述（`messageSource` 解析）

## 后续扩展

- 下一阶段新增 material 专属错误码段（建议 `12xxx`）
- 与转码、编目、存储异常细分对齐
