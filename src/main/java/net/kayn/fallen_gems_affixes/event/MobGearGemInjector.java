package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.compat.GameStagesCompat;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = "fallen_gems_affixes")
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
        Player player = level.getNearestPlayer(le, 50);
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
                    ItemStack gemStack;
                    AtomicReference<Purity> purity = new AtomicReference<>();
                    if (player != null) {
                        GenContext genContext = new GenContext(player.getRandom(), WorldTier.getTier(player), player.getLuck(), level.dimension(), level.getBiome(le.blockPosition()), GameStagesCompat.getStages(player));
                        Gem gem = GemRegistry.INSTANCE.getRandomItem(genContext, g -> {
                            purity.set(Purity.random(genContext));
                            var gemBonus = g.getBonus(category, purity.get());
                            return gemBonus.isPresent();
                        });
                        if (gem != null) {
                            gemStack = gem.toStack(purity.get());
                        } else {
                            gemStack = ItemStack.EMPTY;
                        }
                    } else {
                        gemStack = GemRegistry.createRandomGemStack(GenContext.dummy(random));
                    }
                    if (!gemStack.isEmpty()) {
                        GemInstance instance = GemInstance.socketed(stack, gemStack, i);
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