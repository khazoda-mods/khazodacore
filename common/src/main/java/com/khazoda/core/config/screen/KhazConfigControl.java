package com.khazoda.core.config.screen;

import net.minecraft.client.gui.components.AbstractWidget;

interface KhazConfigControl {
  AbstractWidget widget();

  boolean isValid();

  void save();

  void reset();
}