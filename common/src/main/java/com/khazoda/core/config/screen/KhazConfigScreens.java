package com.khazoda.core.config.screen;

import com.khazoda.core.config.KhazConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class KhazConfigScreens {
  private static final Map<String, KhazConfig> CONFIGS = new LinkedHashMap<>();
  private static final List<Consumer<KhazConfig>> LISTENERS = new ArrayList<>();

  private KhazConfigScreens() {
  }

  public static void register(KhazConfig config) {
    List<Consumer<KhazConfig>> listeners;
    synchronized (KhazConfigScreens.class) {
      KhazConfig previous = CONFIGS.putIfAbsent(config.modId(), config);
      if (previous != null && previous != config) {
        throw new IllegalStateException("Duplicate config screen registered for mod id '" + config.modId() + "'.");
      }
      if (previous != null) {
        return;
      }
      listeners = List.copyOf(LISTENERS);
    }
    listeners.forEach(listener -> listener.accept(config));
  }

  public static List<KhazConfig> registeredConfigs() {
    synchronized (KhazConfigScreens.class) {
      return List.copyOf(CONFIGS.values());
    }
  }

  public static void addRegistrationListener(Consumer<KhazConfig> listener) {
    List<KhazConfig> configs;
    synchronized (KhazConfigScreens.class) {
      LISTENERS.add(listener);
      configs = List.copyOf(CONFIGS.values());
    }
    configs.forEach(listener);
  }
}