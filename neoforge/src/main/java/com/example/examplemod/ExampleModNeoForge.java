package com.example.examplemod;

import com.example.examplemod.registry.MainRegistry;
import com.khazoda.baseline.KhazRegNeoForge;
import com.khazoda.baseline.KhazConfigSyncNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ExampleModNeoForge {

  public ExampleModNeoForge(IEventBus eventBus) {
    ExampleModCommon.init();
    KhazConfigSyncNeoForge.registerPayloadHandlers(eventBus, Constants.CONFIG_SYNC);
    KhazRegNeoForge.init(eventBus, MainRegistry::init);
  }
}
