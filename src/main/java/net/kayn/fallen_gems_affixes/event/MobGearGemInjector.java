package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import net.kayn.fallen_gems_affixes.adventure.socket.GemSocketInjectionHelper;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Mod.EventBusSubscriber(modid = "fallen_gems_affixes")
public class MobGearGemInjector {

    private static final String BOSS_KEY     = "apoth.boss";
    private static final String INJECTED_KEY = "fga.gem_injected";

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!ModConfig.ENABLE_SOCKET_GEM_MODIFIER.get()) return;
        if (!(event.getEntity() instanceof LivingEntity le)) return;

        Level level = event.getLevel();
        if (level.isClientSide()) return;

        CompoundTag tag = le.getPersistentData();
        if (!tag.getBoolean(BOSS_KEY) || tag.getBoolean(INJECTED_KEY)) return;

        RandomSource rand    = level.getRandom();
        ServerLevel sLevel   = (ServerLevel) level;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = le.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            int sockets = SocketHelper.getSockets(stack);
            if (sockets <= 0) continue;

            LootCategory category = LootCategory.forItem(stack);
            if (category == null || category.isNone()) continue;

            List<GemInstance> gems = new ArrayList<>(Collections.nCopies(sockets, GemInstance.EMPTY));

            for (int i = 0; i < sockets; i++) {
                gems.set(i, GemSocketInjectionHelper.rollGemForSocket(stack, category, i, rand, sLevel));
            }

            SocketHelper.setGems(stack, new SocketedGems(gems));
        }

        tag.putBoolean(INJECTED_KEY, true);
    }
}