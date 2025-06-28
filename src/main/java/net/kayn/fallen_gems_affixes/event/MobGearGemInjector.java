package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
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

    private static final String BOSS_KEY = "apoth.boss";

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!ModConfig.ENABLE_SOCKET_GEM_MODIFIER.get()) return;

        if (!(event.getEntity() instanceof LivingEntity le)) return;

        Level level = event.getLevel();
        if (level.isClientSide()) return;

        CompoundTag tag = le.getPersistentData();
        if (!tag.getBoolean(BOSS_KEY)) return;

        RandomSource random = level.getRandom();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = le.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            int sockets = SocketHelper.getSockets(stack);
            if (sockets <= 0) continue;

            LootCategory category = LootCategory.forItem(stack);
            if (category == null || category.isNone()) continue;

            List<GemInstance> gems = new ArrayList<>(Collections.nCopies(sockets, GemInstance.EMPTY));

            for (int i = 0; i < sockets; i++) {
                GemInstance selected = GemInstance.EMPTY;

                if (random.nextFloat() <= ModConfig.SOCKET_GEM_CHANCE.get()) {
                    ItemStack gemStack = GemRegistry.createRandomGemStack(random, (ServerLevel) level, 1.0F, g ->
                            g.getBonuses().stream()
                                    .anyMatch(bonus -> bonus.getGemClass() != null && bonus.getGemClass().types().contains(category))
                    );

                    if (!gemStack.isEmpty()) {
                        GemInstance instance = GemInstance.socketed(stack, gemStack);
                        if (instance.isValid()) {
                            selected = instance;
                        }
                    }
                }

                gems.set(i, selected);
            }

            SocketHelper.setGems(stack, new SocketedGems(gems));
        }
    }
}