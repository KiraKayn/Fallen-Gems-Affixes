package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.socket.AddSocketsRecipe;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AddSocketsRecipe.class, remap = false)
public class TieredAddSocketsRecipeMixin {

    @Inject(method = "assemble", at = @At("RETURN"))
    private void markNewSocketAsRegular(Container pInv, RegistryAccess regs,
                                        CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (!result.isEmpty()) {
            TieredSocketHelper.addRegularSocket(result);
        }
    }
}