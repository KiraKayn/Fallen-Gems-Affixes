package net.kayn.fallen_gems_affixes.mixin.client;

import net.kayn.fallen_gems_affixes.event.PermanentEffectHandler;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrappers;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
     * This method {@link LivingEntity#onEquipItem} can be triggered when player equip item with {@link ItemStack#use}.
     * When a player drag or shift to successfully equip the item on container screen.
     *
     */
    @Inject(method = "onEquipItem", at = @At("HEAD"))
    private void onEquipItemPrefix(EquipmentSlot pSlot, ItemStack pOldItem, ItemStack pNewItem, CallbackInfo ci) {
        LOGGER.info("into onSetItemSlot");
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
                        player.removeEffect(e);
                        if (map.containsPermanent(e)) {
                            player.addEffect(map.getLastPotentialEffectInst(e));
                        }
                    });
                }
                if (map.getLastEffectsProvider() != pNewItem && EquipmentSlotUtil.matchesSlot(pNewItem, pSlot)) {
                        checkGemBonus(pNewItem, (bonus, rarity) -> {
                            MobEffect effect = bonus.getEffect();
                            int amplifier = bonus.getAmplifier(rarity);
                            MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                            player.addEffect(inst);
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
