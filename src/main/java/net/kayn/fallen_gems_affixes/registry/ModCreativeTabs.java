package net.kayn.fallen_gems_affixes.registry;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.reforging.FabledReforging;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.storage.GemCaseRegistry;
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
                        output.accept(ModItems.SIGIL_OF_TRANSMUTATION.get());
                        output.accept(ModItems.SIGIL_OF_SEVERANCE.get());
                        output.accept(ModItems.REINFORCED_GEM_SLATE.get());
                        output.accept(ModItems.AFFIX_SCROLL.get());
                        output.accept(ModItems.SIGIL_OF_ERASURE.get());
                        output.accept(GemCaseRegistry.GEM_CASE_ITEM.get());
                        output.accept(GemCaseRegistry.ENDER_GEM_CASE_ITEM.get());
                        output.accept(ModItems.SIGIL_OF_ELEVATION.get());
                        output.accept(ModItems.FABLED_MATERIAL.get());
                        output.accept(ModItems.SIGIL_OF_PRISMATIC_CONVERSION.get());
                        output.accept(ModItems.SIGIL_OF_CONFLUENCE.get());
                        output.accept(FabledReforging.FABLED_REFORGING_TABLE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}