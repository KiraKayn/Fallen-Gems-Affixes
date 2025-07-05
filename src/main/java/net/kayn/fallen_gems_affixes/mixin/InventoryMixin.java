package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Shadow
    @Final
    public Player player;

    @Inject(method = "load", at = @At("TAIL"))
    private void onLoad(ListTag pListTag, CallbackInfo ci) {
        if (!(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        try {
            int index = 0;
            for (ItemStack equipment : player.getAllSlots()) {
                EquipmentSlot slot = EquipmentSlotUtil.slotFromAllSlotsIndex(index++);
                if (slot == null) continue;
                EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(slot);
                if (slotWrapper == null) continue;
                map.initOperation(slotWrapper, ProtectedMobEffectMap.EffectOperator.ON_INIT);
                for (EquipmentSlot slot1 : LootCategory.forItem(equipment).getSlots()) {
                    if (slot1 == slot) {
                        checkGemBonus(equipment, (bonus, rarity) -> {
                            MobEffect effect = bonus.getEffect();
                            int amplifier = bonus.getAmplifier(rarity);
                            MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                            player.forceAddEffect(inst, null);
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            map.finalizeOperation();
        }
    }
}
