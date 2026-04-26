package net.kayn.fallen_gems_affixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootController;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.kayn.fallen_gems_affixes.adventure.socket.CatalystSocketHelper;
import net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper;
import net.kayn.fallen_gems_affixes.recipe.ErasureRecipe;   // <-- added import
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LootController.class, remap = false)
public class LootControllerMixin {

    @WrapOperation(
            method = "createLootItem(Lnet/minecraft/world/item/ItemStack;Ldev/shadowsoffire/apotheosis/adventure/loot/LootCategory;Ldev/shadowsoffire/apotheosis/adventure/loot/LootRarity;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/apotheosis/adventure/socket/SocketHelper;getSockets(Lnet/minecraft/world/item/ItemStack;)I")
    )
    private static int getBaseSocketsOnly(ItemStack stack, Operation<Integer> original) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        return afxData != null ? afxData.getInt("sockets") : 0;
    }

    @Inject(
            method = "createLootItem(Lnet/minecraft/world/item/ItemStack;Ldev/shadowsoffire/apotheosis/adventure/loot/LootCategory;Ldev/shadowsoffire/apotheosis/adventure/loot/LootRarity;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private static void onLootItemCreated(
            ItemStack stack, LootCategory cat, LootRarity rarity, RandomSource rand,
            CallbackInfoReturnable<ItemStack> cir) {

        ItemStack result = cir.getReturnValue();
        if (!result.isEmpty()) {
            ErasureRecipe.removeScrollAffixes(result);

            if (CatalystSocketHelper.hasCatalystSocket(result)) return;
            TieredSocketHelper.assignSocketTiersToLootItem(result, rand);
        }
    }
}