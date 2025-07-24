package net.kayn.fallen_gems_affixes.mixin;

import com.google.common.collect.ImmutableList;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static dev.shadowsoffire.apotheosis.socket.SocketHelper.getSockets;

@Mixin(value = SocketHelper.class, remap = false)
public class SocketHelperMixin {
    @Inject(method = {"getGemsImpl"}, at = {@At("HEAD")}, cancellable = true)
    private static void getGemsImplTweak(ItemStack stack, CallbackInfoReturnable<SocketedGems> cir) {
//        int size = getSockets(stack);
//        if (size <= 0 || stack.isEmpty()) {
//            cir.setReturnValue(SocketedGems.EMPTY);
//        }
//
//        LootCategory cat = LootCategory.forItem(stack);
//        if (cat.isNone()) {
//            cir.setReturnValue(SocketedGems.EMPTY);
//        }
//
//        NonNullList<GemInstance> list = NonNullList.withSize(size, GemInstance.EMPTY);
//        ItemContainerContents socketedGems = stack.getOrDefault(Apoth.Components.SOCKETED_GEMS, ItemContainerContents.EMPTY);
//
//        for (int i = 0; i < Math.min(size, socketedGems.getSlots()); i++) {
//            ItemStack gem = socketedGems.getStackInSlot(i);
//            if (!gem.isEmpty()) {
//                gem.setCount(1);
//                GemInstance inst = GemInstance.socketed(stack, gem, i);
//                list.set(i, inst);
//            }
//        }
//
//        cir.setReturnValue(new SocketedGems(list));
    }
}