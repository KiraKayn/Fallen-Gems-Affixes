package net.kayn.fallen_gems_affixes.registry;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<CreativeModeTab> AUGMENTS_TAB = CREATIVE_MODE_TABS.register("augments",
            () -> CreativeModeTab.builder()
                    .icon(() -> AugmentItem.createAugment(new ResourceLocation(FallenGemsAffixes.MOD_ID, "soulbound")))
                    .title(Component.translatable("itemGroup.fallen_gems_affixes.augments"))
                    .displayItems((parameters, output) -> {
                        // Add all augment types from loaded JSON data
                        for (AugmentItem.AugmentData data : AugmentItem.getAllAugmentData()) {
                            output.accept(AugmentItem.createAugment(data.getAugmentId()));
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
