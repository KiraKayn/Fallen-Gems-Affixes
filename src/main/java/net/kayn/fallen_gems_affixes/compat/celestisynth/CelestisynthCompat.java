package net.kayn.fallen_gems_affixes.compat.celestisynth;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.thecelestialworkshop.celestisynth.common.entity.projectile.RainfallArrow;
import org.thecelestialworkshop.celestisynth.common.item.weapons.RainfallSerenityItem;

public final class CelestisynthCompat {

    private CelestisynthCompat() {}

    public static boolean isRainfallSerenity(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof RainfallSerenityItem;
    }

    public static AbstractArrow spawnRainfallArrow(ServerLevel level, Player owner) {
        RainfallArrow arrow = new RainfallArrow(level, owner);
        arrow.setOwner(owner);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        arrow.setOrigin(owner.getEyePosition());
        arrow.setImbueQuasar(true);
        arrow.setStrong(true);
        return arrow;
    }
}