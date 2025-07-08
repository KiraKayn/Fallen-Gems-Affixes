package net.kayn.fallen_gems_affixes.mixin.permanent_effect.client;

import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.effectOperationBySlot;

@Mixin(LivingEntity.class)
@OnlyIn(Dist.CLIENT)
public class LivingEntityMixin {
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
        if (!((Object) this instanceof LocalPlayer player)) return;
        var currentEffectsMap = player.getActiveEffectsMap();
        if (currentEffectsMap instanceof ProtectedMobEffectMap<?> map) {
            EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(pSlot);
            effectOperationBySlot(map, player, slotWrapper, pNewItem);
        }
    }
}
