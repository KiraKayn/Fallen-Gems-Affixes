package net.kayn.fallen_gems_affixes.mixin.client;

import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;

@Mixin(Player.class)
public class ClientPlayerMixin {
    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(method = "setItemSlot", at = @At("TAIL"))
    private void onSetItemSlotSuffix(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
        LOGGER.info("into onSetItemSlot");
        if (!((Object) this instanceof LocalPlayer player)) return;
        if (player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map) {
            map.setRemover(ProtectedMobEffectMap.EffectRemover.ON_EQUIP);
            try {
                checkGemBonus(pOldItem, (bonus, rarity) -> {
                    MobEffect effect = bonus.getEffect();
                    if (player.hasEffect(effect)) {
                        LOGGER.info("remove {}", effect);
                        map.removePermanentEffect(effect);
                        player.removeEffect(effect);
                    }
                });
                checkGemBonus(pNewItem, (bonus, rarity) -> {
                    MobEffect effect = bonus.getEffect();
                    if (!player.hasEffect(effect)) {
                        map.addPermanentEffect(bonus.getEffect());
                        player.addEffect(new MobEffectInstance(effect, -1, bonus.getAmplifier(rarity)));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOGGER.info("effect map {}", map);
            map.setRemover(ProtectedMobEffectMap.EffectRemover.EXTERNAL);
        }
    }
}
