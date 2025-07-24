package net.kayn.fallen_gems_affixes.attributes;

import net.kayn.fallen_gems_affixes.util.AttributesUtil;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class MaxHealthDamageHandler {

    private boolean noRecurse = false;

    public MaxHealthDamageHandler() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide || event.getEntity().isDeadOrDying()) return;
        if (noRecurse) return;

        noRecurse = true;

        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
            LivingEntity target = event.getEntity();

            if (AttributesUtil.isPhysicalDamage(event.getSource())
                    && attacker.getAttributes().hasAttribute(AAAttributes.MAX_HEALTH_DAMAGE)) {

                double attrValue = attacker.getAttributeValue(AAAttributes.MAX_HEALTH_DAMAGE);
                if (attrValue > 0) {
                    float extraDamage = (float) (attrValue * target.getMaxHealth());
                    event.setNewDamage(event.getOriginalDamage() + extraDamage);
                }
            }
        }

        noRecurse = false;
    }
}