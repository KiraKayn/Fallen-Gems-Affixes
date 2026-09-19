package net.kayn.fallen_gems_affixes.compat.celestisynth;

import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;

public class CelestisynthAttributeHandler {

    private static final boolean IRONS_SPELLBOOKS_LOADED = ModList.get().isLoaded("irons_spellbooks");

    @SubscribeEvent
    public static void onItemAttributeModifier(StackAttributeModifiersEvent event) {
        if (!ModConfig.ENABLE_CELESTISYNTH_ATTRIBUTES.get()) return;
        if (!IRONS_SPELLBOOKS_LOADED) return;

        ItemStack itemStack = event.getItemStack();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (itemId == null) return;

        CelestisynthIronsSpellbooksIntegration.applyAttributes(event, itemId.toString());
    }
}