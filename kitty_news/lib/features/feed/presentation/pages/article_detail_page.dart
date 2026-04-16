import 'package:flutter/material.dart';
import 'package:kitty_news/core/network/rpc_client.dart';
import 'package:kitty_news/features/feed/data/api/news_api.dart';
import 'package:kitty_news/features/feed/data/models/news_article.dart';

class ArticleDetailPage extends StatefulWidget {
  const ArticleDetailPage({
    super.key,
    required this.rpcCall,
    required this.preview,
  });

  final RpcCall rpcCall;
  final NewsArticle preview;

  @override
  State<ArticleDetailPage> createState() => _ArticleDetailPageState();
}

class _ArticleDetailPageState extends State<ArticleDetailPage> {
  NewsArticle? _loaded;
  bool _loading = true;
  String? _errorText;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _errorText = null;
    });

    try {
      final full = await fetchNewsArticleById(widget.rpcCall, widget.preview.id);
      if (!mounted) return;
      setState(() {
        _loaded = full;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _errorText = '正文加载失败，点击重试';
      });
    } finally {
      if (mounted) {
        setState(() {
          _loading = false;
        });
      }
    }
  }

  NewsArticle get _display => _loaded ?? widget.preview;

  @override
  Widget build(BuildContext context) {
    final article = _display;

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        foregroundColor: const Color(0xFF222222),
        elevation: 0,
        title: const Text('资讯详情'),
        actions: [
          IconButton(icon: const Icon(Icons.share_outlined), onPressed: () {}),
          IconButton(icon: const Icon(Icons.more_horiz), onPressed: () {}),
        ],
      ),
      body: Column(
        children: [
          if (_loading) const LinearProgressIndicator(minHeight: 2),
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    article.title,
                    style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.w700,
                      height: 1.35,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 6,
                          vertical: 2,
                        ),
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(4),
                          color: const Color(0xFFFFEAEA),
                        ),
                        child: Text(
                          article.tag,
                          style: const TextStyle(
                            color: Color(0xFFED4040),
                            fontSize: 11,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        article.source,
                        style: const TextStyle(
                          fontSize: 12,
                          color: Color(0xFF999999),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        article.publishTime,
                        style: const TextStyle(
                          fontSize: 12,
                          color: Color(0xFF999999),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        '${article.commentCount} 评论',
                        style: const TextStyle(
                          fontSize: 12,
                          color: Color(0xFF999999),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  if (article.summary.isNotEmpty)
                    Text(
                      article.summary,
                      style: const TextStyle(
                        fontSize: 15,
                        color: Color(0xFF444444),
                        height: 1.55,
                      ),
                    ),
                  if (article.summary.isNotEmpty) const SizedBox(height: 16),
                  Text(
                    article.body ??
                        (_loading ? '正文加载中…' : '（暂无正文）'),
                    style: const TextStyle(
                      fontSize: 16,
                      color: Color(0xFF222222),
                      height: 1.75,
                    ),
                  ),
                  if (_errorText != null) ...[
                    const SizedBox(height: 16),
                    TextButton(onPressed: _load, child: Text(_errorText!)),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
