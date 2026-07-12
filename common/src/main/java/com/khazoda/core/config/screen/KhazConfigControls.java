package com.khazoda.core.config.screen;

import com.khazoda.core.config.KhazConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class KhazConfigControls {
  private static final int VALID_TEXT_COLOR = 0xFFE0E0E0;
  private static final int INVALID_TEXT_COLOR = 0xFFFF5555;

  private KhazConfigControls() {
  }

  static <T> KhazConfigControl create(KhazConfig config, KhazConfig.Entry<T> entry, Component label, int width, boolean editable, Runnable validityChanged) {
    T value = editable ? config.getLocal(entry) : config.get(entry);
    if (entry.defaultValue() instanceof Boolean) {
      return booleanControl(config, booleanEntry(entry), Boolean.TRUE.equals(value), label, width, editable);
    }
    if (entry.defaultValue() instanceof Enum<?>) {
      return enumControl(config, entry, label, width, editable);
    }
    return textControl(config, entry, value, label, width, editable, validityChanged);
  }

  private static KhazConfigControl booleanControl(KhazConfig config, KhazConfig.Entry<Boolean> entry, boolean value, Component label, int width, boolean editable) {
    CycleButton<Boolean> button = CycleButton.onOffBuilder(value).displayOnlyValue().create(0, 0, width, 20, label);
    setCommonWidgetState(button, KhazConfigText.tooltip(config, entry, editable), editable);
    return new CycleControl<>(button, entry::defaultValue, option -> config.setLocal(entry, option));
  }

  @SuppressWarnings("unchecked")
  private static <E extends Enum<E>> KhazConfigControl enumControl(KhazConfig config, KhazConfig.Entry<?> entry, Component label, int width, boolean editable) {
    KhazConfig.Entry<E> enumEntry = (KhazConfig.Entry<E>) entry;
    E defaultValue = enumEntry.defaultValue();
    E value = editable ? config.getLocal(enumEntry) : config.get(enumEntry);
    CycleButton<E> button = CycleButton.builder(option -> KhazConfigText.enumValueLabel(config, entry, option), value).withValues(defaultValue.getDeclaringClass().getEnumConstants()).displayOnlyValue().create(0, 0, width, 20, label);
    setCommonWidgetState(button, KhazConfigText.tooltip(config, entry, editable), editable);
    return new CycleControl<>(button, enumEntry::defaultValue, option -> config.setLocal(enumEntry, option));
  }

  private static <T> KhazConfigControl textControl(KhazConfig config, KhazConfig.Entry<T> entry, T value, Component label, int width, boolean editable, Runnable validityChanged) {
    EditBox editBox = new EditBox(Minecraft.getInstance().font, 0, 0, width, 20, label);
    TextControl<T> control = new TextControl<>(config, entry, editBox, validityChanged);
    editBox.setMaxLength(Integer.MAX_VALUE);
    editBox.setValue(entry.adapter().format(value));
    editBox.setEditable(editable);
    editBox.active = editable;
    setTooltip(editBox, KhazConfigText.tooltip(config, entry, editable));
    editBox.setResponder(control::updateValue);
    control.updateValue(editBox.getValue());
    return control;
  }

  private static void setCommonWidgetState(AbstractWidget widget, @Nullable Component tooltip, boolean editable) {
    widget.active = editable;
    setTooltip(widget, tooltip);
  }

  private static void setTooltip(AbstractWidget widget, @Nullable Component tooltip) {
    widget.setTooltip(tooltip == null ? null : Tooltip.create(tooltip));
  }

  @SuppressWarnings("unchecked")
  private static KhazConfig.Entry<Boolean> booleanEntry(KhazConfig.Entry<?> entry) {
    return (KhazConfig.Entry<Boolean>) entry;
  }

  private record CycleControl<T>(CycleButton<T> widget, Supplier<T> defaultValue,
                                 Consumer<T> saver) implements KhazConfigControl {
    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public void save() {
      saver.accept(widget.getValue());
    }

    @Override
    public void reset() {
      widget.setValue(defaultValue.get());
    }
  }

  private static final class TextControl<T> implements KhazConfigControl {
    private final KhazConfig config;
    private final KhazConfig.Entry<T> entry;
    private final EditBox widget;
    private final Runnable validityChanged;
    private @Nullable T value;

    private TextControl(KhazConfig config, KhazConfig.Entry<T> entry, EditBox widget, Runnable validityChanged) {
      this.config = config;
      this.entry = entry;
      this.widget = widget;
      this.validityChanged = validityChanged;
    }

    @Override
    public EditBox widget() {
      return widget;
    }

    @Override
    public boolean isValid() {
      return value != null;
    }

    @Override
    public void save() {
      if (value != null) {
        config.setLocal(entry, value);
      }
    }

    @Override
    public void reset() {
      widget.setValue(entry.adapter().format(entry.defaultValue()));
    }

    private void updateValue(String raw) {
      boolean wasValid = isValid();
      value = entry.adapter().parse(raw).orElse(null);
      boolean valid = isValid();
      widget.setTextColor(valid ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
      if (wasValid != valid) {
        validityChanged.run();
      }
    }
  }
}