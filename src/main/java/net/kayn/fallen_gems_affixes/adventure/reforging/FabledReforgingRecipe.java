package net.kayn.fallen_gems_affixes.adventure.reforging;

import com.google.gson.JsonObject;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record FabledReforgingRecipe(ResourceLocation id, int matCost, int sigilCost, int levelCost) implements Recipe<Container> {

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return FabledReforging.FABLED_REFORGING_TYPE.get();
    }

    @Override
    @Deprecated
    public boolean matches(Container pContainer, Level pLevel) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack assemble(Container pContainer, RegistryAccess regs) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack getResultItem(RegistryAccess regs) {
        return ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<FabledReforgingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = FallenGemsAffixes.id("fabled_reforging");

        @Override
        public FabledReforgingRecipe fromJson(ResourceLocation id, JsonObject obj) {
            int matCost = GsonHelper.getAsInt(obj, "material_cost", 1);
            int sigilCost = GsonHelper.getAsInt(obj, "sigil_cost", 0);
            int levelCost = GsonHelper.getAsInt(obj, "level_cost", 0);
            return new FabledReforgingRecipe(id, matCost, sigilCost, levelCost);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FabledReforgingRecipe recipe) {
            buf.writeVarInt(recipe.matCost);
            buf.writeVarInt(recipe.sigilCost);
            buf.writeVarInt(recipe.levelCost);
        }

        @Override
        public FabledReforgingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new FabledReforgingRecipe(id, buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }
    }
}