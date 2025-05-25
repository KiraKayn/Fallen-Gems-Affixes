package net.kayn.apotheosis_ascended_fork.attributes;

import net.kayn.apotheosis_ascended_fork.ApotheosisAscendedFork;
import dev.shadowsoffire.attributeslib.impl.PercentBasedAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AAAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ApotheosisAscendedFork.MOD_ID);

    public static final RegistryObject<Attribute> KICK_REDUCTION = ATTRIBUTES.register("kick_reduction",
            () -> new PercentBasedAttribute("attribute.apotheosis_ascended_fork.kick_reduction", 0.0, 0.0, 1.0).setSyncable(true));

    public static final RegistryObject<Attribute> PROJECTILE_SPEED = ATTRIBUTES.register("projectile_speed",
            () -> new RangedAttribute("attribute.apotheosis_ascended_fork.projectile_speed", 0.0, -0.99, 10.0).setSyncable(true));

    public static final RegistryObject<Attribute> FIRE_RATE = ATTRIBUTES.register("fire_rate",
            () -> new RangedAttribute("attribute.apotheosis_ascended_fork.fire_rate", 0.0, -0.99, 10.0).setSyncable(true));

    public static final RegistryObject<Attribute> ADDITIONAL_AMMO = ATTRIBUTES.register("additional_ammo",
            () -> new RangedAttribute("attribute.apotheosis_ascended_fork.additional_ammo", 0.0, 0.0, 100.0).setSyncable(true));

    public static final RegistryObject<Attribute> SPREAD_REDUCTION = ATTRIBUTES.register("spread_reduction",
            () -> new PercentBasedAttribute("attribute.apotheosis_ascended_fork.spread_reduction", 0.0, 0.0, 1.0).setSyncable(true));

    public static final RegistryObject<Attribute> RELOAD_SPEED = ATTRIBUTES.register("reload_speed",
            () -> new RangedAttribute("attribute.apotheosis_ascended_fork.reload_speed", 0.0, -0.99, 10.0).setSyncable(true));

    public static final RegistryObject<Attribute> BULLET_DAMAGE = ATTRIBUTES.register("bullet_damage",
            () -> new RangedAttribute("attribute.apotheosis_ascended_fork.bullet_damage", 0.0, -0.99, 10.0).setSyncable(true));

}