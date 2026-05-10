package com.khazoda.core;

import com.khazoda.core.KhazConfig.Entry;
import com.khazoda.core.KhazConfig.ValueAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

final class KhazConfigHelpers {
  private KhazConfigHelpers() {
  }

  static Entry<Boolean> createBooleanEntry(String key, boolean defaultValue, String comment) {
    return createEntry(
        key,
        defaultValue,
        comment,
        valueAdapter((raw, fallback) -> {
          if ("true".equalsIgnoreCase(raw)) return true;
          if ("false".equalsIgnoreCase(raw)) return false;
          return fallback;
        }, value -> Boolean.toString(value))
    );
  }

  static Entry<Integer> createIntegerEntry(String key, int defaultValue, int min, int max, String comment) {
    return createEntry(
        key,
        defaultValue,
        commentAppendRange(comment, min, max),
        valueAdapter((raw, fallback) -> {
          try {
            return clamp(Integer.parseInt(raw), min, max);
          } catch (NumberFormatException ignored) {
            return fallback;
          }
        }, value -> Integer.toString(clamp(value, min, max)))
    );
  }

  static Entry<Double> createDecimalEntry(String key, double defaultValue, double min, double max, String comment) {
    return createEntry(
        key,
        defaultValue,
        commentAppendRange(comment, min, max),
        valueAdapter((raw, fallback) -> {
          try {
            return clamp(Double.parseDouble(raw), min, max);
          } catch (NumberFormatException ignored) {
            return fallback;
          }
        }, value -> Double.toString(clamp(value, min, max)))
    );
  }

  static Entry<String> createStringEntry(String key, String defaultValue, String comment) {
    return createEntry(
        key,
        defaultValue,
        comment,
        valueAdapter((raw, fallback) -> raw, value -> value)
    );
  }

  private static <T> Entry<T> createEntry(String key, T defaultValue, String comment, ValueAdapter<T> adapter) {
    T normalizedDefaultValue = adapter.parse(adapter.format(defaultValue), defaultValue);
    String fullComment = commentAppendDefaultValue(comment, adapter.format(normalizedDefaultValue));
    return new Entry<>(key, normalizedDefaultValue, fullComment, adapter);
  }

  private static <T> ValueAdapter<T> valueAdapter(BiFunction<String, T, T> parser, Function<T, String> formatter) {
    return new FunctionalValueAdapter<>(parser, formatter);
  }

  private static String commentAppendRange(String comment, Number min, Number max) {
    return comment + " Range: " + min + "-" + max + ".";
  }

  private static String commentAppendDefaultValue(String comment, String defaultValue) {
    return comment + " Default: " + defaultValue + ".";
  }

  static List<String> splitCommentLines(String text) {
    return List.of(text.split("\\R"));
  }

  static String formatConfigTitle(String modName, String modId) {
    return modName.trim() + " (" + modId + ") Config";
  }

  static String requireNonBlank(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value.trim();
  }

  static void validateEntries(String modId, List<Entry<?>> entries) {
    Set<String> seenKeys = new HashSet<>();
    for (Entry<?> entry : entries) {
      if (!seenKeys.add(entry.key())) {
        throw new IllegalArgumentException("Duplicate config key '" + entry.key() + "' in " + modId);
      }
    }
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  static <T> T readValue(Entry<T> entry, String raw) {
    if (raw == null) {
      return entry.defaultValue();
    }
    return entry.adapter().parse(raw.trim(), entry.defaultValue());
  }

  static <T> String formatValue(Entry<T> entry, Object value) {
    @SuppressWarnings("unchecked") T typedValue = (T) value;
    return entry.adapter().format(typedValue);
  }

  private record FunctionalValueAdapter<T>(
      BiFunction<String, T, T> parser,
      Function<T, String> formatter
  ) implements ValueAdapter<T> {
    @Override
    public T parse(String raw, T fallback) {
      return parser.apply(raw, fallback);
    }

    @Override
    public String format(T value) {
      return formatter.apply(value);
    }
  }
}
