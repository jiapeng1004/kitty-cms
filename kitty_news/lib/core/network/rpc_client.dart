typedef JsonMap = Map<String, dynamic>;
typedef RpcHandler = Future<JsonMap> Function(JsonMap body);
typedef RpcCall = Future<JsonMap> Function(
  String apiPath, [
  JsonMap body,
]);
