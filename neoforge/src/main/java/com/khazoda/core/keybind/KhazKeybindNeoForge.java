package com.khazoda.core.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * NeoForge-side client helper for registering {@link KhazKeybind} controls held in {@link KhazClientKeybinds}.
 */
public final class KhazKeybindNeoForge {
  private static final Map<Identifier, RegisteredKeybind> REGISTERED_KEYBINDS = new LinkedHashMap<>();
  private static final Map<Identifier, KeyMapping.Category> CATEGORIES = new LinkedHashMap<>();
  private static final Set<Identifier> PENDING_KEYBIND_IDS = new LinkedHashSet<>();
  private static boolean clientTickRegistered;
  private static boolean keyMappingsRegistered;

  private KhazKeybindNeoForge() {
  }

  /**
   * Create Minecraft key mappings for all descriptors registered in {@link KhazClientKeybinds}.
   */
  public static void init(IEventBus modEventBus) {
    Objects.requireNonNull(modEventBus, "modEventBus");
    List<KhazKeybind> keybindList = new ArrayList<>();
    for (KhazKeybind keybind : KhazClientKeybinds.registeredKeybinds()) {
      if (PENDING_KEYBIND_IDS.contains(keybind.id()) || REGISTERED_KEYBINDS.containsKey(keybind.id())) {
        continue;
      }
      keybindList.add(keybind);
    }
    if (keybindList.isEmpty()) {
      return;
    }
    if (keyMappingsRegistered) {
      throw new IllegalStateException("KhazKeybindNeoForge.init must be called before RegisterKeyMappingsEvent fires.");
    }
    for (KhazKeybind keybind : keybindList) {
      PENDING_KEYBIND_IDS.add(keybind.id());
    }
    modEventBus.addListener((RegisterKeyMappingsEvent event) -> registerKeyMappings(event, keybindList));
    registerClientTickListenerIfNeeded(keybindList);
  }

  private static void registerKeyMappings(RegisterKeyMappingsEvent event, Collection<KhazKeybind> keybinds) {
    keyMappingsRegistered = true;
    for (KhazKeybind keybind : keybinds) {
      KeyMapping.Category category = CATEGORIES.computeIfAbsent(keybind.category(), id -> registerCategory(event, id));
      KeyMapping keyMapping = createKeyMapping(keybind, category);
      event.register(keyMapping);
      registerKeybind(keybind, keyMapping);
    }
  }

  private static void registerKeybind(KhazKeybind keybind, KeyMapping keyMapping) {
    keybind.setBoundInputSupplier(() -> !keyMapping.isUnbound());
    keybind.setBoundInputLabelSupplier(keyMapping::getTranslatedKeyMessage);
    REGISTERED_KEYBINDS.put(keybind.id(), new RegisteredKeybind(keybind, boundInputHeldInUiSupplier(keyMapping)));
  }

  private static KeyMapping.Category registerCategory(RegisterKeyMappingsEvent event, Identifier id) {
    KeyMapping.Category category = new KeyMapping.Category(id);
    try {
      event.registerCategory(category);
    } catch (IllegalArgumentException ignored) {
      // If category already exists, add to it instead of making a new one. (Category is a Record so this uses existing)
      return new KeyMapping.Category(id);
    }
    return category;
  }

  private static KeyMapping createKeyMapping(KhazKeybind keybind, KeyMapping.Category category) {
    return new KeyMapping(
        keybind.translationKey(),
        keybind.defaultKey().getType(),
        keybind.defaultKey().getValue(),
        category
    );
  }

  private static BooleanSupplier boundInputHeldInUiSupplier(KeyMapping keyMapping) {
    return () -> keyModifierActive(keyMapping) && UiKeybindInput.isBoundInputHeld(
        keyMapping.getKey(),
        keyCode -> InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyCode),
        mouseButton -> GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), mouseButton) == InputConstants.PRESS
    );
  }

  private static boolean keyModifierActive(KeyMapping keyMapping) {
    KeyModifier modifier = keyMapping.getKeyModifier();
    return modifier == KeyModifier.NONE || modifier.isActive(keyMapping.getKeyConflictContext());
  }

  private static void registerClientTickListenerIfNeeded(Collection<KhazKeybind> keybinds) {
    if (clientTickRegistered || keybinds.stream().noneMatch(KhazKeybind::hasCallbacks)) {
      return;
    }
    clientTickRegistered = true;
    NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> tickKeybinds());
  }

  private static void tickKeybinds() {
    for (RegisteredKeybind registered : REGISTERED_KEYBINDS.values()) {
      registered.tick();
    }
  }
}