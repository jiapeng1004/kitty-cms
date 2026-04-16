import 'package:flutter/material.dart';

/// 频道编辑：「推荐」固定置顶；其余可排序、移除；底部从池中添加。
Future<void> showChannelEditorSheet({
  required BuildContext context,
  required List<String> pool,
  required List<String> visible,
  required ValueChanged<List<String>> onApply,
}) {
  return showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.white,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
    ),
    builder: (context) {
      return _ChannelEditorSheetBody(
        pool: pool,
        initialVisible: List<String>.from(visible),
        onApply: onApply,
      );
    },
  );
}

class _ChannelEditorSheetBody extends StatefulWidget {
  const _ChannelEditorSheetBody({
    required this.pool,
    required this.initialVisible,
    required this.onApply,
  });

  final List<String> pool;
  final List<String> initialVisible;
  final ValueChanged<List<String>> onApply;

  @override
  State<_ChannelEditorSheetBody> createState() =>
      _ChannelEditorSheetBodyState();
}

class _ChannelEditorSheetBodyState extends State<_ChannelEditorSheetBody> {
  late List<String> _visible;

  @override
  void initState() {
    super.initState();
    _visible = _normalizeInitial(widget.initialVisible);
  }

  List<String> _normalizeInitial(List<String> raw) {
    const pinned = '推荐';
    final filtered =
        raw.where((name) => widget.pool.contains(name)).toList();
    if (!filtered.contains(pinned)) {
      filtered.insert(0, pinned);
    } else {
      filtered
        ..remove(pinned)
        ..insert(0, pinned);
    }
    return filtered.isEmpty ? <String>[pinned] : filtered;
  }

  List<String> get _hidden =>
      widget.pool.where((c) => !_visible.contains(c)).toList();

  void _remove(String name) {
    if (name == '推荐') {
      return;
    }
    setState(() {
      _visible.remove(name);
    });
  }

  void _add(String name) {
    if (_visible.contains(name)) {
      return;
    }
    setState(() {
      _visible.add(name);
    });
  }

  void _reorderTail(int oldIndex, int newIndex) {
    setState(() {
      final pinned = _visible.first;
      final tail = List<String>.from(_visible.sublist(1));
      final item = tail.removeAt(oldIndex);
      tail.insert(newIndex, item);
      _visible = <String>[pinned, ...tail];
    });
  }

  @override
  Widget build(BuildContext context) {
    final media = MediaQuery.of(context);
    final tail = _visible.length > 1 ? _visible.sublist(1) : <String>[];

    return SafeArea(
      child: Padding(
        padding: EdgeInsets.only(bottom: media.viewInsets.bottom),
        child: SizedBox(
          height: media.size.height * 0.72,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 12, 8, 8),
                child: Row(
                  children: [
                    const Text(
                      '频道管理',
                      style: TextStyle(
                        fontSize: 17,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const Spacer(),
                    TextButton(
                      onPressed: () => Navigator.of(context).pop(),
                      child: const Text('取消'),
                    ),
                    TextButton(
                      onPressed: () {
                        widget.onApply(List<String>.from(_visible));
                        Navigator.of(context).pop();
                      },
                      child: const Text('完成'),
                    ),
                  ],
                ),
              ),
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 16),
                child: Text(
                  '我的频道（长按拖动排序）',
                  style: TextStyle(fontSize: 12, color: Color(0xFF999999)),
                ),
              ),
              const SizedBox(height: 8),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Row(
                  children: [
                    _PinnedChip(label: _visible.first),
                    const SizedBox(width: 8),
                    const Text(
                      '置顶频道不可移除',
                      style: TextStyle(fontSize: 12, color: Color(0xFFBBBBBB)),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 8),
              Expanded(
                child: tail.isEmpty
                    ? const Center(
                        child: Text(
                          '暂无其它频道，请在下方添加',
                          style: TextStyle(color: Color(0xFF999999)),
                        ),
                      )
                    : ReorderableListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 6),
                        itemCount: tail.length,
                        onReorder: (oldIndex, newIndex) {
                          var next = newIndex;
                          if (oldIndex < next) {
                            next -= 1;
                          }
                          _reorderTail(oldIndex, next);
                        },
                        itemBuilder: (context, index) {
                          final name = tail[index];
                          return ListTile(
                            key: ValueKey<String>(name),
                            leading: const Icon(Icons.drag_handle),
                            title: Text(name),
                            trailing: IconButton(
                              icon: const Icon(Icons.close, size: 20),
                              onPressed: () => _remove(name),
                            ),
                          );
                        },
                      ),
              ),
              const Divider(height: 1),
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
                child: Text(
                  '点击添加频道（${_hidden.length}）',
                  style: const TextStyle(
                    fontSize: 12,
                    color: Color(0xFF999999),
                  ),
                ),
              ),
              Padding(
                padding: const EdgeInsets.fromLTRB(12, 0, 12, 16),
                child: Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: _hidden
                      .map(
                        (name) => ActionChip(
                          label: Text(name),
                          onPressed: () => _add(name),
                        ),
                      )
                      .toList(),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _PinnedChip extends StatelessWidget {
  const _PinnedChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        color: const Color(0xFFF2F3F5),
      ),
      child: Text(
        label,
        style: const TextStyle(
          fontWeight: FontWeight.w600,
          color: Color(0xFF222222),
        ),
      ),
    );
  }
}
