package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.event.GetItemSocketsEvent;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.augment.MaliceAugment;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes")
public class SocketEvents {

    @SubscribeEvent
    public static void onItemCrafted(net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
        MaliceAugment.revealIfPending(event.getCrafting());
    }

    @SubscribeEvent
    public static void onGetSockets(GetItemSocketsEvent event) {
        var stack = event.getStack();

        CompoundTag afxData = stack.getTagElement(SocketHelper.AFFIX_DATA);
        if (afxData != null && afxData.contains(SocketHelper.SOCKETS)) return;

        LootCategory cat = LootCategory.forItem(stack);
        if (cat.isNone()) return;

        int extra = ModConfig.EXTRA_SOCKETS.get();
        if (extra <= 0) return;

        event.setSockets(event.getSockets() + extra);
    }
}