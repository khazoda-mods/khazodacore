package com.khazoda.core.config.screen;

import com.khazoda.core.config.KhazConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class KhazConfigScreen extends Screen {
  private static final int ROW_HEIGHT = 32;
  private static final int BUTTON_WIDTH = 98;
  private static final int TITLE_COLOR = 0xFFFFFFFF;

  private final Screen parent;
  private final KhazConfig config;
  private @Nullable KhazConfigList entries;
  private @Nullable Button doneButton;

  private KhazConfigScreen(Screen parent, KhazConfig config) {
    super(Component.translatableWithFallback("config." + config.modId() + ".title", config.modName() + " Config"));
    this.parent = parent;
    this.config = config;
  }

  public static Screen create(Screen parent, KhazConfig config) {
    return new KhazConfigScreen(parent, config);
  }

  @Override
  protected void init() {
    entries = new KhazConfigList(Minecraft.getInstance(), width, height - 64, 32, ROW_HEIGHT, config, this::updateDoneButton);
    addRenderableWidget(entries);

    doneButton = Button.builder(CommonComponents.GUI_DONE, button -> saveAndClose()).bounds(width / 2 - BUTTON_WIDTH - 4, height - 27, BUTTON_WIDTH, 20).build();
    addRenderableWidget(doneButton);
    addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> closeToParent()).bounds(width / 2 + 4, height - 27, BUTTON_WIDTH, 20).build());
    updateDoneButton();
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    graphics.centeredText(font, title, width / 2, 12, TITLE_COLOR);
  }

  @Override
  public void onClose() {
    closeToParent();
  }

  private void saveAndClose() {
    KhazConfigList currentEntries = entries;
    if (currentEntries == null || !currentEntries.isValid()) {
      updateDoneButton();
      return;
    }
    currentEntries.save();
    config.save();
    closeToParent();
  }

  private void updateDoneButton() {
    Button currentDoneButton = doneButton;
    KhazConfigList currentEntries = entries;
    if (currentDoneButton != null && currentEntries != null) {
      currentDoneButton.active = currentEntries.isValid();
    }
  }

  private void closeToParent() {
    Minecraft.getInstance().gui.setScreen(parent);
  }
}