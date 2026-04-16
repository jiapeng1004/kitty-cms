import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:kitty_news/core/network/rpc_client.dart';

/// 通过 HTTP POST 调用统一 RPC 入口，请求体格式与后端约定一致：
/// `{ "method": "<RpcPaths 常量>", "params": { ... } }`
/// 成功响应支持 `{ "data": { ... } }` 或直接返回业务 JSON（与 Demo 返回结构一致）。
class HttpRpcClient {
  HttpRpcClient({
    required String base,
    http.Client? httpClient,
  })  : _endpoint = _normalizeEndpoint(base),
        _http = httpClient ?? http.Client();

  final Uri _endpoint;
  final http.Client _http;

  static Uri _normalizeEndpoint(String base) {
    var s = base.trim();
    if (s.isEmpty) {
      throw ArgumentError('base 不能为空');
    }
    if (!s.contains('://')) {
      s = 'https://$s';
    }
    final uri = Uri.parse(s);
    if (uri.host.isEmpty) {
      throw ArgumentError('无效的 base: $base');
    }
    if (uri.path.isEmpty) {
      return uri.replace(path: '/');
    }
    return uri;
  }

  Future<JsonMap> call(
    String apiPath, [
    JsonMap body = const <String, dynamic>{},
  ]) async {
    final payload = <String, dynamic>{
      'method': apiPath,
      'params': body,
    };

    final response = await _http.post(
      _endpoint,
      headers: const <String, String>{
        'Content-Type': 'application/json; charset=utf-8',
        'Accept': 'application/json',
      },
      body: jsonEncode(payload),
    );

    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw Exception('RPC HTTP ${response.statusCode}: ${response.body}');
    }

    final decoded = jsonDecode(utf8.decode(response.bodyBytes));
    if (decoded is! Map<String, dynamic>) {
      throw Exception('RPC 响应不是 JSON 对象');
    }

    if (decoded.containsKey('error')) {
      final err = decoded['error'];
      throw Exception('RPC error: $err');
    }

    final data = decoded['data'];
    if (data is Map<String, dynamic>) {
      return data;
    }

    return decoded;
  }
}
