package net.kayn.fallen_gems_affixes.attributes;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AAAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, FallenGemsAffixes.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> KICK_REDUCTION = ATTRIBUTES.register("kick_reduction",
            () -> new PercentBasedAttribute("attribute.fallen_gems_affixes.kick_reduction", 0.0, 0.0, 1.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> PROJECTILE_SPEED = ATTRIBUTES.register("projectile_speed",
            () -> new RangedAttribute("attribute.fallen_gems_affixes.projectile_speed", 0.0, -0.99, 10.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> FIRE_RATE = ATTRIBUTES.register("fire_rate",
            () -> new RangedAttribute("attribute.fallen_gems_affixes.fire_rate", 0.0, -0.99, 10.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ADDITIONAL_AMMO = ATTRIBUTES.register("additional_ammo",
            () -> new RangedAttribute("attribute.fallen_gems_affixes.additional_ammo", 0.0, 0.0, 100.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SPREAD_REDUCTION = ATTRIBUTES.register("spread_reduction",
            () -> new PercentBasedAttribute("attribute.fallen_gems_affixes.spread_reduction", 0.0, 0.0, 1.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> RELOAD_SPEED = ATTRIBUTES.register("reload_speed",
            () -> new RangedAttribute("attribute.fallen_gems_affixes.reload_speed", 0.0, -0.99, 10.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> BULLET_DAMAGE = ATTRIBUTES.register("bullet_damage",
            () -> new RangedAttribute("attribute.fallen_gems_affixes.bullet_damage", 0.0, -0.99, 10.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> MAX_HEALTH_DAMAGE = ATTRIBUTES.register("max_health_damage",
            () -> new PercentBasedAttribute("attribute.fallen_gems_affixes.max_health_damage", 0.0D, 0.0D, 1.0D).setSyncable(true));

}