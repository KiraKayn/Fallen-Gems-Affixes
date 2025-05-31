package net.kayn.fallen_gems_affixes.init.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class GemLootModifier extends LootModifier {

    public static final Supplier<Codec<GemLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).and(Codec.STRING.fieldOf("gem_type").forGetter(m -> m.gemType)).and(Codec.FLOAT.fieldOf("drop_chance").forGetter(m -> m.dropChance)).apply(inst, GemLootModifier::new)));

    private final String gemType;
    private final float dropChance;

    public GemLootModifier(LootItemCondition[] conditionsIn, String gemType, float dropChance) {
        super(conditionsIn);
        this.gemType = gemType;
        this.dropChance = dropChance;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() <= dropChance) {
            ItemStack gemStack = createApotheosisGem(gemType);
            if (!gemStack.isEmpty()) {
                generatedLoot.add(gemStack);
            }
        }
        return generatedLoot;
    }

    private ItemStack createApotheosisGem(String gemType) {
        Item gemItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "apotheosis_gem"));
        if (gemItem == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(gemItem);
        CompoundTag nbt = new CompoundTag();

        nbt.putString("gem_type", gemType);

        stack.setTag(nbt);
        return stack;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}