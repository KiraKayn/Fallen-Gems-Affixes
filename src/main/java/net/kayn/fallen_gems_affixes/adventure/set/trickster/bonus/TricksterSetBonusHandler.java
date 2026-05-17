package net.kayn.fallen_gems_affixes.adventure.set.trickster.bonus;

import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.SetBonusHandler;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.*;
import net.kayn.fallen_gems_affixes.event.ShadowCloneDeathEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class TricksterSetBonusHandler {
    private static final String PIECE_COUNT_KEY = "fga.trickster_bonus_pieces";
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("a2c4b6d8-1234-5678-9abc-def012345678");

    public static void onPieceCountChanged(Player player, int newCount) {
        int lastCount = player.getPersistentData().getInt(PIECE_COUNT_KEY);
        if (lastCount == newCount) return;
        player.getPersistentData().putInt(PIECE_COUNT_KEY, newCount);
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;

        int pieces = SetBonusHandler.getSetPieceCount(player, TricksterSetConstants.SET_ID);
        if (pieces < 3) {
            removeSpeedModifier(player);
            return;
        }

        SetAffix weaponAffix = SetAffixHelper.getSetAffix(player.getMainHandItem());
        if (!(weaponAffix instanceof TricksterWeaponAffix wa)) {
            removeSpeedModifier(player);
            return;
        }

        int clones = ShadowCloneManager.getCloneCount(player);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        speedAttr.removeModifier(SPEED_MODIFIER_UUID);
        if (clones > 0) {
            double bonus = clones * wa.getThreePieceSpeedPerClone();
            speedAttr.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_UUID,
                    "trickster_set_speed", bonus, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    private static void removeSpeedModifier(Player player) {
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_MODIFIER_UUID);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDodge(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        int pieces = SetBonusHandler.getSetPieceCount(player, TricksterSetConstants.SET_ID);
        if (pieces < 3) return;

        SetAffix wa = SetAffixHelper.getSetAffix(player.getMainHandItem());
        if (!(wa instanceof TricksterWeaponAffix weaponAffix)) return;

        int clones = ShadowCloneManager.getCloneCount(player);
        float dodgeChance = clones * weaponAffix.getThreePieceDodgePerClone();
        if (player.getRandom().nextFloat() < dodgeChance) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCloneDeath(ShadowCloneDeathEvent event) {
        Player owner = event.getOwner();
        if (owner == null || owner.level().isClientSide) return;

        int pieces = SetBonusHandler.getSetPieceCount(owner, TricksterSetConstants.SET_ID);
        if (pieces < 4) return;

        SetAffix leggingsAffix = SetAffixHelper.getSetAffix(owner.getItemBySlot(EquipmentSlot.LEGS));
        if (!(leggingsAffix instanceof TricksterLeggingsAffix la)) return;

        int reduction = la.getFourPieceCooldownReductionTicks();
        TricksterCooldownHelper.reduceCooldown(owner, TricksterCooldownHelper.CHESTPLATE_CD, reduction);
        TricksterCooldownHelper.reduceCooldown(owner, TricksterCooldownHelper.BOOTS_CD, reduction);
        TricksterCooldownHelper.reduceCooldown(owner, TricksterCooldownHelper.WEAPON_CD, reduction);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        int pieces = SetBonusHandler.getSetPieceCount(player, TricksterSetConstants.SET_ID);
        if (pieces < 5) return;

        SetAffix wa = SetAffixHelper.getSetAffix(player.getMainHandItem());
        if (!(wa instanceof TricksterWeaponAffix weaponAffix)) return;

        if (player.getRandom().nextFloat() < weaponAffix.getFivePieceKillRefreshChance()) {
            TricksterCooldownHelper.clearCooldown(player, TricksterCooldownHelper.CHESTPLATE_CD);
        }
    }
}