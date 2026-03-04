import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/http/dio_provider.dart';
import '../data/wardrobe_api.dart';
import '../models/wardrobe_item.dart';
import '../models/wardrobe_stats.dart';

final wardrobeApiProvider = Provider<WardrobeApi>((ref) {
  final dio = ref.watch(dioProvider);
  return WardrobeApi(dio);
});

final wardrobeItemsProvider = FutureProvider.autoDispose<List<WardrobeItem>>((ref) async {
  final api = ref.watch(wardrobeApiProvider);
  return api.listItems(sortBy: 'created_at', sortDir: 'desc', limit: 50, offset: 0);
});

final wardrobeStatsProvider = FutureProvider.autoDispose<WardrobeStats>((ref) async {
  final api = ref.watch(wardrobeApiProvider);
  return api.getStats();
});

final wardrobeRecommendationProvider =
    FutureProvider.autoDispose<List<WardrobeItem>>((ref) async {
  final api = ref.watch(wardrobeApiProvider);
  return api.getRecommendation(limit: 3);
});

