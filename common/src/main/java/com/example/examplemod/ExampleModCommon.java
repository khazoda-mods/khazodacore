package com.example.examplemod;

import com.example.examplemod.platform.Services;
import com.khazoda.baseline.KhazConfig;

public final class ExampleModCommon {
  public static final KhazConfig CONFIG = KhazConfig.of(Constants.MOD_NAME, Constants.MOD_ID, Services.PLATFORM.getConfigDirectory());

  private ExampleModCommon() {
  }

  public static void init() {
    CONFIG.load();
    Services.PLATFORM.registerServerConfigSync(CONFIG);
  }
}
