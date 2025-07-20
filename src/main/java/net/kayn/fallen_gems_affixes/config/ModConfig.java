package net.kayn.fallen_gems_affixes.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_SOCKET_GEM_MODIFIER;
    public static final ForgeConfigSpec.DoubleValue SOCKET_GEM_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SOCKET_MIXIN;
    public static final ForgeConfigSpec.BooleanValue PERMANENT_EFFECT_USE_TICK_EVENT;
    public static final ForgeConfigSpec.BooleanValue STRICT_SCHOOL_MATCH;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CELESTISYNTH_ATTRIBUTES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SPELL_POWER_PATCH;
    public static final ForgeConfigSpec.ConfigValue<List<String>> IRONS_ITEMS_MAP;

    static {
        BUILDER.push("Mod Config");

        ENABLE_SOCKET_GEM_MODIFIER = BUILDER
                .comment("Enable gem socket injection into items via loot modifier")
                .define("enableSocketGemInjector", true);

        SOCKET_GEM_CHANCE = BUILDER
                .comment("Chance to insert a random gem into each socket slot, 1.0 means 100%")
                .defineInRange("socketGemChance", 0.3, 0.0, 1.0);

        ENABLE_SOCKET_MIXIN = BUILDER
                .comment("Enable the SocketHelperMixin that allows gaps between sockets")
                .define("enableSocketHelperMixin", true);

        PERMANENT_EFFECT_USE_TICK_EVENT = BUILDER
                .comment("Switch the implementation type of PermanentEffectBonus")
                .comment("If true, switches to Player Tick event. Better compatibility, worse performance.")
                .comment("If false, uses default impl. More performant, less compatible.")
                .define("permanentEffectUseTickEvent", false);

        STRICT_SCHOOL_MATCH = BUILDER
                .comment("If false, Adaptive Spell Power Affixes can apply to any compatible item regardless of spell school.")
                .comment("If true, affixes will only apply to items that already grant spell power of the matching school.")
                .define("strictSpellSchoolMatching", true);

        ENABLE_CELESTISYNTH_ATTRIBUTES = BUILDER
                .comment("If true, applies Spell Power attributes to Celestisynth weapons via ItemAttributeModifierEvent.")
                .define("enableCelestisynthAttributes", true);

        ENABLE_SPELL_POWER_PATCH = BUILDER
                .comment("If true, enables Celestisynth weapons patch to increase weapon damage when held, scaled by the respective Spell Power on the item")
                .define("enableCelestisynthSpellPowerPatch", true);

        IRONS_ITEMS_MAP = BUILDER
                .comment("Map of item Resource Locations to School Type IDs for Adaptive Spell Power Affixes")
                .comment("Example: modid:itemid|modid:schoolid")
                .comment("You can also set multiple schools: modid:itemid|modid:schoolid|modid:schoolid")
                .define("irons_items", new ArrayList<>(List.of(
                        "celestisynth:crescentia|irons_spellbooks:ender",
                        "celestisynth:solaris|irons_spellbooks:fire",
                        "celestisynth:aquaflora|irons_spellbooks:nature",
                        "celestisynth:breezebreaker|irons_spellbooks:evocation",
                        "celestisynth:poltergeist|irons_spellbooks:eldritch",
                        "celestisynth:rainfall_serenity|irons_spellbooks:lightning",
                        "celestisynth:keres|irons_spellbooks:blood",
                        "celestisynth:frostbound|irons_spellbooks:ice"
                )));

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}