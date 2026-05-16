package com.khazoda.core;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.function.IntPredicate;

final class UiKeybindInput {
  private UiKeybindInput() {
  }

  static boolean isBoundInputHeld(
      InputConstants.Key key,
      IntPredicate keyDown,
      IntPredicate mouseDown
  ) {
    if (key == InputConstants.UNKNOWN) {
      return false;
    }
    return switch (key.getType()) {
      case KEYSYM -> keyDown.test(key.getValue());
      case MOUSE -> mouseDown.test(key.getValue());
      case SCANCODE -> false;
    };
  }
}
