package com.khazoda.core.config.screen;

import com.khazoda.core.config.KhazConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

final class KhazConfigList extends ContainerObjectSelectionList<KhazConfigRow> {
  private static final int MAX_ROW_WIDTH = 500;

  KhazConfigList(Minecraft minecraft, int width, int height, int y, int itemHeight, KhazConfig config, Runnable validityChanged) {
    super(minecraft, width, height, y, itemHeight);
    config.entries().forEach(entry -> addEntry(new KhazConfigRow(config, entry, validityChanged)));
  }

  @Override
  public int getRowWidth() {
    return Math.min(MAX_ROW_WIDTH, getWidth() - 20);
  }

  void save() {
    for (KhazConfigRow row : children()) {
      row.save();
    }
  }

  boolean isValid() {
    for (KhazConfigRow row : children()) {
      if (!row.isValid()) {
        return false;
      }
    }
    return true;
  }
}