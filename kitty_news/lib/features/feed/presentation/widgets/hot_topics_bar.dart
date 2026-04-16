import 'package:flutter/material.dart';
import 'package:kitty_news/features/feed/data/models/hot_topic.dart';

class HotTopicsBar extends StatelessWidget {
  const HotTopicsBar({
    super.key,
    required this.topics,
    required this.loading,
    required this.onTopicTap,
  });

  final List<HotTopic> topics;
  final bool loading;
  final ValueChanged<HotTopic> onTopicTap;

  @override
  Widget build(BuildContext context) {
    if (loading && topics.isEmpty) {
      return const SizedBox(
        height: 40,
        child: Align(
          alignment: Alignment.centerLeft,
          child: Padding(
            padding: EdgeInsets.only(left: 12),
            child: SizedBox(
              width: 18,
              height: 18,
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          ),
        ),
      );
    }

    if (topics.isEmpty) {
      return const SizedBox.shrink();
    }

    return SizedBox(
      height: 40,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        itemBuilder: (context, index) {
          if (index == 0) {
            return Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(4),
                    color: const Color(0xFFFFEAEA),
                  ),
                  child: const Text(
                    '热',
                    style: TextStyle(
                      color: Color(0xFFED4040),
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
              ],
            );
          }

          final topic = topics[index - 1];
          return GestureDetector(
            onTap: () => onTopicTap(topic),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  '${topic.rank}',
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: Color(0xFFED4040),
                  ),
                ),
                const SizedBox(width: 4),
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 160),
                  child: Text(
                    topic.title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 14,
                      color: Color(0xFF333333),
                    ),
                  ),
                ),
              ],
            ),
          );
        },
        separatorBuilder: (context, index) {
          if (index == 0) {
            return const SizedBox(width: 4);
          }
          return const Padding(
            padding: EdgeInsets.symmetric(horizontal: 6),
            child: Text(
              '|',
              style: TextStyle(color: Color(0xFFE0E0E0)),
            ),
          );
        },
        itemCount: topics.length + 1,
      ),
    );
  }
}
