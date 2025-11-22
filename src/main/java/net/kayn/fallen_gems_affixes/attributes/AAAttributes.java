package net.kayn.fallen_gems_affixes.attributes;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import dev.shadowsoffire.attributeslib.impl.PercentBasedAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AAAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<Attribute> MAX_HEALTH_DAMAGE = ATTRIBUTES.register("max_health_damage",
            () -> new PercentBasedAttribute("attribute.fallen_gems_affixes.max_health_damage", 0.0D, 0.0D, 1.0D).setSyncable(true));

}