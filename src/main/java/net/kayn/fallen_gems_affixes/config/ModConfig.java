package net.kayn.fallen_gems_affixes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_SOCKET_GEM_MODIFIER;
    public static final ForgeConfigSpec.DoubleValue SOCKET_GEM_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SOCKET_MIXIN;
    public static final ForgeConfigSpec.BooleanValue PERMANENT_EFFECT_USE_TICK_EVENT;
    public static final ForgeConfigSpec.BooleanValue STRICT_SCHOOL_MATCH;

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
                .define("permanentEffectUseTickEvent", false);

        STRICT_SCHOOL_MATCH = BUILDER
                .comment("If false, Adaptive Spell Power Affixes can apply to any compatible item regardless of spell school.")
                .comment("If true, affixes will only apply to items that already grant spell power of the matching school.")
                .define("strictSpellSchoolMatching", true);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}