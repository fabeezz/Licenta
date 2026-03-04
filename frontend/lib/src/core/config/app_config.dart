class AppConfig {
  /// Use `--dart-define=API_BASE_URL=http://...:8000` when running.
  ///
  /// Defaults:
  /// - Android emulator: http://10.0.2.2:8000
  /// - Web (if used): http://localhost:8000
  static const String apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8000',
  );

  static String mediaUrl(String filename) => '$apiBaseUrl/media/$filename';
}

