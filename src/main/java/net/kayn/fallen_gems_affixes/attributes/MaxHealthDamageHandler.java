package net.kayn.fallen_gems_affixes.attributes;

import net.kayn.fallen_gems_affixes.util.AttributesUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MaxHealthDamageHandler {

    private boolean noRecurse = false;

    public MaxHealthDamageHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide || event.getEntity().isDeadOrDying()) return;
        if (noRecurse) return;

        noRecurse = true;

        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
            LivingEntity target = event.getEntity();

            if (AttributesUtil.isPhysicalDamage(event.getSource())
                    && attacker.getAttributes().hasAttribute(AAAttributes.MAX_HEALTH_DAMAGE.get())) {

                double attrValue = attacker.getAttributeValue(AAAttributes.MAX_HEALTH_DAMAGE.get());
                if (attrValue > 0.001) {
                    int time = target.invulnerableTime;
                    target.invulnerableTime = 0;

                    float extraDamage = (float) (attrValue * target.getMaxHealth());
                    DamageSource damageSource = new DamageSource(
                            target.level().registryAccess()
                                    .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                                    .getHolderOrThrow(AADamageTypes.MAX_HEALTH_DAMAGE),
                            attacker
                    );
                    target.hurt(damageSource, extraDamage);

                    target.invulnerableTime = time;

                    if (target.isDeadOrDying()) {
                        target.getPersistentData().putBoolean("apoth.killed_by_aux_dmg", true);
                    }
                }
            }
        }

        noRecurse = false;
    }
}