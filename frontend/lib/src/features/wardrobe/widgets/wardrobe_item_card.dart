import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../../core/config/app_config.dart';
import '../models/wardrobe_item.dart';

class WardrobeItemCard extends StatelessWidget {
  final WardrobeItem item;
  final VoidCallback? onMarkWorn;

  const WardrobeItemCard({super.key, required this.item, this.onMarkWorn});

  @override
  Widget build(BuildContext context) {
    final created = DateFormat.yMMMd().add_Hm().format(item.createdAt);

    return Card(
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          AspectRatio(
            aspectRatio: 1,
            child: Image.network(
              AppConfig.mediaUrl(item.bestImageName),
              fit: BoxFit.cover,
              errorBuilder: (context, error, stack) =>
                  const Center(child: Icon(Icons.broken_image_outlined)),
              loadingBuilder: (context, child, progress) {
                if (progress == null) return child;
                return const Center(child: CircularProgressIndicator());
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  (item.category ?? 'unknown').toUpperCase(),
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 6),
                Wrap(
                  spacing: 8,
                  runSpacing: 6,
                  children: [
                    if (item.material != null && item.material!.isNotEmpty)
                      _Chip(text: 'material: ${item.material}'),
                    if (item.season != null && item.season!.isNotEmpty)
                      _Chip(text: 'season: ${item.season}'),
                    if (item.occasion != null && item.occasion!.isNotEmpty)
                      _Chip(text: 'occasion: ${item.occasion}'),
                    _Chip(text: 'wear: ${item.wearCount}'),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  'Created: $created',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
                const SizedBox(height: 10),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton.icon(
                    onPressed: onMarkWorn,
                    icon: const Icon(Icons.check_circle_outline),
                    label: const Text('Mark worn'),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _Chip extends StatelessWidget {
  final String text;

  const _Chip({required this.text});

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.secondaryContainer,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        child: Text(text, style: Theme.of(context).textTheme.labelMedium),
      ),
    );
  }
}
