package com.example.examplemod.platform;

import com.example.examplemod.Constants;
import com.example.examplemod.platform.services.IPlatformHelper;
import com.khazoda.baseline.KhazConfigSyncFabric;
import com.khazoda.baseline.KhazConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
  @Override
  public String getPlatformName() {
    return "Fabric";
  }

  @Override
  public boolean isModLoaded(String modId) {
    return FabricLoader.getInstance().isModLoaded(modId);
  }

  @Override
  public boolean isDevelopmentEnvironment() {
    return FabricLoader.getInstance().isDevelopmentEnvironment();
  }

  @Override
  public Path getConfigDirectory() {
    return FabricLoader.getInstance().getConfigDir();
  }

  @Override
  public void registerServerConfigSync(KhazConfig config) {
    KhazConfigSyncFabric.registerServerConfigSync(config, Constants.CONFIG_SYNC);
  }
}
