package com.khazoda.core.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ServerConfigSyncPayload(KhazConfigSync sync, Map<String, String> serverValues) implements CustomPacketPayload {
  private static final StreamCodec<ByteBuf, Map<String, String>> VALUES_CODEC =
      ByteBufCodecs.map(LinkedHashMap::new,
          ByteBufCodecs.STRING_UTF8, //keys
          ByteBufCodecs.STRING_UTF8); //values

  public ServerConfigSyncPayload {
    Objects.requireNonNull(sync, "sync");
    serverValues = Collections.unmodifiableMap(new LinkedHashMap<>(serverValues));
  }

  static ServerConfigSyncPayload read(KhazConfigSync sync, RegistryFriendlyByteBuf buffer) {
    return new ServerConfigSyncPayload(sync, VALUES_CODEC.decode(buffer));
  }

  void write(RegistryFriendlyByteBuf buffer) {
    VALUES_CODEC.encode(buffer, serverValues);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return sync.type();
  }
}
