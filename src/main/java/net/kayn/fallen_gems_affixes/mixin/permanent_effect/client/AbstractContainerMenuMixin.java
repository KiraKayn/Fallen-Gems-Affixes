package net.kayn.fallen_gems_affixes.mixin.permanent_effect.client;

import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotWrapper;
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;

@Mixin(AbstractContainerMenu.class)
@OnlyIn(Dist.CLIENT)
public class AbstractContainerMenuMixin {
    /**
     * This method {@link AbstractContainerMenu#initializeContents} is triggered whenever a clientside container receives {@link ClientboundContainerSetContentPacket}.
     * <p>
     * Server will send this packet when {@link LocalPlayer} is joining the world after {@link ServerPlayer} finished {@link Inventory#load(ListTag)}
     * <p>
     * Server will send this packet when valid container contents change is admitted, like change slot for an item stack.
     * <p>
     * This method is for initializing {@code currentPermanentEffects} inside {@code ProtectedMobEffectMap} on client first tick.
     * <p>
     * This method only triggers on clientside.
     */
    @Inject(method = "initializeContents", at = @At("TAIL"))
    private void initializeContentsSuffix(int pStateId, List<ItemStack> pItems, ItemStack pCarried, CallbackInfo ci) {
        if (!((Object) this instanceof InventoryMenu menu)) return;
        ProtectedMobEffectMap<?> map1 = null;
        try {
            for (int i = 0; i < 4; i++) {
                Slot slot = menu.getSlot(40 - i);
                if (slot == null) continue;
                var container = slot.container;
                if (container instanceof Inventory inventory) {
                    Player player = inventory.player;
                    if (player == null) return;
                    if (!(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
                    map1 = map;
                    int index = 0;
                    for (ItemStack equipment : player.getAllSlots()) {
                        EquipmentSlot eSlot = EquipmentSlotUtil.slotFromAllSlotsIndex(index++);
                        if (eSlot == null) continue;
                        EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(eSlot);
                        if (slotWrapper == null) continue;
                        map.initOperation(slotWrapper, ProtectedMobEffectMap.EffectOperator.ON_INIT);

                        if (map.getLastEffectsProvider() != equipment && EquipmentSlotUtil.matchesSlot(equipment, eSlot)) {
                            checkGemBonus(equipment, (bonus, rarity) -> {
                                MobEffect effect = bonus.getEffect();
                                int amplifier = bonus.getAmplifier(rarity);
                                MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                                player.forceAddEffect(inst, null);
                                map.setLastEffectsProvider(equipment);
                            });
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (map1 != null) {
                map1.finalizeOperation();
            }
        }
    }
}
