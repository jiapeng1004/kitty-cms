class HotTopic {
  const HotTopic({
    required this.rank,
    required this.title,
    required this.id,
  });

  final int rank;
  final String title;
  final String id;

  factory HotTopic.fromJson(Map<String, dynamic> json) {
    return HotTopic(
      rank: json['rank'] as int? ?? 0,
      title: json['title'] as String? ?? '',
      id: json['id'] as String? ?? '',
    );
  }
}
