import 'dart:io';

import 'package:dio/dio.dart';

import '../../../core/http/api_exception.dart';
import '../models/wardrobe_item.dart';
import '../models/wardrobe_stats.dart';

class WardrobeApi {
  final Dio _dio;

  WardrobeApi(this._dio);

  Future<List<WardrobeItem>> listItems({
    String? category,
    String? brand,
    String? material,
    String? season,
    String? occasion,
    String sortBy = 'created_at',
    String sortDir = 'desc',
    int limit = 50,
    int offset = 0,
  }) async {
    try {
      final resp = await _dio.get<List<dynamic>>(
        '/wardrobe/items',
        queryParameters: {
          if (category != null && category.isNotEmpty) 'category': category,
          if (brand != null && brand.isNotEmpty) 'brand': brand,
          if (material != null && material.isNotEmpty) 'material': material,
          if (season != null && season.isNotEmpty) 'season': season,
          if (occasion != null && occasion.isNotEmpty) 'occasion': occasion,
          'sort_by': sortBy,
          'sort_dir': sortDir,
          'limit': limit,
          'offset': offset,
        },
      );

      final data = resp.data ?? const [];
      return data
          .map((e) => WardrobeItem.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw ApiException(
        e.response?.data?.toString() ?? e.message ?? 'Failed to list items',
        statusCode: e.response?.statusCode,
      );
    }
  }

  /// Uploads an item image.
  ///
  /// Backend note: in your FastAPI `POST /wardrobe/items`, these metadata fields
  /// are query parameters (not multipart form fields), because they are not declared
  /// with `Form(...)`.
  Future<WardrobeItem> uploadItem({
    required File imageFile,
    String? brand,
    String? material,
    String? season,
    String? occasion,
  }) async {
    try {
      final form = FormData.fromMap({
        'image': await MultipartFile.fromFile(imageFile.path),
      });

      final resp = await _dio.post<Map<String, dynamic>>(
        '/wardrobe/items',
        data: form,
        queryParameters: {
          if (brand != null && brand.isNotEmpty) 'brand': brand,
          if (material != null && material.isNotEmpty) 'material': material,
          if (season != null && season.isNotEmpty) 'season': season,
          if (occasion != null && occasion.isNotEmpty) 'occasion': occasion,
        },
      );

      if (resp.data == null) {
        throw ApiException('Empty response from server');
      }

      return WardrobeItem.fromJson(resp.data!);
    } on DioException catch (e) {
      throw ApiException(
        e.response?.data?.toString() ?? e.message ?? 'Failed to upload item',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<WardrobeItem> markWorn(int id) async {
    try {
      final resp = await _dio.post<Map<String, dynamic>>('/wardrobe/items/$id/wear');
      if (resp.data == null) {
        throw ApiException('Empty response from server');
      }
      return WardrobeItem.fromJson(resp.data!);
    } on DioException catch (e) {
      throw ApiException(
        e.response?.data?.toString() ?? e.message ?? 'Failed to mark worn',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<WardrobeStats> getStats() async {
    try {
      final resp = await _dio.get<Map<String, dynamic>>('/wardrobe/stats');
      if (resp.data == null) {
        throw ApiException('Empty response from server');
      }
      return WardrobeStats.fromJson(resp.data!);
    } on DioException catch (e) {
      throw ApiException(
        e.response?.data?.toString() ?? e.message ?? 'Failed to load stats',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<List<WardrobeItem>> getRecommendation({
    String? occasion,
    String? season,
    int limit = 3,
  }) async {
    try {
      final resp = await _dio.get<List<dynamic>>(
        '/wardrobe/recommendation',
        queryParameters: {
          if (occasion != null && occasion.isNotEmpty) 'occasion': occasion,
          if (season != null && season.isNotEmpty) 'season': season,
          'limit': limit,
        },
      );

      final data = resp.data ?? const [];
      return data
          .map((e) => WardrobeItem.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw ApiException(
        e.response?.data?.toString() ?? e.message ?? 'Failed to load recommendation',
        statusCode: e.response?.statusCode,
      );
    }
  }
}

