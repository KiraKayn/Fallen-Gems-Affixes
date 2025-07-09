package net.kayn.fallen_gems_affixes.mixin.permanent_effect;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.onHotBarSelectedChange;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Shadow
    @Final
    public Player player;

    /**
     * The load method is invoked when {@link ServerPlayer} enters the world first time.
     * <p>
     * This method is server side only.
     */
    @Inject(method = "load", at = @At("TAIL"))
    private void onLoad(ListTag pListTag, CallbackInfo ci) {
        if (!(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        try {
            // Exclude the first slot to fix the login issue.
            // 1 means start from offhand.
            int index = 1;
            for (ItemStack equipment : EquipmentSlotUtil.getOffHandAndArmors(player)) {
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

    /**
     * This method triggers when use keybind to change the hot bar selected slot.
     * This method only triggers on clientside.
     */
    @OnlyIn(Dist.CLIENT)
    @Inject(method = "swapPaint", at = @At("TAIL"))
    private void swapPaint(double pDirection, CallbackInfo ci) {
        if (player != null) {
            onHotBarSelectedChange(player);
        }
    }
}
