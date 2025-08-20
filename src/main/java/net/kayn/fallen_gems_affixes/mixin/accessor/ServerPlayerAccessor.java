package net.kayn.fallen_gems_affixes.mixin.accessor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ServerPlayer.class, remap = false)
public interface ServerPlayerAccessor {

    @Accessor("levitationStartPos")
    void setLevitationStartPos(Vec3 levitationStartPos);
}
