package net.kayn.fallen_gems_affixes.event;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.ItemAttributeModifierEvent;

import java.util.UUID;

public class CelestisynthIronsSpellbooksIntegration {

    private static final UUID CRESCENTIA_UUID = UUID.fromString("c1e5c3a1-2b43-4c18-ab05-6de0bb4d64d1");
    private static final UUID SOLARIS_UUID = UUID.fromString("c1e5c3a2-2b43-4c18-ab05-6de0bb4d64d2");
    private static final UUID AQUAFLORA_UUID = UUID.fromString("c1e5c3a3-2b43-4c18-ab05-6de0bb4d64d3");
    private static final UUID BREEZEBREAKER_UUID = UUID.fromString("c1e5c3a4-2b43-4c18-ab05-6de0bb4d64d4");
    private static final UUID POLTERGEIST_UUID = UUID.fromString("c1e5c3a5-2b43-4c18-ab05-6de0bb4d64d5");
    private static final UUID RAINFALL_SERENITY_UUID = UUID.fromString("c1e5c3a6-2b43-4c18-ab05-6de0bb4d64d6");
    private static final UUID KERES_UUID = UUID.fromString("c1e5c3a7-2b43-4c18-ab05-6de0bb4d64d7");

    public static void applyAttributes(ItemAttributeModifierEvent event, String itemName) {
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
            case "celestisynth:solaris" ->
                    event.addModifier(AttributeRegistry.FIRE_SPELL_POWER.get(), new AttributeModifier(SOLARIS_UUID,
                            "Solaris Fire Spell Power Boost", 0.10,
                            AttributeModifier.Operation.MULTIPLY_BASE));

            case "celestisynth:aquaflora" ->
                    event.addModifier(AttributeRegistry.NATURE_SPELL_POWER.get(), new AttributeModifier(AQUAFLORA_UUID,
                            "Aquaflora Nature Spell Power Boost", 0.10,
                            AttributeModifier.Operation.MULTIPLY_BASE));

            case "celestisynth:breezebreaker" -> {
                event.addModifier(AttributeRegistry.NATURE_SPELL_POWER.get(), new AttributeModifier(BREEZEBREAKER_UUID,
                        "Breezebreaker Nature Spell Power Reduction", -0.25,
                        AttributeModifier.Operation.MULTIPLY_BASE));
                event.addModifier(AttributeRegistry.EVOCATION_SPELL_POWER.get(), new AttributeModifier(BREEZEBREAKER_UUID,
                        "Breezebreaker Evocation Spell Power Boost", 0.20,
                        AttributeModifier.Operation.MULTIPLY_BASE));
                event.addModifier(AttributeRegistry.EVOCATION_MAGIC_RESIST.get(), new AttributeModifier(BREEZEBREAKER_UUID,
                        "Breezebreaker Evocation Magic Resist Boost", 0.20,
                        AttributeModifier.Operation.MULTIPLY_BASE));
            }

            case "celestisynth:poltergeist" -> {
                event.addModifier(AttributeRegistry.ENDER_SPELL_POWER.get(), new AttributeModifier(POLTERGEIST_UUID,
                        "Poltergeist Ender Spell Power Reduction", -0.15,
                        AttributeModifier.Operation.MULTIPLY_BASE));
                event.addModifier(AttributeRegistry.ENDER_MAGIC_RESIST.get(), new AttributeModifier(POLTERGEIST_UUID,
                        "Poltergeist Ender Spell Power Reduction", -0.15,
                        AttributeModifier.Operation.MULTIPLY_BASE));
                event.addModifier(AttributeRegistry.ELDRITCH_SPELL_POWER.get(), new AttributeModifier(POLTERGEIST_UUID,
                        "Poltergeist Eldritch Spell Power Boost", 0.20,
                        AttributeModifier.Operation.MULTIPLY_BASE));
                event.addModifier(AttributeRegistry.ELDRITCH_MAGIC_RESIST.get(), new AttributeModifier(POLTERGEIST_UUID,
                        "Poltergeist Eldritch Magic Resist Boost", 0.20,
                        AttributeModifier.Operation.MULTIPLY_BASE));
            }

            case "celestisynth:rainfall_serenity" -> {
                event.addModifier(AttributeRegistry.SPELL_POWER.get(), new AttributeModifier(RAINFALL_SERENITY_UUID,
                        "Rainfall Serenity Spell Power Reduction", -0.075,
                        AttributeModifier.Operation.MULTIPLY_BASE));
                event.addModifier(AttributeRegistry.MANA_REGEN.get(), new AttributeModifier(RAINFALL_SERENITY_UUID,
                        "Rainfall Serenity Mana Regen Reduction", -0.10,
                        AttributeModifier.Operation.MULTIPLY_BASE));
                event.addModifier(AttributeRegistry.LIGHTNING_SPELL_POWER.get(), new AttributeModifier(RAINFALL_SERENITY_UUID,
                        "Rainfall Serenity Lightning Spell Power Boost", 0.20,
                        AttributeModifier.Operation.MULTIPLY_BASE));
                event.addModifier(AttributeRegistry.LIGHTNING_MAGIC_RESIST.get(), new AttributeModifier(RAINFALL_SERENITY_UUID,
                        "Rainfall Serenity Lightning Magic Resist Boost", 0.20,
                        AttributeModifier.Operation.MULTIPLY_BASE));
            }

            case "celestisynth:keres" ->
                    event.addModifier(AttributeRegistry.BLOOD_SPELL_POWER.get(), new AttributeModifier(KERES_UUID,
                            "Keres Blood Spell Power Boost", 0.10,
                            AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }
}