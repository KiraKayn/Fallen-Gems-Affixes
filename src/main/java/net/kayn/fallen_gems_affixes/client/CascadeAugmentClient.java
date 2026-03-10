package net.kayn.fallen_gems_affixes.client;

import net.kayn.fallen_gems_affixes.augment.CascadeAugment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class CascadeAugmentClient {

    @Nullable
    public static CascadeAugment.CascadeData getEffectiveData(CascadeAugment.CascadeData base) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return null;
        return CascadeAugment.computeEffective(base, player);
    }
}