package com.khazoda.core.config;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KhazConfigSyncNeoForge {
  private static final Map<Identifier, SyncRegistration> SYNC_REGISTRATIONS = new LinkedHashMap<>();
  private static boolean serverGameListenersRegistered;
  private static boolean clientGameListenersRegistered;

  private KhazConfigSyncNeoForge() {
  }

  public static void registerServerConfigSync(KhazConfig config, KhazConfigSync sync) {
    registerSyncRegistration(config, sync);
    registerServerLoginSyncListener();
    registerClientDisconnectReloadListener();
  }

  private static void registerSyncRegistration(KhazConfig config, KhazConfigSync sync) {
    Identifier payloadId = sync.payloadId();
    SyncRegistration registration = new SyncRegistration(config, sync);
    SyncRegistration previous = SYNC_REGISTRATIONS.putIfAbsent(payloadId, registration);
    if (previous != null && previous.config() != config) {
      throw new IllegalStateException("Duplicate config sync payload id registered: " + payloadId);
    }
  }

  private static void registerServerLoginSyncListener() {
    if (!serverGameListenersRegistered) {
      serverGameListenersRegistered = true;
      NeoForge.EVENT_BUS.addListener(KhazConfigSyncNeoForge::onPlayerLoggedIn);
    }
  }

  private static void registerClientDisconnectReloadListener() {
    if (!clientGameListenersRegistered && FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
      clientGameListenersRegistered = true;
      KhazConfigSyncClientNeoForge.registerDisconnectReloadListener();
    }
  }

  public static void registerPayloadHandlers(IEventBus modEventBus, KhazConfigSync sync) {
    modEventBus.addListener((RegisterPayloadHandlersEvent event) -> onRegisterPayloadHandlers(event, sync));
  }

  private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event, KhazConfigSync sync) {
    event.registrar("1").playToClient(sync.type(), sync.codec(), (payload, context) -> {
      SyncRegistration registration = SYNC_REGISTRATIONS.get(sync.payloadId());
      if (registration != null) {
        registration.config().applyServerSyncedValues(payload.serverValues());
      }
    });
  }

  private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) {
      for (SyncRegistration registration : SYNC_REGISTRATIONS.values()) {
        PacketDistributor.sendToPlayer(player, registration.sync().payload(registration.config().createServerSyncSnapshot()));
      }
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
