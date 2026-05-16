package com.khazoda.core.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ServerConfigSyncPayload(KhazConfigSync sync, Map<String, String> serverValues) implements CustomPacketPayload {
  public ServerConfigSyncPayload {
    sync = Objects.requireNonNull(sync, "sync");
    serverValues = Collections.unmodifiableMap(new LinkedHashMap<>(serverValues));
  }

  static ServerConfigSyncPayload read(KhazConfigSync sync, RegistryFriendlyByteBuf buffer) {
    return new ServerConfigSyncPayload(sync, buffer.readMap(LinkedHashMap::new, input -> input.readUtf(), input -> input.readUtf()));
  }

  void write(RegistryFriendlyByteBuf buffer) {
    buffer.writeMap(serverValues, (output, value) -> output.writeUtf(value), (output, value) -> output.writeUtf(value));
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return sync.type();
  }
}
