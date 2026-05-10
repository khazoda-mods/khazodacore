package com.khazoda.baseline;

import java.util.function.Supplier;

/**
 * Fabric entrypoint helper for the {@link KhazReg} registry system.
 */
public final class KhazRegFabric {
  private KhazRegFabric() {
  }

  /**
   * Call KhazRegFabric.init(MainRegistry::init) in your Fabric mod constructor's onInitialize() method.
   */
  public static void init(Supplier<KhazReg> registryBootstrap) {
    registryBootstrap.get().registerAllStatic();
  }
}
