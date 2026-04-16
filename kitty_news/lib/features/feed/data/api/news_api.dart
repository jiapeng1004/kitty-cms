import 'package:kitty_news/core/network/rpc_client.dart';
import 'package:kitty_news/core/network/rpc_paths.dart';
import 'package:kitty_news/features/feed/data/models/hot_topic.dart';
import 'package:kitty_news/features/feed/data/models/news_article.dart';
import 'package:kitty_news/features/feed/data/models/news_feed_page.dart';

Future<NewsFeedPage> fetchNewsFeed(
  RpcCall call, {
  required String channel,
  required int cursor,
  int size = 10,
}) async {
  final response = await call(
    RpcPaths.newsFeedList,
    <String, dynamic>{
      'channel': channel,
      'cursor': cursor,
      'size': size,
    },
  );
  return NewsFeedPage.fromJson(response);
}

Future<NewsArticle> fetchNewsArticleById(RpcCall call, String id) async {
  final response = await call(
    RpcPaths.newsArticleGet,
    <String, dynamic>{'id': id},
  );
  return NewsArticle.fromJson(response);
}

Future<List<HotTopic>> fetchNewsHotTopics(RpcCall call) async {
  final response = await call(RpcPaths.newsHotList);
  return (response['items'] as List<dynamic>? ?? <dynamic>[])
      .whereType<Map<String, dynamic>>()
      .map(HotTopic.fromJson)
      .toList();
}
