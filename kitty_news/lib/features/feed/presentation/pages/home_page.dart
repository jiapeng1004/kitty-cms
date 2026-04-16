import 'package:flutter/material.dart';
import 'package:kitty_news/core/network/rpc_client.dart';
import 'package:kitty_news/features/feed/data/api/news_api.dart';
import 'package:kitty_news/features/feed/data/models/hot_topic.dart';
import 'package:kitty_news/features/feed/data/models/news_article.dart';
import 'package:kitty_news/features/feed/presentation/pages/article_detail_page.dart';
import 'package:kitty_news/features/feed/presentation/widgets/channel_editor_sheet.dart';
import 'package:kitty_news/features/feed/presentation/widgets/hot_topics_bar.dart';
import 'package:kitty_news/features/feed/presentation/widgets/news_card.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.rpcCall});

  final RpcCall rpcCall;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const List<String> _channelPool = <String>[
    '推荐',
    '热点',
    '科技',
    '财经',
    '体育',
    '娱乐',
    '本地',
    '国际',
    '游戏',
    '汽车',
  ];

  late List<String> _visibleChannels;

  final List<NewsArticle> _items = <NewsArticle>[];
  final ScrollController _scrollController = ScrollController();

  List<HotTopic> _hotTopics = <HotTopic>[];
  bool _hotLoading = true;

  String _currentChannel = '推荐';
  int _cursor = 0;
  bool _hasMore = true;
  bool _isLoading = false;
  bool _isLoadingMore = false;
  String? _errorText;
  int _currentNavIndex = 0;

  @override
  void initState() {
    super.initState();
    _visibleChannels = _channelPool.take(6).toList();
    _currentChannel = _visibleChannels.first;
    _scrollController.addListener(_onScroll);
    _refreshFeed();
    _loadHotTopics();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _loadHotTopics() async {
    setState(() {
      _hotLoading = true;
    });
    try {
      final list = await fetchNewsHotTopics(widget.rpcCall);
      if (!mounted) return;
      setState(() {
        _hotTopics = list;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _hotTopics = <HotTopic>[];
      });
    } finally {
      if (mounted) {
        setState(() {
          _hotLoading = false;
        });
      }
    }
  }

  void _openArticle(NewsArticle article) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (context) => ArticleDetailPage(
          rpcCall: widget.rpcCall,
          preview: article,
        ),
      ),
    );
  }

  Future<void> _refreshFeed() async {
    setState(() {
      _isLoading = true;
      _errorText = null;
      _cursor = 0;
      _hasMore = true;
    });

    try {
      final page = await fetchNewsFeed(
        widget.rpcCall,
        channel: _currentChannel,
        cursor: 0,
      );
      if (!mounted) return;
      setState(() {
        _items
          ..clear()
          ..addAll(page.items);
        _cursor = page.nextCursor;
        _hasMore = page.hasMore;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _errorText = '加载失败，请下拉重试';
      });
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _loadMore() async {
    if (_isLoadingMore || !_hasMore || _isLoading) {
      return;
    }

    setState(() {
      _isLoadingMore = true;
    });

    try {
      final page = await fetchNewsFeed(
        widget.rpcCall,
        channel: _currentChannel,
        cursor: _cursor,
      );
      if (!mounted) return;
      setState(() {
        _items.addAll(page.items);
        _cursor = page.nextCursor;
        _hasMore = page.hasMore;
      });
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('加载更多失败')));
    } finally {
      if (mounted) {
        setState(() {
          _isLoadingMore = false;
        });
      }
    }
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
      _loadMore();
    }
  }

  void _onChangeChannel(String channel) {
    if (channel == _currentChannel) {
      return;
    }
    setState(() {
      _currentChannel = channel;
    });
    _refreshFeed();
  }

  Future<void> _openChannelEditor() async {
    await showChannelEditorSheet(
      context: context,
      pool: _channelPool,
      visible: _visibleChannels,
      onApply: (next) {
        setState(() {
          _visibleChannels = next;
          if (!_visibleChannels.contains(_currentChannel)) {
            _currentChannel = _visibleChannels.first;
          }
        });
        _refreshFeed();
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(56),
        child: AppBar(
          backgroundColor: Colors.white,
          titleSpacing: 12,
          title: _SearchBar(channel: _currentChannel),
          actions: const [
            Padding(
              padding: EdgeInsets.only(right: 12),
              child: Icon(Icons.camera_alt_outlined),
            ),
          ],
        ),
      ),
      body: Column(
        children: [
          _ChannelBar(
            channels: _visibleChannels,
            currentChannel: _currentChannel,
            onTap: _onChangeChannel,
            onEditTap: _openChannelEditor,
          ),
          HotTopicsBar(
            topics: _hotTopics,
            loading: _hotLoading,
            onTopicTap: (topic) {
              _openArticle(
                NewsArticle(
                  id: topic.id,
                  title: topic.title,
                  summary: '',
                  source: '热榜',
                  publishTime: '',
                  commentCount: 0,
                  tag: '热点',
                ),
              );
            },
          ),
          Expanded(child: _buildFeedBody()),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentNavIndex,
        onDestinationSelected: (index) {
          setState(() {
            _currentNavIndex = index;
          });
        },
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), label: '首页'),
          NavigationDestination(
            icon: Icon(Icons.play_circle_outline),
            label: '视频',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_add_alt_1_outlined),
            label: '关注',
          ),
          NavigationDestination(icon: Icon(Icons.person_outline), label: '我的'),
        ],
      ),
    );
  }

  Widget _buildFeedBody() {
    if (_isLoading && _items.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_errorText != null && _items.isEmpty) {
      return Center(
        child: TextButton(onPressed: _refreshFeed, child: Text(_errorText!)),
      );
    }

    return RefreshIndicator(
      onRefresh: () async {
        await _loadHotTopics();
        await _refreshFeed();
      },
      child: ListView.builder(
        controller: _scrollController,
        itemCount: _items.length + 1,
        itemBuilder: (context, index) {
          if (index == _items.length) {
            return _LoadMoreFooter(
              hasMore: _hasMore,
              isLoadingMore: _isLoadingMore,
            );
          }
          final article = _items[index];
          return NewsCard(
            article: article,
            onTap: () => _openArticle(article),
          );
        },
      ),
    );
  }
}

class _SearchBar extends StatelessWidget {
  const _SearchBar({required this.channel});

  final String channel;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 36,
      padding: const EdgeInsets.symmetric(horizontal: 12),
      decoration: BoxDecoration(
        color: const Color(0xFFF2F3F5),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Row(
        children: [
          const Icon(Icons.search, size: 18, color: Color(0xFF999999)),
          const SizedBox(width: 6),
          Text(
            '$channel 频道热点',
            style: const TextStyle(color: Color(0xFF999999), fontSize: 13),
          ),
        ],
      ),
    );
  }
}

class _ChannelBar extends StatelessWidget {
  const _ChannelBar({
    required this.channels,
    required this.currentChannel,
    required this.onTap,
    required this.onEditTap,
  });

  final List<String> channels;
  final String currentChannel;
  final ValueChanged<String> onTap;
  final VoidCallback onEditTap;

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.white,
      height: 48,
      child: Row(
        children: [
          Expanded(
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              itemBuilder: (context, index) {
                final channel = channels[index];
                final selected = channel == currentChannel;
                return GestureDetector(
                  onTap: () => onTap(channel),
                  child: Text(
                    channel,
                    style: TextStyle(
                      fontSize: selected ? 17 : 15,
                      fontWeight: selected ? FontWeight.w700 : FontWeight.w400,
                      color: selected
                          ? const Color(0xFF222222)
                          : const Color(0xFF7F7F7F),
                    ),
                  ),
                );
              },
              separatorBuilder: (context, index) => const SizedBox(width: 14),
              itemCount: channels.length,
            ),
          ),
          IconButton(
            onPressed: onEditTap,
            icon: const Icon(Icons.add, color: Color(0xFF999999)),
            tooltip: '频道管理',
          ),
        ],
      ),
    );
  }
}

class _LoadMoreFooter extends StatelessWidget {
  const _LoadMoreFooter({required this.hasMore, required this.isLoadingMore});

  final bool hasMore;
  final bool isLoadingMore;

  @override
  Widget build(BuildContext context) {
    if (isLoadingMore) {
      return const Padding(
        padding: EdgeInsets.all(16),
        child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
      );
    }

    if (!hasMore) {
      return const Padding(
        padding: EdgeInsets.all(16),
        child: Center(
          child: Text(
            '已经到底啦',
            style: TextStyle(fontSize: 12, color: Color(0xFF999999)),
          ),
        ),
      );
    }

    return const SizedBox(height: 32);
  }
}
