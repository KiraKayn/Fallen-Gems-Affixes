package net.kayn.fallen_gems_affixes.mixin;

import net.kayn.fallen_gems_affixes.types.common.LivingEntitySetter;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LivingEntitySetter {
    @Shadow
    protected int attackStrengthTicker;
    @Override
    public void FGA$setAttackStrengthTicker() {
        this.attackStrengthTicker = 1000;
    };
}
