package net.kayn.fallen_gems_affixes.mixin.permanent_effect.client;


import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.effectOperationBySlot;

@Mixin(Player.class)
@OnlyIn(Dist.CLIENT)
public abstract class PlayerMixin {
    /**
     * This method triggers when {@link EquipmentSlot} {@link Slot} changes by player.
     * <p>
     * This method can be triggered when player equip item with {@link ItemStack#use}.
     * <p>
     * This method can be triggered when player try to put item into {@link EquipmentSlot} {@link Slot}.
     * <p>
     * Originally both server and client can trigger, but we care client here.
     */
    @Inject(method = "setItemSlot", at = @At("HEAD"))
    private void onSetItemSlotPrefix(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
        if (!((Object) this instanceof LocalPlayer player)) return;
        var currentEffectsMap = player.getActiveEffectsMap();
        if (currentEffectsMap instanceof ProtectedMobEffectMap<?> map) {
            EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(pSlot);
            effectOperationBySlot(map, player, slotWrapper, pStack);
        }
    }
}