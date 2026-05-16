package com.khazoda.core.keybind;

import java.util.function.BooleanSupplier;

final class RegisteredKeybind {
  private final KhazKeybind keybind;
  private boolean wasDown;

  RegisteredKeybind(KhazKeybind keybind, BooleanSupplier boundInputHeld) {
    this.keybind = keybind;
    keybind.setBoundInputHeldSupplier(boundInputHeld);
  }

  void tick() {
    boolean down = keybind.isBoundInputHeld();
    Runnable onPress = keybind.onPress();
    if (onPress != null && down && !wasDown) {
      onPress.run();
    }
    Runnable onHold = keybind.onHold();
    if (onHold != null && down) {
      onHold.run();
    }
    wasDown = down;
  }
}
