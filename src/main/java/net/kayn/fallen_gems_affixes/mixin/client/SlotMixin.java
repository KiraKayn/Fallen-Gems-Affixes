package net.kayn.fallen_gems_affixes.mixin.client;

import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrappers;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
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

/**
 * The method injected {@link Slot#onTake} triggers when player take item out of the slot which can be logical equipment slot by click.
 * <p>
 *
 */
@Mixin(Slot.class)
@OnlyIn(Dist.CLIENT)
public class SlotMixin {
    @Shadow @Final private int slot;

    @Shadow @Final public Container container;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void onTakePrefix(Player pPlayer, ItemStack pStack, CallbackInfo ci) {
        if (!(this.container instanceof Inventory inv)) return;
        if (!(inv.player instanceof LocalPlayer player)) return;
        if (!(slot >= 36 && slot <= 40 || slot == inv.selected)) return;
        if (inv.player != pPlayer) return;
        if (!(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        if (pStack.isEmpty()) return;
        try {
            EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(LivingEntity.getEquipmentSlotForItem(pStack));
            map.setOperator(ProtectedMobEffectMap.EffectOperator.ON_EQUIP);
            map.setCurrentSlot(slotWrapper);
            checkGemBonus(pStack, (bonus, rarity) -> {
                MobEffect effect = bonus.getEffect();
                player.removeEffect(effect);
                if (map.containsPermanent(effect)) {
                    player.addEffect(map.getLastPotentialEffectInst(effect));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            map.setOperator(ProtectedMobEffectMap.EffectOperator.EXTERNAL);
            map.setCurrentSlot(EquipmentSlotWrappers.NONE);
        }
    }
//
//    @Inject(method = "set", at = @At("HEAD"))
//    private void onSetPrefix(ItemStack pStack, CallbackInfo ci) {
//        if (!(this.container instanceof Inventory inv)) return;
//        if (!(inv.player instanceof LocalPlayer player)) return;
//        if (!(slot >= 36 && slot <= 40 || slot == inv.selected)) return;
//        if (!(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
//        if (pStack.isEmpty()) return;
//        try {
//            EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(LivingEntity.getEquipmentSlotForItem(pStack));
//            map.setOperator(ProtectedMobEffectMap.EffectOperator.ON_EQUIP);
//            map.setCurrentSlot(slotWrapper);
//            checkGemBonus(pStack, (bonus, rarity) -> {
//                MobEffect effect = bonus.getEffect();
//                player.removeEffect(effect);
//                if (map.containsPermanent(effect)) {
//                    player.addEffect(map.getLastPotentialEffectInst(effect));
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            map.setOperator(ProtectedMobEffectMap.EffectOperator.EXTERNAL);
//            map.setCurrentSlot(EquipmentSlotWrappers.NONE);
//        }
//    }
}
