package com.khazoda.core.config;

import com.khazoda.core.config.screen.KhazConfigScreens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * KhazConfig is a simple multiloader config helper built into KhazodaCore.
 * Config files are generated and read from {mod_id}.properties files in /config.
 */
public final class KhazConfig {
  private static final Logger LOG = LoggerFactory.getLogger(KhazConfig.class);

  private final String modName;
  private final String modId;
  private final Path file;
  private final List<Entry<?>> entries;
  private final Map<Entry<?>, Object> values = new LinkedHashMap<>();
  private final Map<Entry<?>, Object> serverSyncedValues = new LinkedHashMap<>();
  private boolean loaded;

  private KhazConfig(String modName, String modId, Path configDirectory, List<Entry<?>> entries) {
    this.modName = KhazConfigHelpers.requireNonBlank(modName, "modName");
    this.modId = KhazConfigHelpers.requireNonBlank(modId, "modId");
    this.file = Objects.requireNonNull(configDirectory, "configDirectory").resolve(this.modId + ".properties");
    this.entries = List.copyOf(entries);
    KhazConfigHelpers.validateEntries(this.modId, this.entries);
  }

  public static KhazConfig of(String modName, String modId, Path configDirectory, Entry<?>... entries) {
    KhazConfig config = new KhazConfig(modName, modId, configDirectory, List.of(entries));
    KhazConfigScreens.register(config);
    return config;
  }

  public static Entry<Boolean> bool(String key, boolean defaultValue, String comment) {
    return KhazConfigHelpers.createBooleanEntry(key, defaultValue, comment);
  }

  public static Entry<Integer> integer(String key, int defaultValue, int min, int max, String comment) {
    return KhazConfigHelpers.createIntegerEntry(key, defaultValue, min, max, comment);
  }

  public static Entry<Double> decimal(String key, double defaultValue, double min, double max, String comment) {
    return KhazConfigHelpers.createDecimalEntry(key, defaultValue, min, max, comment);
  }

  public static Entry<String> string(String key, String defaultValue, String comment) {
    return KhazConfigHelpers.createStringEntry(key, defaultValue, comment);
  }

  public static <E extends Enum<E>> Entry<E> enumeration(String key, E defaultValue, String comment) {
    return KhazConfigHelpers.createEnumEntry(key, defaultValue, comment);
  }

  public synchronized void load() {
    if (loaded) return;

    Properties properties = new Properties();
    if (Files.exists(file)) {
      try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
        properties.load(reader);
      } catch (IOException e) {
        LOG.warn("Failed to read config file {}: {}", file, e.getMessage());
      }
    }

    boolean changed = !Files.exists(file);
    values.clear();
    for (Entry<?> entry : entries) {
      Object value = KhazConfigHelpers.readValue(entry, properties.getProperty(entry.key()));
      values.put(entry, value);
      String serialized = KhazConfigHelpers.formatValue(entry, value);
      if (!Objects.equals(properties.getProperty(entry.key()), serialized)) {
        changed = true;
      }
    }

    loaded = true;
    if (changed) {
      write();
    }
  }

  public synchronized void reload() {
    loaded = false;
    load();
  }

  public String modName() {
    return modName;
  }

  public String modId() {
    return modId;
  }

  public List<Entry<?>> entries() {
    return entries;
  }

  public synchronized <T> T getLocal(Entry<T> entry) {
    requireKnownEntry(entry);
    load();
    @SuppressWarnings("unchecked") T value = (T) values.getOrDefault(entry, entry.defaultValue());
    return value;
  }

  public synchronized <T> T get(Entry<T> entry) {
    requireKnownEntry(entry);
    load();
    @SuppressWarnings("unchecked") T value = (T) serverSyncedValues.getOrDefault(entry, values.getOrDefault(entry, entry.defaultValue()));
    return value;
  }

  public synchronized boolean hasServerSyncedValue(Entry<?> entry) {
    requireKnownEntry(entry);
    load();
    return serverSyncedValues.containsKey(entry);
  }

  public synchronized <T> void setLocal(Entry<T> entry, T value) {
    requireKnownEntry(entry);
    load();
    values.put(entry, entry.adapter().normalize(value, entry.defaultValue()));
  }

  public synchronized void save() {
    load();
    write();
  }

  public synchronized Map<String, String> createServerSyncSnapshot() {
    load();

    Map<String, String> snapshot = new LinkedHashMap<>();
    for (Entry<?> entry : entries) {
      if (entry.serverSynced()) {
        snapshot.put(entry.key(), KhazConfigHelpers.formatValue(entry, values.getOrDefault(entry, entry.defaultValue())));
      }
    }
    return snapshot;
  }

  public synchronized void applyServerSyncedValues(Map<String, String> serializedValues) {
    load();
    serverSyncedValues.clear();
    for (Entry<?> entry : entries) {
      if (entry.serverSynced() && serializedValues.containsKey(entry.key())) {
        serverSyncedValues.put(entry, KhazConfigHelpers.readValue(entry, serializedValues.get(entry.key())));
      }
    }
  }

  public synchronized void clearServerSyncedValuesAndReload() {
    serverSyncedValues.clear();
    reload();
  }

  private synchronized void write() {
    try {
      Files.createDirectories(file.getParent());
      Files.writeString(file, render(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      LOG.warn("Failed to write config file {}: {}", file, e.getMessage());
    }
  }

  private String render() {
    StringBuilder builder = new StringBuilder();
    builder.append("# ").append(KhazConfigHelpers.formatConfigTitle(modName, modId)).append('\n').append('\n');

    for (Entry<?> entry : entries) {
      if (!entry.comment().isBlank()) {
        for (String line : KhazConfigHelpers.splitCommentLines(entry.comment())) {
          builder.append("# ").append(line).append('\n');
        }
      }
      builder.append(entry.key())
          .append('=')
          .append(KhazConfigHelpers.formatValue(entry, values.getOrDefault(entry, entry.defaultValue())))
          .append('\n')
          .append('\n');
    }
    return builder.toString();
  }

  private void requireKnownEntry(Entry<?> entry) {
    Objects.requireNonNull(entry, "entry");
    for (Entry<?> knownEntry : entries) {
      if (knownEntry == entry) {
        return;
      }
    }
    throw new IllegalArgumentException("Config entry '" + entry.key() + "' is not registered to " + modId);
  }

  public interface ValueAdapter<T> {
    /**
     * Empty means the raw value is not valid for this entry.
     */
    Optional<T> parse(String raw);

    default T parse(String raw, T fallback) {
      return parse(raw).orElse(fallback);
    }

    String format(T value);

    default T normalize(T value, T fallback) {
      return parse(format(value), fallback);
    }
  }

  public record Entry<T>(String key, T defaultValue, String comment, ValueAdapter<T> adapter, boolean serverSynced) {
    public Entry(String key, T defaultValue, String comment, ValueAdapter<T> adapter) {
      this(key, defaultValue, comment, adapter, true);
    }

    public Entry {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(defaultValue, "defaultValue");
      Objects.requireNonNull(comment, "comment");
      Objects.requireNonNull(adapter, "adapter");
    }

    /**
     * Client-side options that should ignore server sync.
     */
    public Entry<T> localOnly() {
      return new Entry<>(key, defaultValue, comment, adapter, false);
    }
  }
}