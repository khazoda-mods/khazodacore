package com.example.examplemod.registry;

import com.example.examplemod.Constants;
import com.khazoda.baseline.KhazReg;

public final class MainRegistry {
  public static final KhazReg reg = new KhazReg(Constants.MOD_ID);
  private static boolean initialized;

  private MainRegistry() {
  }

  public static KhazReg init() {
    if (initialized) return reg;
    initialized = true;

    // Define registrations as static fields in this class,
    // or complete all registration setup here and seal it with reg.freeze() at the end.

    /*
     Register in dedicated classes and initialize them:
     ModItems.init();
     ModBlocks.init();
     ModSounds.init();

     Register through explicit feature/bootstrap methods:
     MetalsFeature.register(reg);
     MachinesFeature.register(reg);
     WorldgenFeature.register(reg);

     Do conditional registration:
     if (Services.PLATFORM.isModLoaded("other-mod-id")) {
       CompatContent.register(reg);
     }
     if (MyConfig.enableExtraContent()) {
       ExtraContent.init();
     }

     Create non-static registrations and register them:
     var items = new ModItems(reg);
     var blocks = new ModBlocks(reg);
     items.register();
     blocks.register();

     Avoid late or implicit registration:
     - do not register from Fabric/NeoForge registry events
     - do not register when gameplay code first accesses a class
     - do not rely on accidental classloading
    */

    reg.freeze();
    return reg;
  }
}