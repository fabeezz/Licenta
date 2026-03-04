import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';

import '../providers/wardrobe_providers.dart';

class UploadItemScreen extends ConsumerStatefulWidget {
  const UploadItemScreen({super.key});

  @override
  ConsumerState<UploadItemScreen> createState() => _UploadItemScreenState();
}

class _UploadItemScreenState extends ConsumerState<UploadItemScreen> {
  final _picker = ImagePicker();

  XFile? _picked;
  bool _submitting = false;

  final _brandCtrl = TextEditingController();
  final _materialCtrl = TextEditingController();
  final _seasonCtrl = TextEditingController();
  final _occasionCtrl = TextEditingController();

  @override
  void dispose() {
    _brandCtrl.dispose();
    _materialCtrl.dispose();
    _seasonCtrl.dispose();
    _occasionCtrl.dispose();
    super.dispose();
  }

  Future<void> _pickImage(ImageSource source) async {
    final file = await _picker.pickImage(
      source: source,
      maxWidth: 1600,
      imageQuality: 92,
    );
    if (!mounted) return;
    setState(() => _picked = file);
  }

  Future<void> _submit() async {
    if (_picked == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Pick an image first.')),
      );
      return;
    }

    setState(() => _submitting = true);
    final api = ref.read(wardrobeApiProvider);

    try {
      await api.uploadItem(
        imageFile: File(_picked!.path),
        brand: _brandCtrl.text.trim().isEmpty ? null : _brandCtrl.text.trim(),
        material:
            _materialCtrl.text.trim().isEmpty ? null : _materialCtrl.text.trim(),
        season:
            _seasonCtrl.text.trim().isEmpty ? null : _seasonCtrl.text.trim(),
        occasion: _occasionCtrl.text.trim().isEmpty
            ? null
            : _occasionCtrl.text.trim(),
      );

      ref.invalidate(wardrobeItemsProvider);

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Uploaded successfully.')),
      );
      context.pop();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Upload failed: $e')),
      );
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final picked = _picked;

    return Scaffold(
      appBar: AppBar(title: const Text('Add item')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: _submitting ? null : () => _pickImage(ImageSource.gallery),
                  icon: const Icon(Icons.photo_library_outlined),
                  label: const Text('Gallery'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: _submitting ? null : () => _pickImage(ImageSource.camera),
                  icon: const Icon(Icons.photo_camera_outlined),
                  label: const Text('Camera'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          AspectRatio(
            aspectRatio: 1,
            child: DecoratedBox(
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(12),
              ),
              child: picked == null
                  ? const Center(child: Text('No image selected'))
                  : ClipRRect(
                      borderRadius: BorderRadius.circular(12),
                      child: Image.file(
                        File(picked.path),
                        fit: BoxFit.cover,
                      ),
                    ),
            ),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _brandCtrl,
            enabled: !_submitting,
            decoration: const InputDecoration(
              labelText: 'Brand (optional)',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _materialCtrl,
            enabled: !_submitting,
            decoration: const InputDecoration(
              labelText: 'Material (optional)',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _seasonCtrl,
            enabled: !_submitting,
            decoration: const InputDecoration(
              labelText: 'Season (optional)',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _occasionCtrl,
            enabled: !_submitting,
            decoration: const InputDecoration(
              labelText: 'Occasion (optional)',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 16),
          FilledButton.icon(
            onPressed: _submitting ? null : _submit,
            icon: _submitting
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.cloud_upload_outlined),
            label: Text(_submitting ? 'Uploading...' : 'Upload'),
          ),
        ],
      ),
    );
  }
}

