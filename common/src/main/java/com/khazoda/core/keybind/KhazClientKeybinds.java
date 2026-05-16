package com.khazoda.core.keybind;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Common registry for client-side keybind descriptors.
 * Keybind descriptors are held here in common, and then registered separately in
 * Fabric and NeoForge mostly automatically from KhazKeybindFabric and KhazKeybindNeoForge.
 */
public final class KhazClientKeybinds {
  private static final Map<Identifier, KhazKeybind> KEYBINDS = new LinkedHashMap<>();

  private KhazClientKeybinds() {
  }

  /**
   * Register keybind descriptors from client setup before calling the loader helper.
   */
  public static void register(KhazKeybind... keybinds) {
    register(List.of(keybinds));
  }

  public static void register(Collection<KhazKeybind> keybinds) {
    Objects.requireNonNull(keybinds, "keybinds");
    for (KhazKeybind keybind : keybinds) {
      Objects.requireNonNull(keybind, "keybind");
      KhazKeybind existing = KEYBINDS.putIfAbsent(keybind.id(), keybind);
      if (existing != null && existing != keybind) {
        throw new IllegalArgumentException("Keybind '" + keybind.id() + "' is already registered.");
      }
    }
  }

  static Collection<KhazKeybind> registeredKeybinds() {
    return List.copyOf(KEYBINDS.values());
  }
}
