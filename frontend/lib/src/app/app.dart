import 'package:flutter/material.dart';

import 'router.dart';

class WardrobeApp extends StatelessWidget {
  const WardrobeApp({super.key});

  @override
  Widget build(BuildContext context) {
    final router = buildRouter();

    return MaterialApp.router(
      title: 'Wardrobe',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color.fromARGB(255, 86, 247, 169),
        ),
        useMaterial3: true,
      ),
      routerConfig: router,
    );
  }
}
