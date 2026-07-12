package com.khazoda.core.config;

import com.khazoda.core.config.screen.KhazConfigScreen;
import com.khazoda.core.config.screen.KhazConfigScreens;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KhazConfigModMenuApi implements ModMenuApi {
  @Override
  public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
    Map<String, ConfigScreenFactory<?>> factories = new LinkedHashMap<>();
    for (KhazConfig config : KhazConfigScreens.registeredConfigs()) {
      factories.put(config.modId(), parent -> KhazConfigScreen.create(parent, config));
    }
    return factories;
  }
}