package net.kayn.fallen_gems_affixes.mixin.permanent_effect.client;

import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrappers;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
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
    @Shadow
    @Final
    public Container container;
    @Shadow
    @Final
    private int slot;

    /**
     * The method injected {@link Slot#onTake} triggers when player takes item out of the slot that can be logical equipment slot by a click.
     * <p>
     * This method is clientside only.
     */
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
                player.removeEffectNoUpdate(effect);
                if (map.containsPermanent(effect)) {
                    player.forceAddEffect(map.getLastPotentialEffectInst(effect), null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            map.setOperator(ProtectedMobEffectMap.EffectOperator.EXTERNAL);
            map.setCurrentSlot(EquipmentSlotWrappers.NONE);
        }
    }
}
