package net.kayn.fallen_gems_affixes.init.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kayn.fallen_gems_affixes.adventure.socket.GemSocketInjectionHelper;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SocketGemModifier extends LootModifier {

    public static final Codec<SocketGemModifier> CODEC = RecordCodecBuilder.create(inst ->
            LootModifier.codecStart(inst).apply(inst, SocketGemModifier::new)
    );

    public SocketGemModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!ModConfig.ENABLE_SOCKET_GEM_MODIFIER.get()) return generatedLoot;

        RandomSource rand   = context.getRandom();
        ServerLevel level   = context.getLevel();

        for (ItemStack stack : generatedLoot) {
            if (stack.isEmpty()) continue;

            int sockets = SocketHelper.getSockets(stack);
            if (sockets <= 0) continue;

            LootCategory category = LootCategory.forItem(stack);
            if (category == null || category.isNone()) continue;

            List<GemInstance> gems = new ArrayList<>(Collections.nCopies(sockets, GemInstance.EMPTY));

            for (int i = 0; i < sockets; i++) {
                gems.set(i, GemSocketInjectionHelper.rollGemForSocket(stack, category, i, rand, level));
            }

            SocketHelper.setGems(stack, new SocketedGems(gems));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}