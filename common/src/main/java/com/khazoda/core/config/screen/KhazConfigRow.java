package com.khazoda.core.config.screen;

import com.khazoda.core.Constants;
import com.khazoda.core.config.KhazConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

import static com.khazoda.core.Constants.ID;

final class KhazConfigRow extends ContainerObjectSelectionList.Entry<KhazConfigRow> {
  private static final int CONTROL_WIDTH = 100;
  private static final int RESET_WIDTH = 20;
  private static final int LABEL_COLOR = 0xFFFFFFFF;
  private static final int DISABLED_LABEL_COLOR = 0xFFA0A0A0;

  private final Component label;
  private final KhazConfigControl control;
  private final Button resetButton;
  private final boolean editable;

  KhazConfigRow(KhazConfig config, KhazConfig.Entry<?> entry, Runnable validityChanged) {
    this.editable = !config.hasServerSyncedValue(entry);
    this.label = KhazConfigText.label(config, entry);
    this.control = KhazConfigControls.create(config, entry, label, CONTROL_WIDTH, editable, validityChanged);
    this.resetButton = SpriteIconButton.builder(Component.literal("⟳"), button -> reset(), true)
        .width(RESET_WIDTH)
        .sprite(ID(Constants.MOD_ID, "reset"), 16, 16)
        .build();
    this.resetButton.setTooltip(Tooltip.create(KhazConfigText.resetTooltip()));
    this.resetButton.active = editable;
  }

  @Override
  public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
    int labelX = getContentX();
    int labelY = getContentY() + 10;
    int controlY = getContentY() + 6;
    int resetX = getContentRight() - RESET_WIDTH - 8;
    int controlX = resetX - CONTROL_WIDTH - 6;

    control.widget().setX(controlX);
    control.widget().setY(controlY);
    resetButton.setX(resetX);
    resetButton.setY(controlY);

    graphics.enableScissor(labelX, getContentY(), controlX - 6, getContentBottom());
    graphics.text(Minecraft.getInstance().font, label, labelX, labelY, editable ? LABEL_COLOR : DISABLED_LABEL_COLOR);
    graphics.disableScissor();
    control.widget().extractRenderState(graphics, mouseX, mouseY, partialTick);
    resetButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public List<? extends NarratableEntry> narratables() {
    return List.of(control.widget(), resetButton);
  }

  @Override
  public List<? extends GuiEventListener> children() {
    return List.of(control.widget(), resetButton);
  }

  @Override
  public void visitWidgets(Consumer<AbstractWidget> consumer) {
    consumer.accept(control.widget());
    consumer.accept(resetButton);
  }

  boolean isValid() {
    return !editable || control.isValid();
  }

  void save() {
    if (editable && control.isValid()) {
      control.save();
    }
  }

  private void reset() {
    control.reset();
  }
}