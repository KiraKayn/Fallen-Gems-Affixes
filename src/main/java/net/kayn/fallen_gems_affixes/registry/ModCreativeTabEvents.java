package net.kayn.fallen_gems_affixes.registry;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.kayn.fallen_gems_affixes.Fallen.Registries.AUGMENT_REGISTRY;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTabEvents {

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {

        if (event.getTab() == ModCreativeTabs.AUGMENTS_TAB.get()) {
            AUGMENT_REGISTRY.forEach((id, aug) -> {
                event.accept(AugmentItem.createAugment(aug));
            });
        }
    }
}