package com.khazoda.core.reg;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

/**
 * NeoForge entrypoint helper for the {@link KhazReg} registry system.
 */
public final class KhazRegNeoForge {
  private KhazRegNeoForge() {
  }

  /**
   * Call KhazRegNeoForge.init(eventBus, MainRegistry::init) in your NeoForge mod constructor.
   */
  public static void init(IEventBus eventBus, Supplier<KhazReg> registryBootstrap) {
    KhazReg reg = registryBootstrap.get();
    eventBus.addListener((RegisterEvent event) -> registerRegistries(reg, event));
    eventBus.addListener((FMLCommonSetupEvent event) -> verifyRegistriesRegistered(reg, event));
  }

  private static void registerRegistries(KhazReg reg, RegisterEvent event) {
    reg.registerNeoForge(event.getRegistry());
  }

  private static void verifyRegistriesRegistered(KhazReg reg, FMLCommonSetupEvent event) {
    reg.verifyAllStaticRegistrations();
  }
}
