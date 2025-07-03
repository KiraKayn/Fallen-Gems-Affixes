package net.kayn.fallen_gems_affixes.mixin.client;


import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;

@Mixin(LivingEntity.class)
@OnlyIn(Dist.CLIENT)
public abstract class LivingEntityMixin extends Entity {
    private static final Logger LOGGER = LogManager.getLogger();

    protected LivingEntityMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "onEquipItem", at = @At("HEAD"))
    private void onEquipItemPrefix(EquipmentSlot pSlot, ItemStack pOldItem, ItemStack pNewItem, CallbackInfo ci) {
        LOGGER.info("into onSetItemSlot");
        if (!((Object) this instanceof LocalPlayer player)) return;
        var currentEffectsMap = player.getActiveEffectsMap();
        if (currentEffectsMap instanceof ProtectedMobEffectMap<?> map) {
            map.setRemover(ProtectedMobEffectMap.EffectRemover.ON_EQUIP);
            try {
                checkGemBonus(pNewItem, (bonus, rarity) -> {
                    MobEffect effect = bonus.getEffect();
                    int amplifier = bonus.getAmplifier(rarity);
                    MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                    player.addEffect(inst);
                    map.addPermanentEffect(effect, amplifier);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOGGER.info("effect map {}", map);
            map.setRemover(ProtectedMobEffectMap.EffectRemover.EXTERNAL);
        }
    }
}