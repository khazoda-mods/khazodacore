package com.khazoda.core.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Fabric-side client helper for registering {@link KhazKeybind} controls held in {@link KhazClientKeybinds}.
 */
public final class KhazKeybindFabric {
  private static final Map<Identifier, RegisteredKeybind> REGISTERED_KEYBINDS = new LinkedHashMap<>();
  private static final Map<Identifier, KeyMapping.Category> CATEGORIES = new LinkedHashMap<>();
  private static boolean clientTickRegistered;

  private KhazKeybindFabric() {
  }

  /**
   * Create Minecraft key mappings for all descriptors registered in {@link KhazClientKeybinds}.
   */
  public static void init() {
    register(KhazClientKeybinds.registeredKeybinds());
  }

  private static void register(Collection<KhazKeybind> keybinds) {
    boolean registeredAnyCallbackKeybind = false;
    for (KhazKeybind keybind : keybinds) {
      if (REGISTERED_KEYBINDS.containsKey(keybind.id())) {
        continue;
      }
      KeyMapping keyMapping = KeyMappingHelper.registerKeyMapping(createKeyMapping(keybind));
      registerKeybind(keybind, keyMapping);
      registeredAnyCallbackKeybind = registeredAnyCallbackKeybind || keybind.hasCallbacks();
    }
    registerClientTickListenerIfNeeded(registeredAnyCallbackKeybind);
  }

  private static void registerKeybind(KhazKeybind keybind, KeyMapping keyMapping) {
    keybind.setBoundInputSupplier(() -> !keyMapping.isUnbound());
    keybind.setBoundInputLabelSupplier(keyMapping::getTranslatedKeyMessage);
    REGISTERED_KEYBINDS.put(keybind.id(), new RegisteredKeybind(keybind, boundInputHeldInUiSupplier(keyMapping)));
  }

  private static KeyMapping createKeyMapping(KhazKeybind keybind) {
    return new KeyMapping(
        keybind.translationKey(),
        keybind.defaultKey().getType(),
        keybind.defaultKey().getValue(),
        keyMappingCategory(keybind.category())
    );
  }

  private static BooleanSupplier boundInputHeldInUiSupplier(KeyMapping keyMapping) {
    return () -> keyMapping.isDown() || UiKeybindInput.isBoundInputHeld(
        currentKey(keyMapping),
        keyCode -> InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyCode),
        mouseButton -> GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), mouseButton) == InputConstants.PRESS
    );
  }

  private static InputConstants.Key currentKey(KeyMapping keyMapping) {
    return InputConstants.getKey(keyMapping.saveString());
  }

  private static KeyMapping.Category keyMappingCategory(Identifier id) {
    return CATEGORIES.computeIfAbsent(id, KhazKeybindFabric::registerCategory);
  }

  private static KeyMapping.Category registerCategory(Identifier id) {
    try {
      return KeyMapping.Category.register(id);
    } catch (IllegalArgumentException ignored) {
      // If category already exists, add to it instead of making a new one. (Category is a Record so this uses existing)
      return new KeyMapping.Category(id);
    }
  }

  private static void registerClientTickListenerIfNeeded(boolean registeredAnyCallbackKeybind) {
    if (clientTickRegistered || !registeredAnyCallbackKeybind) {
      return;
    }
    clientTickRegistered = true;
    ClientTickEvents.END_CLIENT_TICK.register(client -> tickKeybinds());
  }

  private static void tickKeybinds() {
    for (RegisteredKeybind registered : REGISTERED_KEYBINDS.values()) {
      registered.tick();
    }
  }
}
