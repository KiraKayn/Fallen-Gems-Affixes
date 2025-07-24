package net.kayn.fallen_gems_affixes.init.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixConvertLootModifier;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class GemLootModifier extends LootModifier {

    public static final Supplier<MapCodec<GemLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(GemEntry.CODEC.listOf().fieldOf("gems").forGetter(m -> m.gems))
                    .apply(inst, GemLootModifier::new)));

    private final List<GemEntry> gems;

    public GemLootModifier(LootItemCondition[] conditionsIn, List<GemEntry> gems) {
        super(conditionsIn);
        this.gems = gems;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        RandomSource rand = context.getRandom();

        float totalWeight = 0f;
        for (GemEntry entry : gems) {
            totalWeight += entry.drop_chance();
        }

        if (totalWeight <= 0f) return generatedLoot;

        float roll = rand.nextFloat() * totalWeight;
        float cumulative = 0f;

        for (GemEntry entry : gems) {
            cumulative += entry.drop_chance();
            if (roll <= cumulative) {
                ItemStack stack = createApotheosisGem(entry);
                if (!stack.isEmpty()) {
                    generatedLoot.add(stack);
                }
                break;
            }
        }

        return generatedLoot;
    }

    private ItemStack createApotheosisGem(GemEntry entry) {

        Item gemItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("apotheosis", "gem"));
        if (gemItem == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(gemItem);

        stack.set(Apoth.Components.PURITY, Purity.valueOf(entry.purity));
        stack.set(Apoth.Components.GEM, GemRegistry.INSTANCE.holder(ResourceLocation.tryParse(entry.gem)));

        return stack;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }

    public record GemEntry(String gem, String purity, float drop_chance) {
        public static final Codec<GemEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                        Codec.STRING.fieldOf("gem").forGetter(GemEntry::gem),
                        Codec.STRING.fieldOf("purity").forGetter(GemEntry::purity),
                        Codec.FLOAT.fieldOf("drop_chance").forGetter(GemEntry::drop_chance))
                .apply(inst, GemEntry::new));

    }
}