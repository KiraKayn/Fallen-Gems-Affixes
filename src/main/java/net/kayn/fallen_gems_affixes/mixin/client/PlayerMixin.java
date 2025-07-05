package net.kayn.fallen_gems_affixes.mixin.client;


import net.kayn.fallen_gems_affixes.util.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;

@Mixin(Player.class)
@OnlyIn(Dist.CLIENT)
public abstract class PlayerMixin extends Entity {
    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);

    private static final Logger LOGGER = LogManager.getLogger();

    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    @Inject(method = "setItemSlot", at = @At("HEAD"))
    private void onSetItemSlotPrefix(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
        LOGGER.info("into onSetItemSlot");
        if (!((Object) this instanceof LocalPlayer player)) return;
        var currentEffectsMap = player.getActiveEffectsMap();
        if (currentEffectsMap instanceof ProtectedMobEffectMap<?> map) {
            try {
                EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(pSlot);
                map.initOperation(slotWrapper);
                Set<MobEffect> cachedEffects = map.getEffectsFromCache(slotWrapper);
                if (cachedEffects != null) {
                    cachedEffects.forEach(e -> {
                        player.removeEffect(e);
                        if (map.containsPermanent(e)) {
                            player.addEffect(map.getLastPotentialEffectInst(e));
                        }
                    });
                }
                if (map.getLastEffectsProvider() != pStack && EquipmentSlotUtil.matchesSlot(pStack, pSlot)) {
                    checkGemBonus(pStack, (bonus, rarity) -> {
                        MobEffect effect = bonus.getEffect();
                        int amplifier = bonus.getAmplifier(rarity);
                        MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                        player.addEffect(inst);
                        map.setLastEffectsProvider(pStack);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                LOGGER.info("effect map {}", map);
                map.finalizeOperation();
            }
        }
    }
}