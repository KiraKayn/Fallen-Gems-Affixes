package net.kayn.fallen_gems_affixes.mixin.client;

import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;

@Mixin(Slot.class)
@OnlyIn(Dist.CLIENT)
public class SlotMixin {
    @Shadow @Final private int slot;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void onTakePrefix(Player pPlayer, ItemStack pStack, CallbackInfo ci) {
        if (!(slot >= 36 && slot <= 39)) return;
        if (!(pPlayer instanceof LocalPlayer player)) return;
        var currentEffectsMap = player.getActiveEffectsMap();
        if (!(currentEffectsMap instanceof ProtectedMobEffectMap<?> map)) return;
        map.setRemover(ProtectedMobEffectMap.EffectRemover.ON_EQUIP);
        checkGemBonus(pStack, (bonus, rarity) -> {
            MobEffect effect = bonus.getEffect();
            int amplifier = bonus.getAmplifier(rarity);
            player.removeEffect(effect);
            map.tryRemovePermanentEffect(effect, amplifier);
            if (map.containsPermanent(effect)) {
                player.addEffect(map.getLastPotentialEffectInst(effect));
            }
        });
        map.setRemover(ProtectedMobEffectMap.EffectRemover.EXTERNAL);
    }
}
