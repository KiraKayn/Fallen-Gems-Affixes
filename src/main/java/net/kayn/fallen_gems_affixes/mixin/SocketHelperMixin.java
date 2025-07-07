package net.kayn.fallen_gems_affixes.mixin;

import com.google.common.collect.ImmutableList;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper.getSockets;

@Mixin(value = SocketHelper.class, remap = false)
public class SocketHelperMixin {
    @Inject(method = {"getGemsImpl"}, at = {@At("HEAD")}, cancellable = true)
    private static void getGemsImplTweak(ItemStack stack, CallbackInfoReturnable<SocketedGems> cir) {
        int size = getSockets(stack);
        if (size > 0 && !stack.isEmpty()) {
            LootCategory cat = LootCategory.forItem(stack);
            if (cat.isNone()) {
                cir.setReturnValue(SocketedGems.EMPTY);
            } else {
                List<GemInstance> gems = NonNullList.withSize(size, GemInstance.EMPTY);
                int i = 0;
                CompoundTag afxData = stack.getTagElement("affix_data");
                if (afxData != null && afxData.contains("gems")) {
                    for(Tag tag : afxData.getList("gems", 10)) {
                        ItemStack gemStack = ItemStack.of((CompoundTag)tag);
                        gemStack.setCount(1);
                        GemInstance inst = GemInstance.socketed(stack, gemStack);
                        if (inst.isValid()) {
                            gems.set(i++, inst);
                        }
                        else {
                            i++;
                        }
                        if (i >= size) {
                            break;
                        }
                    }
                }

                cir.setReturnValue(new SocketedGems(ImmutableList.copyOf(gems)));
            }
        } else {
            cir.setReturnValue(SocketedGems.EMPTY);
        }
    }
}