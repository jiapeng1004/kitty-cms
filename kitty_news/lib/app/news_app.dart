import 'package:flutter/material.dart';
import 'package:kitty_news/core/network/kitty_rpc_client_factory.dart';
import 'package:kitty_news/core/network/rpc_config.dart';
import 'package:kitty_news/features/feed/presentation/pages/home_page.dart';

class NewsApp extends StatelessWidget {
  const NewsApp({super.key});

  @override
  Widget build(BuildContext context) {
    final rpc = createKittyRpcClient(RpcAppConfig.instance);

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Kitty News',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFFED4040)),
        scaffoldBackgroundColor: const Color(0xFFF5F5F5),
        useMaterial3: true,
      ),
      home: HomePage(rpcCall: rpc),
    );
  }
}
