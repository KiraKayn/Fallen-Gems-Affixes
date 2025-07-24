package net.kayn.fallen_gems_affixes.init.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SocketGemModifier extends LootModifier {

    public static final MapCodec<SocketGemModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).apply(inst, SocketGemModifier::new)
    );

    public SocketGemModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!ModConfig.ENABLE_SOCKET_GEM_MODIFIER.get()) return generatedLoot;

        RandomSource rand = context.getRandom();
        ServerLevel level = context.getLevel();
        Player player = GenContext.findPlayer(context);
        for (ItemStack stack : generatedLoot) {
            if (stack.isEmpty()) continue;

            int sockets = SocketHelper.getSockets(stack);
            if (sockets <= 0) continue;

            LootCategory category = LootCategory.forItem(stack);
            if (category == null || category.isNone()) continue;

            List<GemInstance> gems = new ArrayList<>(Collections.nCopies(sockets, GemInstance.EMPTY));

            for (int i = 0; i < sockets; i++) {
                GemInstance selected = GemInstance.EMPTY;

                if (rand.nextFloat() <= ModConfig.SOCKET_GEM_CHANCE.get()) {
                    ItemStack gemStack;
                    AtomicReference<Purity> purity = new AtomicReference<>();
                    if (player != null) {
                        GenContext genContext = new GenContext(player.getRandom(), WorldTier.getTier(player), player.getLuck(), level.dimension(), level.getBiome(player.blockPosition()), GameStagesCompat.getStages(player));
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
                        gemStack = GemRegistry.createRandomGemStack(GenContext.dummy(rand));
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

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}