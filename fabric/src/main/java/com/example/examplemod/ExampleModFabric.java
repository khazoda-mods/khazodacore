package com.example.examplemod;

import com.example.examplemod.registry.MainRegistry;
import com.khazoda.baseline.KhazRegFabric;
import net.fabricmc.api.ModInitializer;

public class ExampleModFabric implements ModInitializer {

  @Override
  public void onInitialize() {
    ExampleModCommon.init();
    KhazRegFabric.init(MainRegistry::init);
  }
}
