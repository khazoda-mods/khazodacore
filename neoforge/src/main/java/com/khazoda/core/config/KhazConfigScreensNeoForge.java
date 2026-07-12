package com.khazoda.core.config;

import com.khazoda.core.Constants;
import com.khazoda.core.config.screen.KhazConfigScreen;
import com.khazoda.core.config.screen.KhazConfigScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class KhazConfigScreensNeoForge {
  private KhazConfigScreensNeoForge() {
  }

  @SubscribeEvent
  private static void onClientSetup(FMLClientSetupEvent event) {
    KhazConfigScreens.addRegistrationListener(KhazConfigScreensNeoForge::register);
  }

  private static void register(KhazConfig config) {
    ModList.get().getModContainerById(config.modId())
        .filter(container -> container.getCustomExtension(IConfigScreenFactory.class).isEmpty())
        .ifPresent(container -> container.registerExtensionPoint(
            IConfigScreenFactory.class,
            (modContainer, parent) -> KhazConfigScreen.create(parent, config)
        ));
  }
}