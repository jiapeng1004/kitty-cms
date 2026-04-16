import 'package:kitty_news/features/feed/data/models/news_article.dart';

class NewsFeedPage {
  const NewsFeedPage({
    required this.items,
    required this.nextCursor,
    required this.hasMore,
  });

  final List<NewsArticle> items;
  final int nextCursor;
  final bool hasMore;

  factory NewsFeedPage.fromJson(Map<String, dynamic> json) {
    final list = (json['items'] as List<dynamic>? ?? <dynamic>[])
        .whereType<Map<String, dynamic>>()
        .map(NewsArticle.fromJson)
        .toList();

    return NewsFeedPage(
      items: list,
      nextCursor: json['nextCursor'] as int? ?? 0,
      hasMore: json['hasMore'] as bool? ?? false,
    );
  }
}
