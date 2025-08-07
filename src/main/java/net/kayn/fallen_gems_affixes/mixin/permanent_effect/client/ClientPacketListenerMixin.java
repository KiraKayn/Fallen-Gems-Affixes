package net.kayn.fallen_gems_affixes.mixin.permanent_effect.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.onHotBarSelectedChange;

@Mixin(value = ClientPacketListener.class, remap = false)
@OnlyIn(Dist.CLIENT)
public class ClientPacketListenerMixin {
    @Inject(method = "handleSetCarriedItem", at = @At("TAIL"))
    private void onSetCarriedItem(ClientboundSetCarriedItemPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            Player player = mc.player;
            if (player != null) {
                onHotBarSelectedChange(player);
            }
        }
    }
}
