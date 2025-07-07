package net.kayn.fallen_gems_affixes.mixin.permanent_effect.client;

import net.kayn.fallen_gems_affixes.event.PermanentEffectHandler;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrappers;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;

@Mixin(LivingEntity.class)
@OnlyIn(Dist.CLIENT)
public class LivingEntityMixin {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * This method triggers when player **equip** an item.
     * <p>
     * This method {@link LivingEntity#onEquipItem} can be triggered when player equip item with {@link ItemStack#use}.
     * <p>
     * When a player drag or shift to successfully equip the item on container screen.
     * <p>
     * When set equipment slot with an item by command.
     * <p>
     * Originally both server and client can trigger, but we care client here.
     */
    @Inject(method = "onEquipItem", at = @At("HEAD"))
    private void onEquipItemPrefix(EquipmentSlot pSlot, ItemStack pOldItem, ItemStack pNewItem, CallbackInfo ci) {
        if (PermanentEffectHandler.isUseTickEvent()) return;
        if (!((Object) this instanceof LocalPlayer player)) return;
        var currentEffectsMap = player.getActiveEffectsMap();
        if (currentEffectsMap instanceof ProtectedMobEffectMap<?> map) {
            try {
                map.setOperator(ProtectedMobEffectMap.EffectOperator.ON_EQUIP);
                EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(pSlot);
                map.setCurrentSlot(slotWrapper);
                Set<MobEffect> cachedEffects = map.getEffectsFromCache(slotWrapper);
                if (cachedEffects != null) {
                    cachedEffects.forEach(e -> {
                        player.removeEffectNoUpdate(e);
                        if (map.containsPermanent(e)) {
                            player.forceAddEffect(map.getLastPotentialEffectInst(e), null);
                        }
                    });
                }
                if (map.getLastEffectsProvider() != pNewItem && EquipmentSlotUtil.matchesSlot(pNewItem, pSlot)) {
                    checkGemBonus(pNewItem, (bonus, rarity) -> {
                        MobEffect effect = bonus.getEffect();
                        int amplifier = bonus.getAmplifier(rarity);
                        MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                        player.forceAddEffect(inst, null);
                        map.setLastEffectsProvider(pNewItem);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                LOGGER.info("effect map {}", map);
                map.setOperator(ProtectedMobEffectMap.EffectOperator.EXTERNAL);
                map.setCurrentSlot(EquipmentSlotWrappers.NONE);
            }
        }
    }
}
