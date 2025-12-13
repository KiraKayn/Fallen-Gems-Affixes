package net.kayn.fallen_gems_affixes.registry;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<CreativeModeTab> AUGMENTS_TAB = CREATIVE_MODE_TABS.register("augments",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.fallen_gems_affixes.fallen_gems_affixes"))
                    .icon(() -> new ItemStack(ModItems.SIGIL_OF_ASCENSION.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.SIGIL_OF_ASCENSION.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}