package com.example.examplemod;

import com.khazoda.baseline.KhazConfigSync;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

  public static final String MOD_ID = "examplemod";
  public static final String MOD_NAME = "ExampleMod";
  public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
  public static final KhazConfigSync CONFIG_SYNC = KhazConfigSync.create(ID("config_sync"));

  public static Identifier ID(String path) {
    return Identifier.fromNamespaceAndPath(MOD_ID, path);
  }
}