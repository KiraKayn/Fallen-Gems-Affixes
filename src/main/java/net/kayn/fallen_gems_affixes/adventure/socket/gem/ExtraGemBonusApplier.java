package net.kayn.fallen_gems_affixes.adventure.socket.gem;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExtraGemBonusApplier {

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(ExtraGemBonusRegistry.INSTANCE);
    }
}