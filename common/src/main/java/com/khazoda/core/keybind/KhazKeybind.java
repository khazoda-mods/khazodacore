package com.khazoda.core.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Common descriptor for a client-side Minecraft keybind. Supports pressed, held and held-in-GUI states.
 * Should work for all standard keyboard keys as well as mouse buttons
 */
public final class KhazKeybind {
  private final Identifier id;
  private final Identifier category;
  private final InputConstants.Key defaultKey;
  private final Runnable onPress;
  private final Runnable onHold;

  // Loader helpers replace these once Minecraft owns the live KeyMapping.
  private BooleanSupplier boundInputHeld = () -> false;
  private BooleanSupplier boundInput;
  private Supplier<Component> boundInputLabel;

  private KhazKeybind(Builder builder) {
    this.id = builder.id;
    this.category = builder.category;
    this.defaultKey = builder.defaultKey;
    this.onPress = builder.onPress;
    this.onHold = builder.onHold;
    this.boundInput = () -> this.defaultKey != InputConstants.UNKNOWN;
    this.boundInputLabel = this.defaultKey::getDisplayName;
  }

  public static Builder builder(Identifier id) {
    return new Builder(id);
  }

  public Identifier id() {
    return id;
  }

  public Identifier category() {
    return category;
  }

  /**
   * Returns true while the input is held, including inside UI screens. Best for tooltip/gui item controls
   */
  public boolean isBoundInputHeldInUi() {
    return isBoundInputHeld();
  }

  /**
   * Returns false when a keybind is unbound by default or by the player
   */
  public boolean hasBoundInput() {
    return boundInput.getAsBoolean();
  }

  /**
   * Returns Minecraft's current display label for the bound input. Good for tooltips to show user a dynamic input
   */
  public Component boundInputLabel() {
    return boundInputLabel.get();
  }

  String translationKey() {
    return id.toLanguageKey("key");
  }

  InputConstants.Key defaultKey() {
    return defaultKey;
  }

  Runnable onPress() {
    return onPress;
  }

  Runnable onHold() {
    return onHold;
  }

  boolean hasCallbacks() {
    return onPress != null || onHold != null;
  }

  boolean isBoundInputHeld() {
    return boundInputHeld.getAsBoolean();
  }

  void setBoundInputHeldSupplier(BooleanSupplier boundInputHeld) {
    this.boundInputHeld = Objects.requireNonNull(boundInputHeld, "boundInputHeld");
  }

  void setBoundInputSupplier(BooleanSupplier boundInput) {
    this.boundInput = Objects.requireNonNull(boundInput, "boundInput");
  }

  void setBoundInputLabelSupplier(Supplier<Component> boundInputLabel) {
    this.boundInputLabel = Objects.requireNonNull(boundInputLabel, "boundInputLabel");
  }

  public static final class Builder {
    private final Identifier id;
    private Identifier category;
    private InputConstants.Key defaultKey = InputConstants.UNKNOWN;
    private Runnable onPress;
    private Runnable onHold;

    private Builder(Identifier id) {
      this.id = Objects.requireNonNull(id, "id");
      this.category = Identifier.fromNamespaceAndPath(id.getNamespace(), "main");
    }

    public Builder category(Identifier category) {
      this.category = Objects.requireNonNull(category, "category");
      return this;
    }

    public Builder defaultKey(int keyCode) {
      return defaultKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
    }

    public Builder defaultScanCode(int scanCode) {
      return defaultKey(InputConstants.Type.SCANCODE.getOrCreate(scanCode));
    }

    public Builder defaultMouse(int mouseButton) {
      return defaultKey(InputConstants.Type.MOUSE.getOrCreate(mouseButton));
    }

    public Builder defaultKey(InputConstants.Key defaultKey) {
      this.defaultKey = Objects.requireNonNull(defaultKey, "defaultKey");
      return this;
    }

    public Builder onPress(Runnable onPress) {
      this.onPress = Objects.requireNonNull(onPress, "onPress");
      return this;
    }

    public Builder onHold(Runnable onHold) {
      this.onHold = Objects.requireNonNull(onHold, "onHold");
      return this;
    }

    public KhazKeybind build() {
      return new KhazKeybind(this);
    }
  }
}
