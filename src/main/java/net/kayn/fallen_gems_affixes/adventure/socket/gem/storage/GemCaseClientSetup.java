package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GemCaseClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(GemCaseRegistry.GEM_CASE_MENU.get(), GemCaseScreen::new);

            BlockEntityRenderers.register(GemCaseRegistry.GEM_CASE_TILE.get(), ctx -> new GemCaseTileRenderer());
            BlockEntityRenderers.register(GemCaseRegistry.ENDER_GEM_CASE_TILE.get(), ctx -> new GemCaseTileRenderer());
        });
    }
}

