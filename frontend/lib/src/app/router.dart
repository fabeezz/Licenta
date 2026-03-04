import 'package:go_router/go_router.dart';

import '../features/wardrobe/screens/recommendation_screen.dart';
import '../features/wardrobe/screens/stats_screen.dart';
import '../features/wardrobe/screens/upload_item_screen.dart';
import '../features/wardrobe/screens/wardrobe_list_screen.dart';

GoRouter buildRouter() {
  return GoRouter(
    initialLocation: '/',
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => const WardrobeListScreen(),
        routes: [
          GoRoute(
            path: 'upload',
            builder: (context, state) => const UploadItemScreen(),
          ),
          GoRoute(
            path: 'stats',
            builder: (context, state) => const StatsScreen(),
          ),
          GoRoute(
            path: 'recommendation',
            builder: (context, state) => const RecommendationScreen(),
          ),
        ],
      ),
    ],
  );
}

