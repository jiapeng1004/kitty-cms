class NewsArticle {
  const NewsArticle({
    required this.id,
    required this.title,
    required this.summary,
    required this.source,
    required this.publishTime,
    required this.commentCount,
    required this.tag,
    this.body,
  });

  final String id;
  final String title;
  final String summary;
  final String source;
  final String publishTime;
  final int commentCount;
  final String tag;

  /// 详情正文（列表可为空，详情接口返回）
  final String? body;

  factory NewsArticle.fromJson(Map<String, dynamic> json) {
    return NewsArticle(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      summary: json['summary'] as String? ?? '',
      source: json['source'] as String? ?? '',
      publishTime: json['publishTime'] as String? ?? '',
      commentCount: json['commentCount'] as int? ?? 0,
      tag: json['tag'] as String? ?? '',
      body: json['body'] as String?,
    );
  }
}
