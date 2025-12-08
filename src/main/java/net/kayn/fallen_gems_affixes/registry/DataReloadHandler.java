package net.kayn.fallen_gems_affixes.registry;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DataReloadHandler {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new AugmentItem.Loader());
        FallenGemsAffixes.LOGGER.info("Registered Augment JSON loader!");
    }
}