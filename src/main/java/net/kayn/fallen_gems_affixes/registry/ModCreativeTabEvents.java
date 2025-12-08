package net.kayn.fallen_gems_affixes.registry;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTabEvents {

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {

        if (event.getTab() == ModCreativeTabs.AUGMENTS_TAB.get()) {
            // Temp icon
            event.accept(Items.DIAMOND);
            // for (AugmentItem.AugmentData data : AugmentItem.getAllAugmentData()) {
            //     event.accept(AugmentItem.createAugment(data.getAugmentId()));
            // }
        }
    }
}