// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';
import 'package:kitty_news/app/news_app.dart';

void main() {
  testWidgets('Kitty News app smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const NewsApp());
    await tester.pump(const Duration(milliseconds: 700));

    expect(find.text('首页'), findsOneWidget);
    expect(find.text('推荐'), findsAtLeastNWidgets(1));
  });
}
