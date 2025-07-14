package net.kayn.fallen_gems_affixes.event;

import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;

import java.util.UUID;

public class CelestisynthAttributeHandler {

    // Unique UUIDs for each item to avoid conflicts
    private static final UUID CRESCENTIA_UUID = UUID.fromString("c1e5c3a1-2b43-4c18-ab05-6de0bb4d64d1");
    private static final UUID SOLARIS_UUID = UUID.fromString("c1e5c3a2-2b43-4c18-ab05-6de0bb4d64d2");
    private static final UUID AQUAFLORA_UUID = UUID.fromString("c1e5c3a3-2b43-4c18-ab05-6de0bb4d64d3");
    private static final UUID BREEZEBREAKER_UUID = UUID.fromString("c1e5c3a4-2b43-4c18-ab05-6de0bb4d64d4");
    private static final UUID POLTERGEIST_UUID = UUID.fromString("c1e5c3a5-2b43-4c18-ab05-6de0bb4d64d5");
    private static final UUID RAINFALL_SERENITY_UUID = UUID.fromString("c1e5c3a6-2b43-4c18-ab05-6de0bb4d64d6");
    private static final UUID KERES_UUID = UUID.fromString("c1e5c3a7-2b43-4c18-ab05-6de0bb4d64d7");

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        if (!ModConfig.ENABLE_CELESTISYNTH_ATTRIBUTES.get()) return;

        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            ItemStack itemStack = event.getItemStack();
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());

            if (itemId == null) return;

            String itemName = itemId.toString();

            // Celestisynth Crescentia
            switch (itemName) {
                case "celestisynth:crescentia" -> {
                    // -25% spell_resist
                    event.addModifier(AttributeRegistry.SPELL_RESIST.get(), new AttributeModifier(CRESCENTIA_UUID,
                            "Crescentia Spell Resist Reduction", -0.25,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // +20% ender_spell_power
                    event.addModifier(AttributeRegistry.ENDER_SPELL_POWER.get(), new AttributeModifier(CRESCENTIA_UUID,
                            "Crescentia Ender Spell Power Boost", 0.20,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // +20% ender_magic_resist
                    event.addModifier(AttributeRegistry.ENDER_MAGIC_RESIST.get(), new AttributeModifier(CRESCENTIA_UUID,
                            "Crescentia Ender Magic Resist Boost", 0.20,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                }
                // Celestisynth Solaris
                case "celestisynth:solaris" ->
                    // +20% fire_spell_power
                        event.addModifier(AttributeRegistry.FIRE_SPELL_POWER.get(), new AttributeModifier(SOLARIS_UUID,
                                "Solaris Fire Spell Power Boost", 0.10,
                                AttributeModifier.Operation.MULTIPLY_BASE));


                // Celestisynth Aquaflora
                case "celestisynth:aquaflora" ->
                    // +10% nature_spell_power
                        event.addModifier(AttributeRegistry.NATURE_SPELL_POWER.get(), new AttributeModifier(AQUAFLORA_UUID,
                                "Aquaflora Nature Spell Power Boost", 0.10,
                                AttributeModifier.Operation.MULTIPLY_BASE));


                // Celestisynth Breezebreaker
                case "celestisynth:breezebreaker" -> {
                    // -25% nature_spell_power
                    event.addModifier(AttributeRegistry.NATURE_SPELL_POWER.get(), new AttributeModifier(BREEZEBREAKER_UUID,
                            "Breezebreaker Nature Spell Power Reduction", -0.25,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // +20% evocation_spell_power
                    event.addModifier(AttributeRegistry.EVOCATION_SPELL_POWER.get(), new AttributeModifier(BREEZEBREAKER_UUID,
                            "Breezebreaker Evocation Spell Power Boost", 0.20,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // +20% evocation_magic_resist
                    event.addModifier(AttributeRegistry.EVOCATION_MAGIC_RESIST.get(), new AttributeModifier(BREEZEBREAKER_UUID,
                            "Breezebreaker Evocation Magic Resist Boost", 0.20,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                }

                // Celestisynth Poltergeist
                case "celestisynth:poltergeist" -> {
                    // -15% ender_spell_power
                    event.addModifier(AttributeRegistry.ENDER_SPELL_POWER.get(), new AttributeModifier(POLTERGEIST_UUID,
                            "Poltergeist Ender Spell Power Reduction", -0.15,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // -15% ender_magic_resist
                    event.addModifier(AttributeRegistry.ENDER_MAGIC_RESIST.get(), new AttributeModifier(POLTERGEIST_UUID,
                            "Poltergeist Ender Spell Power Reduction", -0.15,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // +20% eldritch_spell_power
                    event.addModifier(AttributeRegistry.ELDRITCH_SPELL_POWER.get(), new AttributeModifier(POLTERGEIST_UUID,
                            "Poltergeist Eldritch Spell Power Boost", 0.20,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // +20% eldritch_magic_resist
                    event.addModifier(AttributeRegistry.ELDRITCH_MAGIC_RESIST.get(), new AttributeModifier(POLTERGEIST_UUID,
                            "Poltergeist Eldritch Magic Resist Boost", 0.20,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                }

                // Celestisynth Rainfall Serenity
                case "celestisynth:rainfall_serenity" -> {
                    // -7.5% spell_power
                    event.addModifier(AttributeRegistry.SPELL_POWER.get(), new AttributeModifier(RAINFALL_SERENITY_UUID,
                            "Rainfall Serenity Spell Power Reduction", -0.075,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // -10% mana_regen
                    event.addModifier(AttributeRegistry.MANA_REGEN.get(), new AttributeModifier(RAINFALL_SERENITY_UUID,
                            "Rainfall Serenity Mana Regen Reduction", -0.10,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // +20% lightning_spell_power
                    event.addModifier(AttributeRegistry.LIGHTNING_SPELL_POWER.get(), new AttributeModifier(RAINFALL_SERENITY_UUID,
                            "Rainfall Serenity Lightning Spell Power Boost", 0.20,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                    // +20% lightning_magic_resist
                    event.addModifier(AttributeRegistry.LIGHTNING_MAGIC_RESIST.get(), new AttributeModifier(RAINFALL_SERENITY_UUID,
                            "Rainfall Serenity Lightning Magic Resist Boost", 0.20,
                            AttributeModifier.Operation.MULTIPLY_BASE));
                }

                // Celestisynth Keres
                case "celestisynth:keres" ->
                    // +10% blood_spell_power
                        event.addModifier(AttributeRegistry.BLOOD_SPELL_POWER.get(), new AttributeModifier(KERES_UUID,
                                "Keres Blood Spell Power Boost", 0.10,
                                AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }
}