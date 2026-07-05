package com.khazoda.core;

import net.minecraft.resources.Identifier;

public final class Constants {
  public static final String MOD_ID = "khazodacore";
  public static final String MOD_NAME = "KhazodaCore";

  private Constants() {}

  public static Identifier ID(String namespace, String path) {
    return Identifier.fromNamespaceAndPath(namespace, path);
  }
}