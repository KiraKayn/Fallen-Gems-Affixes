package net.kayn.fallen_gems_affixes.mixin.permanent_effect.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.onHotBarSelectedChange;

@Mixin(Minecraft.class)
@OnlyIn(Dist.CLIENT)
public class MinecraftMixin {

    /**
     * This method triggers when use mouse scroll to change the hot bar selected slot.
     * This method only triggers on clientside.
     */
    @Inject(method = "handleKeybinds", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Inventory;selected:I",
            opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void onHotbarSlotChanged(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        Player player = mc.player;
        if (player != null) {
            onHotBarSelectedChange(player);
        }
    }
}
