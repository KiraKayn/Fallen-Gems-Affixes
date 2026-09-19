package net.kayn.fallen_gems_affixes.compat.celestisynth;

import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import dev.shadowsoffire.apothic_attributes.modifiers.StackAttributeModifiersEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class CelestisynthIronsSpellbooksIntegration {

    private static final ResourceLocation CRESCENTIA_ID    = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "crescentia_bonus");
    private static final ResourceLocation SOLARIS_ID       = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "solaris_bonus");
    private static final ResourceLocation AQUAFLORA_ID     = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "aquaflora_bonus");
    private static final ResourceLocation BREEZEBREAKER_ID = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "breezebreaker_bonus");
    private static final ResourceLocation POLTERGEIST_ID   = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "poltergeist_bonus");
    private static final ResourceLocation RAINFALL_ID      = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "rainfall_bonus");
    private static final ResourceLocation KERES_ID         = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "keres_bonus");

    private static final ResourceLocation MAINHAND_SLOT_ID = ResourceLocation.fromNamespaceAndPath("apothic_attributes", "mainhand");

    public static void applyAttributes(StackAttributeModifiersEvent event, String itemName) {
        EntitySlotGroup mainhand = ALObjects.BuiltInRegs.ENTITY_SLOT_GROUP.get(MAINHAND_SLOT_ID);
        if (mainhand == null) return;

        switch (itemName) {
            case "celestisynth:crescentia" -> {
                event.addModifier(AttributeRegistry.SPELL_RESIST, new AttributeModifier(CRESCENTIA_ID, -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.ENDER_SPELL_POWER, new AttributeModifier(CRESCENTIA_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.ENDER_MAGIC_RESIST, new AttributeModifier(CRESCENTIA_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
            }
            case "celestisynth:solaris" ->
                    event.addModifier(AttributeRegistry.FIRE_SPELL_POWER, new AttributeModifier(SOLARIS_ID, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);

            case "celestisynth:aquaflora" ->
                    event.addModifier(AttributeRegistry.NATURE_SPELL_POWER, new AttributeModifier(AQUAFLORA_ID, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);

            case "celestisynth:breezebreaker" -> {
                event.addModifier(AttributeRegistry.NATURE_SPELL_POWER, new AttributeModifier(BREEZEBREAKER_ID, -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.EVOCATION_SPELL_POWER, new AttributeModifier(BREEZEBREAKER_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.EVOCATION_MAGIC_RESIST, new AttributeModifier(BREEZEBREAKER_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
            }

            case "celestisynth:poltergeist" -> {
                event.addModifier(AttributeRegistry.ENDER_SPELL_POWER, new AttributeModifier(POLTERGEIST_ID, -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.ENDER_MAGIC_RESIST, new AttributeModifier(POLTERGEIST_ID, -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.ELDRITCH_SPELL_POWER, new AttributeModifier(POLTERGEIST_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.ELDRITCH_MAGIC_RESIST, new AttributeModifier(POLTERGEIST_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
            }

            case "celestisynth:rainfall_serenity" -> {
                event.addModifier(AttributeRegistry.SPELL_POWER, new AttributeModifier(RAINFALL_ID, -0.075, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.MANA_REGEN, new AttributeModifier(RAINFALL_ID, -0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.LIGHTNING_SPELL_POWER, new AttributeModifier(RAINFALL_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
                event.addModifier(AttributeRegistry.LIGHTNING_MAGIC_RESIST, new AttributeModifier(RAINFALL_ID, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
            }

            case "celestisynth:keres" ->
                    event.addModifier(AttributeRegistry.BLOOD_SPELL_POWER, new AttributeModifier(KERES_ID, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), mainhand);
        }
    }
}