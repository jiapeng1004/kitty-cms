import 'package:kitty_news/core/network/http_rpc_client.dart';
import 'package:kitty_news/core/network/rpc_client.dart';
import 'package:kitty_news/core/network/rpc_config.dart';
import 'package:kitty_news/core/network/rpc_paths.dart';
import 'package:kitty_news/features/feed/data/services/demo_news_rpc_handlers.dart';

/// 组装 RPC 调用函数：Demo 或 HTTP。
RpcCall createKittyRpcClient(RpcAppConfig config) {
  if (config.useDemoBackend) {
    return _demoCall;
  }

  final client = HttpRpcClient(base: config.httpBase);
  return client.call;
}

final Map<String, RpcHandler> _demoHandlers = <String, RpcHandler>{
  RpcPaths.newsFeedList: DemoNewsRpcHandlers.newsFeedList,
  RpcPaths.newsArticleGet: DemoNewsRpcHandlers.newsArticleGet,
  RpcPaths.newsHotList: DemoNewsRpcHandlers.newsHotList,
};

Future<JsonMap> _demoCall(
  String apiPath, [
  JsonMap body = const <String, dynamic>{},
]) async {
  final handler = _demoHandlers[apiPath];
  if (handler == null) {
    throw Exception('未注册的 RPC API: $apiPath');
  }
  return handler(body);
}
