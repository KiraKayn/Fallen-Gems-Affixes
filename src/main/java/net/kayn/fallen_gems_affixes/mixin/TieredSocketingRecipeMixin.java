package net.kayn.fallen_gems_affixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketMode;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SocketingRecipe.class, remap = false)
public class TieredSocketingRecipeMixin {

    private static final int SLOT_BASE     = 1;
    private static final int SLOT_ADDITION = 2;

    @Inject(method = "matches", at = @At("RETURN"), cancellable = true)
    private void checkTieredCompatibility(Container inv, Level level,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;

        TieredSocketMode mode = ModConfig.TIERED_SOCKET_MODE.get();

        ItemStack input    = inv.getItem(SLOT_BASE);
        ItemStack gemStack = inv.getItem(SLOT_ADDITION);
        GemInstance gem    = GemInstance.unsocketed(gemStack);
        if (!gem.isValidUnsocketed()) return;

        if (!TieredSocketHelper.hasCompatibleEmptySocket(input, gem, mode)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(
            method = "assemble",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/shadowsoffire/apotheosis/adventure/socket/SocketHelper;"
                            + "getFirstEmptySocket(Lnet/minecraft/world/item/ItemStack;)I"
            ),
            remap = false
    )
    private int redirectToFirstCompatibleSocket(
            ItemStack result, Operation<Integer> original,
            @Local(argsOnly = true) Container inv,
            @Local(argsOnly = true) RegistryAccess regs) {

        TieredSocketMode mode = ModConfig.TIERED_SOCKET_MODE.get();

        ItemStack gemStack = inv.getItem(SLOT_ADDITION);
        GemInstance gem    = GemInstance.unsocketed(gemStack);
        if (!gem.isValidUnsocketed()) return original.call(result);

        int compatible = TieredSocketHelper.getFirstCompatibleEmptySocket(result, gem, mode);
        return compatible >= 0 ? compatible : original.call(result);
    }
}