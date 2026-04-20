package net.kayn.fallen_gems_affixes.attributes;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class AADamageTypes {

    public static final ResourceKey<DamageType> MAX_HEALTH_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "max_health_damage")
    );

    public static void bootstrap(BootstapContext<DamageType> context) {
        context.register(MAX_HEALTH_DAMAGE, new DamageType(
                "max_health_damage",
                net.minecraft.world.damagesource.DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                0.1F
        ));
    }
}