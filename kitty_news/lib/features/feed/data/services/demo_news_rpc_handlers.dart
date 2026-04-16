import 'dart:math';

import 'package:kitty_news/core/network/rpc_client.dart';

class DemoNewsRpcHandlers {
  DemoNewsRpcHandlers._();

  static Future<JsonMap> newsFeedList(JsonMap body) async {
    await Future<void>.delayed(const Duration(milliseconds: 500));

    final channel = body['channel'] as String? ?? '推荐';
    final cursor = body['cursor'] as int? ?? 0;
    final size = body['size'] as int? ?? 10;

    const total = 50;
    final start = cursor * size;
    final end = min(start + size, total);
    final hasMore = end < total;

    final items = List<JsonMap>.generate(end - start, (index) {
      final number = start + index + 1;
      return <String, dynamic>{
        'id': '$channel-$number',
        'title': '$channel | 这是一条第 $number 条资讯标题（Demo 数据）',
        'summary': '围绕 $channel 的内容摘要：用于演示卡片布局、分页与 RPC 调用链路。',
        'source': _sourceByChannel(channel),
        'publishTime': '${(number % 12) + 1} 小时前',
        'commentCount': 50 + number * 3,
        'tag': channel,
      };
    });

    return <String, dynamic>{
      'items': items,
      'nextCursor': hasMore ? cursor + 1 : cursor,
      'hasMore': hasMore,
    };
  }

  static String _sourceByChannel(String channel) {
    switch (channel) {
      case '热点':
        return '热点速递';
      case '科技':
        return '极客观察';
      case '财经':
        return '财经研报';
      case '体育':
        return '体育前线';
      case '娱乐':
        return '娱闻快报';
      default:
        return '猫咪头条';
    }
  }

  static Future<JsonMap> newsArticleGet(JsonMap body) async {
    await Future<void>.delayed(const Duration(milliseconds: 350));

    final id = body['id'] as String? ?? '';
    final parts = id.split('-');
    final channel = parts.isNotEmpty ? parts.first : '推荐';
    final title = '$channel | 资讯详情（Demo） $id';
    final summary = '这是列表摘要的延续，用于演示详情页通过 RPC 拉取正文。';
    final bodyText = StringBuffer()
      ..writeln('（Demo）本文为占位内容，后端就绪后由 $id 对应的真实正文替换。')
      ..writeln()
      ..writeln('第二段：支持多段排版与换行，便于预览阅读体验。')
      ..writeln()
      ..writeln('第三段：评论区、相关推荐等可在后续迭代接入。');

    return <String, dynamic>{
      'id': id,
      'title': title,
      'summary': summary,
      'source': _sourceByChannel(channel),
      'publishTime': '刚刚',
      'commentCount': 120,
      'tag': channel,
      'body': bodyText.toString(),
    };
  }

  static Future<JsonMap> newsHotList(JsonMap body) async {
    await Future<void>.delayed(const Duration(milliseconds: 200));

    const titles = <String>[
      '多地发布春日出行提示',
      'AI 助手更新引发讨论',
      '赛事回顾：焦点战复盘',
      '财经速览：市场关注要点',
      '新剧定档，阵容公布',
      '科技新品发布会前瞻',
      '本地生活优惠合集',
      '汽车圈一周热点速读',
    ];

    final items = List<JsonMap>.generate(titles.length, (index) {
      final rank = index + 1;
      return <String, dynamic>{
        'rank': rank,
        'title': titles[index],
        'id': 'hot-$rank',
      };
    });

    return <String, dynamic>{'items': items};
  }
}
