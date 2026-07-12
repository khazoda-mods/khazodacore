package com.khazoda.core.config;

import com.khazoda.core.config.KhazConfig.Entry;
import com.khazoda.core.config.KhazConfig.ValueAdapter;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

final class KhazConfigHelpers {
  private KhazConfigHelpers() {
  }

  static Entry<Boolean> createBooleanEntry(String key, boolean defaultValue, String comment) {
    return createEntry(key, defaultValue, comment, valueAdapter(raw -> {
      if ("true".equalsIgnoreCase(raw)) return Optional.of(true);
      if ("false".equalsIgnoreCase(raw)) return Optional.of(false);
      return Optional.empty();
    }, value -> Boolean.toString(value)));
  }

  static Entry<Integer> createIntegerEntry(String key, int defaultValue, int min, int max, String comment) {
    return createEntry(key, defaultValue, commentAppendRange(comment, min, max), valueAdapter(raw -> {
      try {
        int parsed = Integer.parseInt(raw);
        return parsed < min || parsed > max ? Optional.empty() : Optional.of(parsed);
      } catch (NumberFormatException ignored) {
        return Optional.empty();
      }
    }, value -> Integer.toString(clamp(value, min, max))));
  }

  static Entry<Double> createDecimalEntry(String key, double defaultValue, double min, double max, String comment) {
    return createEntry(key, defaultValue, commentAppendRange(comment, min, max), valueAdapter(raw -> {
      try {
        double parsed = Double.parseDouble(raw);
        return !Double.isFinite(parsed) || parsed < min || parsed > max ? Optional.empty() : Optional.of(parsed);
      } catch (NumberFormatException ignored) {
        return Optional.empty();
      }
    }, value -> Double.toString(clamp(value, min, max))));
  }

  static Entry<String> createStringEntry(String key, String defaultValue, String comment) {
    return createEntry(key, defaultValue, comment, valueAdapter(Optional::of, value -> value));
  }

  static <E extends Enum<E>> Entry<E> createEnumEntry(String key, E defaultValue, String comment) {
    Class<E> enumClass = defaultValue.getDeclaringClass();
    return createEntry(key, defaultValue, comment, valueAdapter(raw -> parseEnum(enumClass, raw), value -> value.name().toLowerCase(Locale.ROOT)));
  }

  private static <T> Entry<T> createEntry(String key, T defaultValue, String comment, ValueAdapter<T> adapter) {
    T normalizedDefaultValue = adapter.normalize(defaultValue, defaultValue);
    String fullComment = commentAppendDefaultValue(comment, adapter.format(normalizedDefaultValue));
    return new Entry<>(key, normalizedDefaultValue, fullComment, adapter);
  }

  private static <T> ValueAdapter<T> valueAdapter(Function<String, Optional<T>> parser, Function<T, String> formatter) {
    return new FunctionalValueAdapter<>(parser, formatter);
  }

  private static <E extends Enum<E>> Optional<E> parseEnum(Class<E> enumClass, String raw) {
    for (E value : enumClass.getEnumConstants()) {
      if (value.name().equalsIgnoreCase(raw)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
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

  static <T> T readValue(Entry<T> entry, @Nullable String raw) {
    if (raw == null) {
      return entry.defaultValue();
    }
    return entry.adapter().parse(raw.trim(), entry.defaultValue());
  }

  static <T> String formatValue(Entry<T> entry, Object value) {
    @SuppressWarnings("unchecked") T typedValue = (T) value;
    return entry.adapter().format(typedValue);
  }

  private record FunctionalValueAdapter<T>(Function<String, Optional<T>> parser,
                                           Function<T, String> formatter) implements ValueAdapter<T> {
    @Override
    public Optional<T> parse(String raw) {
      return parser.apply(raw);
    }

    @Override
    public String format(T value) {
      return formatter.apply(value);
    }
  }
}