/// 联调开关：后端就绪后改为 `false` 并配置 [httpBase]。
class RpcAppConfig {
  const RpcAppConfig({
    required this.useDemoBackend,
    this.httpBase = 'https://api.example.com/rpc',
  });

  /// `true` 使用内存 Demo；`false` 使用 [HttpRpcClient] 请求真实服务。
  final bool useDemoBackend;

  /// 完整 RPC 入口 URL（含路径），例如 `https://your-cms.example.com/rpc`。
  final String httpBase;

  /// 默认：Demo。发布联调前改为 `RpcAppConfig(useDemoBackend: false, httpBase: '...')`。
  static const RpcAppConfig instance = RpcAppConfig(useDemoBackend: true);
}
