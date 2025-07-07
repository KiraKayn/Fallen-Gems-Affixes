package net.kayn.fallen_gems_affixes.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public interface IPermanentEffectHandler {
    void addPermanentEffect(LivingEntity entity, EquipmentSlotWrapper slot, MobEffect effect, int amplifier);

    void removePermanentEffect(LivingEntity entity, EquipmentSlotWrapper slot, MobEffect effect, int amplifier);

    void setEffectPermanent(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, MobEffect effect, int amplifier);

    void unsetEffectPermanent(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, MobEffect effect, int amplifier);
}
