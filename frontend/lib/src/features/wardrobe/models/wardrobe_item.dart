class WardrobeItem {
  final int id;
  final String imageOriginalName;
  final String? imageNoBgName;
  final String? category;
  final Map<String, dynamic>? colorTags;
  final String? brand;
  final String? material;
  final String? season;
  final String? occasion;
  final int wearCount;
  final DateTime? lastWornAt;
  final DateTime createdAt;

  const WardrobeItem({
    required this.id,
    required this.imageOriginalName,
    required this.imageNoBgName,
    required this.category,
    required this.colorTags,
    required this.brand,
    required this.material,
    required this.season,
    required this.occasion,
    required this.wearCount,
    required this.lastWornAt,
    required this.createdAt,
  });

  String get bestImageName => imageNoBgName ?? imageOriginalName;

  factory WardrobeItem.fromJson(Map<String, dynamic> json) {
    return WardrobeItem(
      id: json['id'] as int,
      imageOriginalName: json['image_original_name'] as String,
      imageNoBgName: json['image_no_bg_name'] as String?,
      category: json['category'] as String?,
      colorTags: (json['color_tags'] as Map?)?.cast<String, dynamic>(),
      brand: json['brand'] as String?,
      material: json['material'] as String?,
      season: json['season'] as String?,
      occasion: json['occasion'] as String?,
      wearCount: json['wear_count'] as int,
      lastWornAt: json['last_worn_at'] != null
          ? DateTime.parse(json['last_worn_at'] as String)
          : null,
      createdAt: DateTime.parse(json['created_at'] as String),
    );
  }
}

