package com.khazoda.core;

import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

final class NeoForgeKhazConfigSyncClient {
  private NeoForgeKhazConfigSyncClient() {
  }

  static void registerDisconnectReloadListener() {
    NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> KhazConfigSyncNeoForge.clearServerSyncedValuesAndReload());
  }
}
