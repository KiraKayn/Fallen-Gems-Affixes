package net.kayn.fallen_gems_affixes.mixin.permanent_effect;

import net.kayn.fallen_gems_affixes.event.PermanentEffectHandler;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrappers;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;
import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.tickFunctionCurio;

@Mixin(value = Player.class, remap = false)
public class PlayerMixin {
    /**
     * Main logic when use Tick Event to manage Permanent Effect.
     */
    @Inject(method = "tick", at = @At("RETURN"))
    private void afterTick(CallbackInfo ci) {
        Player player = (Player)(Object)this;
        ProtectedMobEffectMap<LivingEntity> cached = PermanentEffectHandler.getTickEventProtectedMapWrapper(player);
        try {
            for (ItemStack equipment : player.getAllSlots()) {
                EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(equipment);
                if (slotWrapper == EquipmentSlotWrappers.NONE) continue;
                cached.initOperation(slotWrapper);
                Set<Holder<MobEffect>> effects = cached.getEffectsFromCache(slotWrapper);
                if (effects == null) continue;
                checkGemBonus(equipment, (bonus, rarity) -> {
                    Holder<MobEffect> effect = bonus.getEffect();
                    if (!player.getActiveEffectsMap().containsKey(effect) && effects.contains(effect)) {
                        player.addEffect(new MobEffectInstance(effect, -1, bonus.getAmplifier(rarity)));
                        cached.addPermanentEffect(slotWrapper, effect, bonus.getAmplifier(rarity), true);
                    }
                });
                break;
            }
            tickFunctionCurio.accept(player, cached);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cached.finalizeOperation();
        }
    }
}
