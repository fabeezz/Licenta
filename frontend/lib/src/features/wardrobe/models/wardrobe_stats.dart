class WardrobeStats {
  final int totalItems;
  final List<CategoryCount> byCategory;

  const WardrobeStats({
    required this.totalItems,
    required this.byCategory,
  });

  factory WardrobeStats.fromJson(Map<String, dynamic> json) {
    final byCat = (json['by_category'] as List<dynamic>? ?? [])
        .map((e) => CategoryCount.fromJson(e as Map<String, dynamic>))
        .toList();

    return WardrobeStats(
      totalItems: json['total_items'] as int? ?? 0,
      byCategory: byCat,
    );
  }
}

class CategoryCount {
  final String? category;
  final int count;

  const CategoryCount({required this.category, required this.count});

  factory CategoryCount.fromJson(Map<String, dynamic> json) {
    return CategoryCount(
      category: json['category'] as String?,
      count: json['count'] as int? ?? 0,
    );
  }
}

