package net.kayn.fallen_gems_affixes.client;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.color.FabledColor;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ColorClient {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? FabledColor.FABLED.getValue() : -1,
                ModItems.FABLED_MATERIAL.get()
        );
    }
}