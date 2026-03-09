package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.augment.CascadeAugment;
import net.kayn.fallen_gems_affixes.augment.DualityAugment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AttributeEvents.class, remap = false)
public abstract class CritMixin {

    private static final ThreadLocal<Integer> CRIT_ITERATION =
            ThreadLocal.withInitial(() -> 0);

    private static boolean noRecurse$FGA = false;

    private static boolean vanillaCritMark$FGA = false;

    @Inject(method = "apothCriticalStrike", at = @At("HEAD"))
    private void resetCounter(LivingHurtEvent e, CallbackInfo ci) {
        CRIT_ITERATION.set(0);
    }

    @ModifyVariable(
            method = "apothCriticalStrike",
            at = @At(value = "CONSTANT", args = "floatValue=0.85"),
            name = "critMult"
    )
    private float applyCascade(float critMult, LivingHurtEvent e) {
        int iteration = CRIT_ITERATION.get();
        CRIT_ITERATION.set(iteration + 1);

        // Only fires on overcrit (iteration 1+)
        if (iteration == 0) return critMult;

        LivingEntity attacker = e.getSource().getEntity() instanceof LivingEntity le ? le : null;
        if (attacker == null) return critMult;

        CascadeAugment.CascadeData data = CascadeAugment.getCascadeData(attacker);
        if (data == null) return critMult;

        if (attacker.getRandom().nextFloat() < data.chance) {
            critMult *= (1.0f + data.damageBonus);
        }

        return critMult;
    }

    // duality implementation
    @Inject(method = "apothCriticalStrike", at = @At("HEAD"), cancellable = true)
    private void dualityNoRecurse(LivingHurtEvent e, CallbackInfo ci) {
        if (noRecurse$FGA) ci.cancel();
    }

    @Inject(method = "apothCriticalStrike", at = @At("TAIL"))
    private void dualityCritGlobal(LivingHurtEvent e, CallbackInfo ci) {
        if (CRIT_ITERATION.get() > 0 || vanillaCritMark$FGA) {
            LivingEntity attacker = e.getSource().getEntity() instanceof LivingEntity le ? le : null;
            if (attacker == null) return;
            LivingEntity target = e.getEntity();
            ItemStack stack = attacker.getMainHandItem();
            if (stack.hasTag() && stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
                CompoundTag augmentData = stack.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA);
                ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, CompoundTag.TAG_COMPOUND);
                for (int i = 0; i < augments.size(); i++) {
                    CompoundTag augment = augments.getCompound(i);
                    if (augment.getString(Fallen.AugmentMisc.TYPE).equals(Fallen.Augments.DUALITY_STRING)) {
                        DualityAugment.DualityData data = (DualityAugment.DualityData) Fallen.Augments.DUALITY.deserializeInnerData(augment.getCompound(Fallen.AugmentMisc.INNER_DATA));
                        float physicalRatio = data.getPhysicalRatio();
                        float magicRatio = data.getMagicRatio();
                        float original = e.getAmount();
                        e.setAmount(original * physicalRatio);
                        dealDualityMagicDamage$FGA(target, attacker, original, magicRatio);
                        break;
                    }
                }
            }
        }
        vanillaCritMark$FGA = false;
    }

    @Inject(method = "vanillaCritDmg", at = @At("TAIL"))
    private void dualityCritVanilla(CriticalHitEvent e, CallbackInfo ci) {
        if (e.isVanillaCritical()) {
            if (!(e.getTarget() instanceof LivingEntity)) return;
            vanillaCritMark$FGA = true;
        }
    }

    private void dealDualityMagicDamage$FGA(Entity target, LivingEntity attacker, float originalDamage, float magicRatio) {
        DamageSource damageSource = new DamageSource(
                target.level().registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.MAGIC),
                attacker
        );
        noRecurse$FGA = true;
        int time = target.invulnerableTime;
        target.invulnerableTime = 0;
        target.hurt(damageSource, originalDamage * magicRatio);
        target.invulnerableTime = time;
        noRecurse$FGA = false;
    }
}