package net.kayn.fallen_gems_affixes.attributes;

import net.kayn.fallen_gems_affixes.util.AttributesUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MaxHealthDamageHandler {

    private boolean noRecurse = false;

    public MaxHealthDamageHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide || event.getEntity().isDeadOrDying()) return;
        if (noRecurse) return;

        noRecurse = true;

        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
            LivingEntity target = event.getEntity();

            if (AttributesUtil.isPhysicalDamage(event.getSource())
                    && attacker.getAttributes().hasAttribute(AAAttributes.MAX_HEALTH_DAMAGE.get())) {

                double attrValue = attacker.getAttributeValue(AAAttributes.MAX_HEALTH_DAMAGE.get());
                if (attrValue > 0) {
                    float extraDamage = (float) (attrValue * target.getMaxHealth());
                    event.setAmount(event.getAmount() + extraDamage);
                }
            }
        }

        noRecurse = false;
    }
}