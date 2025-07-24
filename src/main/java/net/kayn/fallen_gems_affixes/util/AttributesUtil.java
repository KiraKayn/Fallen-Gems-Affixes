package net.kayn.fallen_gems_affixes.util;

import net.minecraft.world.damagesource.DamageSource;

public class AttributesUtil {

    public static boolean isPhysicalDamage(DamageSource source) {
        String type = source.getMsgId();

        return !type.equals("magic") && !type.equals("explosion") && !type.equals("wither") && !type.equals("anvil");
    }
}