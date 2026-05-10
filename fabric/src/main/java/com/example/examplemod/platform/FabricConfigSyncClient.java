package com.example.examplemod.platform;

import com.example.examplemod.ExampleModCommon;
import com.example.examplemod.Constants;
import com.khazoda.baseline.KhazConfigSyncFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricConfigSyncClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    KhazConfigSyncFabric.registerClientboundPayloadType(Constants.CONFIG_SYNC);
    registerServerConfigReceiver();
    registerDisconnectReloadListener();
  }

  private static void registerServerConfigReceiver() {
    ClientPlayNetworking.registerGlobalReceiver(Constants.CONFIG_SYNC.type(), (payload, context) -> ExampleModCommon.CONFIG.applyServerSyncedValues(payload.serverValues()));
  }

  private static void registerDisconnectReloadListener() {
    ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> ExampleModCommon.CONFIG.clearServerSyncedValuesAndReload());
  }
}
