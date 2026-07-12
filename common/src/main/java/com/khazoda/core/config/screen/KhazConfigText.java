package com.khazoda.core.config.screen;

import com.khazoda.core.config.KhazConfig;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

final class KhazConfigText {
  private KhazConfigText() {
  }

  static Component label(KhazConfig config, KhazConfig.Entry<?> entry) {
    return Component.translatableWithFallback(labelKey(config, entry), humanizeKey(entry.key()));
  }

  static @Nullable Component tooltip(KhazConfig config, KhazConfig.Entry<?> entry) {
    String key = labelKey(config, entry) + ".tooltip";
    return entry.comment().isBlank() && !I18n.exists(key) ? null : Component.translatableWithFallback(key, entry.comment());
  }

  static Component enumValueLabel(KhazConfig config, KhazConfig.Entry<?> entry, Enum<?> value) {
    String valueKey = labelKey(config, entry) + ".value." + value.name().toLowerCase(Locale.ROOT);
    return Component.translatableWithFallback(valueKey, humanizeKey(value.name()));
  }

  private static String labelKey(KhazConfig config, KhazConfig.Entry<?> entry) {
    return "config." + config.modId() + "." + entry.key();
  }

  private static String humanizeKey(String key) {
    String spaced = key.replace('_', ' ').replace('-', ' ').replace('.', ' ').trim().toLowerCase(Locale.ROOT);
    if (spaced.isEmpty()) {
      return key;
    }
    return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
  }
}