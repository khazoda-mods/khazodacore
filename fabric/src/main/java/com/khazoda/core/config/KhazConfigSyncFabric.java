package com.khazoda.core.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class KhazConfigSyncFabric {
  private static final Map<Identifier, SyncRegistration> SYNC_REGISTRATIONS = new LinkedHashMap<>();
  private static final Set<Identifier> REGISTERED_PAYLOAD_TYPES = new LinkedHashSet<>();
  private static boolean serverGameListenersRegistered;

  private KhazConfigSyncFabric() {
  }

  public static void registerServerConfigSync(KhazConfig config, KhazConfigSync sync) {
    registerSyncRegistration(config, sync);
    registerClientboundPayloadType(sync);
    registerClientSyncReceiver(config, sync);
    registerServerJoinSyncListener();
  }

  private static void registerSyncRegistration(KhazConfig config, KhazConfigSync sync) {
    Identifier payloadId = sync.payloadId();
    SyncRegistration registration = new SyncRegistration(config, sync);
    SyncRegistration previous = SYNC_REGISTRATIONS.putIfAbsent(payloadId, registration);
    if (previous != null && previous.config() != config) {
      throw new IllegalStateException("Duplicate config sync payload id registered: " + payloadId);
    }
  }

  private static void registerServerJoinSyncListener() {
    if (!serverGameListenersRegistered) {
      serverGameListenersRegistered = true;
      ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
        for (SyncRegistration registration : SYNC_REGISTRATIONS.values()) {
          if (ServerPlayNetworking.canSend(listener, registration.sync().type())) {
            sender.sendPacket(registration.sync().payload(registration.config().createServerSyncSnapshot()));
          }
        }
      });
    }
  }

  public static void registerClientboundPayloadType(KhazConfigSync sync) {
    if (REGISTERED_PAYLOAD_TYPES.add(sync.payloadId())) {
      PayloadTypeRegistry.clientboundPlay().register(sync.type(), sync.codec());
    }
  }

  private static void registerClientSyncReceiver(KhazConfig config, KhazConfigSync sync) {
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      KhazConfigSyncClientFabric.register(config, sync);
    }
  }

  static void clearServerSyncedValuesAndReload() {
    for (SyncRegistration registration : SYNC_REGISTRATIONS.values()) {
      registration.config().clearServerSyncedValuesAndReload();
    }
  }

  private record SyncRegistration(KhazConfig config, KhazConfigSync sync) {
  }
}