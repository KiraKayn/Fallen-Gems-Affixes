package net.kayn.fallen_gems_affixes.event;

import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class CelestisynthAttributeHandler {

    private static final boolean IRONS_SPELLBOOKS_LOADED = ModList.get().isLoaded("irons_spellbooks");

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        if (!ModConfig.ENABLE_CELESTISYNTH_ATTRIBUTES.get()) return;
        if (!IRONS_SPELLBOOKS_LOADED) return;

        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            ItemStack itemStack = event.getItemStack();
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());

            if (itemId == null) return;

            String itemName = itemId.toString();

            // Only call the integration if the mod is loaded
            CelestisynthIronsSpellbooksIntegration.applyAttributes(event, itemName);
        }
    }
}