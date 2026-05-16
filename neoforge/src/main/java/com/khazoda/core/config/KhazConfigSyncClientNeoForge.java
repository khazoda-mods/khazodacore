package com.khazoda.core.config;

import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

final class KhazConfigSyncClientNeoForge {
  private KhazConfigSyncClientNeoForge() {
  }

  static void registerDisconnectReloadListener() {
    NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> KhazConfigSyncNeoForge.clearServerSyncedValuesAndReload());
  }
}
