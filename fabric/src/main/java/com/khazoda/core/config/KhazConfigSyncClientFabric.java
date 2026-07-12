package com.khazoda.core.config;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

final class KhazConfigSyncClientFabric {
  private static final Set<Identifier> REGISTERED_RECEIVERS = new LinkedHashSet<>();
  private static boolean disconnectListenerRegistered;

  private KhazConfigSyncClientFabric() {
  }

  static void register(KhazConfig config, KhazConfigSync sync) {
    if (REGISTERED_RECEIVERS.add(sync.payloadId())) {
      ClientPlayNetworking.registerGlobalReceiver(sync.type(), (payload, context) -> config.applyServerSyncedValues(payload.serverValues()));
    }
    registerDisconnectListener();
  }

  private static void registerDisconnectListener() {
    if (!disconnectListenerRegistered) {
      disconnectListenerRegistered = true;
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> KhazConfigSyncFabric.clearServerSyncedValuesAndReload());
    }
  }
}