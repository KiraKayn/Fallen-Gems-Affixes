package net.kayn.fallen_gems_affixes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_SOCKET_GEM_MODIFIER;
    public static final ForgeConfigSpec.DoubleValue SOCKET_GEM_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SOCKET_MIXIN;
    public static final ForgeConfigSpec.BooleanValue PERMANENT_EFFECT_USE_TICK_EVENT;

    static {
        BUILDER.push("Socket Gem Modifier");

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
                .comment("True: Switches to Player Tick event, Permanent Effect Bonus will apply every tick, worse performance but better compatibility")
                .comment("False: Use default implementation, aggressive but no Player Tick event, better for performance")
                .define("permanentEffectUseTickEvent", false);



        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}