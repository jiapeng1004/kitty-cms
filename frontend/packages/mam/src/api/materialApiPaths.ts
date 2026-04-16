import { MATERIAL_SERVICE_PATH } from './constants'

/**
 * 与 kitty-material-func Controller 路径对齐（联调清单，见 openspec 11.4）。
 * 请求时请在前面加 {@link MATERIAL_SERVICE_PATH}（默认 `/kitty-cms` 网关前缀）。
 */
const M = `${MATERIAL_SERVICE_PATH}/api/material`

export const MaterialApiPaths = {
  /** 与 MaterialCatalogController 一致：/api/catalog，非 /api/material/catalog */
  catalogTree: `${MATERIAL_SERVICE_PATH}/api/catalog/tree`,
  /** GET …/permission/tree/role/{roleId} */
  catalogTreeRolePermission: `${MATERIAL_SERVICE_PATH}/api/catalog/permission/tree/role`,
  resourceList: `${M}/resource/list`,
  resourcePreview: `${M}/resource/preview`,
  resourceKeyframe: `${M}/resource/keyframe`,
  resourceDetail: `${M}/resource/detail`,
  resource: `${M}/resource`,
  resourceFolder: `${M}/resource/folder`,
  resourcePathRebuild: `${M}/resource/path/rebuild`,
  resourceFolderPlanUpload: `${M}/resource/folder/plan-upload`,
  resourceFingerprint: `${M}/resource/fingerprint`,
  resourceFingerprintPrecheck: `${M}/resource/fingerprint/precheck`,
  resourceMetaFile: `${M}/resource/meta-file`,
  resourceMetaFileBind: `${M}/resource/meta-file/bind`,
  storageList: `${M}/storage/list`,
  storageRoutePreview: `${M}/storage/route/preview`,
  storageObjectKeyNormalize: `${M}/storage/object-key/normalize`,
  storageConnectivityCheck: `${M}/storage/connectivity/check`,
  uploadChunkSession: `${M}/upload/chunk/session`,
  metadataTemplate: `${M}/metadata/template`,
  metadataTemplateList: `${M}/metadata/template/list`,
  metadataTemplateFields: `${M}/metadata/template/fields`,
  metadataTemplateBindings: `${M}/metadata/template/bindings`,
  metadataField: `${M}/metadata/field`,
  metadataFieldList: `${M}/metadata/field/list`,
  metadataInstanceFormFields: `${M}/metadata/instance/form-fields`,
  metadataInstanceSave: `${M}/metadata/instance/save`,
  metadataInstanceLast: `${M}/metadata/instance/last`,
  metadataInstanceHistory: `${M}/metadata/instance/history`,
  taskList: `${M}/task/list`,
  taskTranscodeEnqueue: `${M}/task/transcode/enqueue`,
  taskTranscodeRetry: `${M}/task/transcode/retry`,
  transcodeStrategyList: `${M}/transcode/strategy/list`,
  transcodeStrategy: `${M}/transcode/strategy`,
  transcodeBindList: `${M}/transcode/bind/list`,
  transcodeBind: `${M}/transcode/bind`,
  messageSend: `${M}/message/send`,
  messageList: `${M}/message/list`,
  messageRead: `${M}/message/read`,
  messageSse: `${M}/message/sse`,
  reviewSubmit: `${M}/review/submit`,
  reviewApprove: `${M}/review/approve`,
  reviewReject: `${M}/review/reject`,
  reviewQuery: `${M}/review/query`
} as const
