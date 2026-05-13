/**
 * 与后端 servlet.context-path 一致（application.yml: context-path: /${spring.application.name}）
 * kitty-user 服务根路径，所有调用该服务的 API 需在此基础上拼接路径
 */
export const USER_SERVICE_PATH = ''
