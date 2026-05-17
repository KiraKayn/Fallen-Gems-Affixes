package net.kayn.fallen_gems_affixes.adventure.set.trickster;

import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.entity.ShadowCloneEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class ShadowCloneManager {
    private static final double SPAWN_RADIUS = 2.5;
    private static final double SEARCH_RADIUS = 64.0;

    public static List<ShadowCloneEntity> getClones(Player player) {
        if (player.level().isClientSide) return List.of();
        AABB area = player.getBoundingBox().inflate(SEARCH_RADIUS);
        return player.level().getEntitiesOfClass(ShadowCloneEntity.class, area, clone -> {
            Player owner = clone.getOwner();
            return owner != null && owner.getUUID().equals(player.getUUID());
        });
    }

    public static int getCloneCount(Player player) {
        return getClones(player).size();
    }

    public static void spawnClonesAtCircle(Player player, int count) {
        if (count <= 0) return;
        Level level = player.level();
        if (level.isClientSide) return;

        int pieces = net.kayn.fallen_gems_affixes.adventure.set.SetBonusHandler.getSetPieceCount(player, TricksterSetConstants.SET_ID);
        boolean useHealth = pieces >= 2;

        float cloneHealth = 1.0f;
        if (useHealth) {
            float healthFraction = 0.25f;
            net.kayn.fallen_gems_affixes.adventure.set.SetAffix chestAffix = SetAffixHelper.getSetAffix(
                    player.getItemBySlot(EquipmentSlot.CHEST));
            if (chestAffix instanceof TricksterChestplateAffix ca) {
                healthFraction = ca.getTwoPieceCloneHealthFraction();
            }
            cloneHealth = (float) (player.getAttributeValue(Attributes.MAX_HEALTH) * healthFraction);
        }

        double angleStep = count > 0 ? (2.0 * Math.PI / count) : 0;

        for (int i = 0; i < count; i++) {
            double angle = angleStep * i;
            double offsetX = SPAWN_RADIUS * Math.cos(angle);
            double offsetZ = SPAWN_RADIUS * Math.sin(angle);

            ShadowCloneEntity clone = new ShadowCloneEntity(TricksterEntities.SHADOW_CLONE.get(), level);
            clone.setOwnerUUID(player.getUUID());
            clone.setDieOnHit(!useHealth);
            clone.setOffset(offsetX, 0.0, offsetZ);

            if (useHealth) {
                clone.getAttribute(Attributes.MAX_HEALTH).setBaseValue(cloneHealth);
                clone.setHealth(cloneHealth);
            }

            clone.setPos(player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
            clone.setYRot(player.getYRot());
            level.addFreshEntity(clone);
        }
    }

    public static void removeAllClones(Player player) {
        getClones(player).forEach(c -> {
            c.fireDeathEvent();
            c.discard();
        });
    }

    private ShadowCloneManager() {}
}