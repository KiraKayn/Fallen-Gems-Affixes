package net.kayn.fallen_gems_affixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootRule;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LootRule.SocketLootRule.class, remap = false)
public class LootControllerMixin {

    @WrapOperation(
            method = "execute",
            at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/apotheosis/socket/SocketHelper;getSockets(Lnet/minecraft/world/item/ItemStack;)I")
    )
    private int fga$readStoredSockets(ItemStack stack, Operation<Integer> original) {
        return stack.getOrDefault(Apoth.Components.SOCKETS, 0);
    }
}