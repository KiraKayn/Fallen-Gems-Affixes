package net.kayn.fallen_gems_affixes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.item.AffixScrollItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.stream.Stream;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

public class ModCommands {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_APPLICABLE_AUGMENT = (ctx, builder) -> {
        Entity entity = ctx.getSource().getEntity();
        if (entity instanceof ServerPlayer) {
            Stream<String> suggestions = AugmentRegistry.BY_ID.keySet().stream().map(ResourceLocation::toString);
            return SharedSuggestionProvider.suggest(suggestions, builder);
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    };

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    RarityRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_AFFIX = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    AffixRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("fallen_gems_affixes")
                .requires(c -> c.hasPermission(2));

        // augment add
        builder.then(Commands.literal("augment")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("Augment", ResourceLocationArgument.id()).suggests(SUGGEST_APPLICABLE_AUGMENT)
                                .then(Commands.argument("ExtraData", FloatArgumentType.floatArg(1f))
                                        .executes(c -> addAugmentToMainHandItem(c, FloatArgumentType.getFloat(c, "ExtraData"))))
                                .executes(c -> addAugmentToMainHandItem(c, 1f)))));

        // augment clear
        builder.then(Commands.literal("clearAugment")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ModCommands::clearAugments)));

        // scroll give
        builder.then(Commands.literal("scroll")
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rarity", ResourceLocationArgument.id()).suggests(SUGGEST_RARITY)
                                        .then(Commands.argument("affix", ResourceLocationArgument.id()).suggests(SUGGEST_AFFIX)
                                                .then(Commands.argument("level", FloatArgumentType.floatArg(0, 1))
                                                        .executes(c -> giveScroll(c,
                                                                ResourceLocationArgument.getId(c, "rarity"),
                                                                ResourceLocationArgument.getId(c, "affix"),
                                                                FloatArgumentType.getFloat(c, "level"))))
                                                .executes(c -> giveScroll(c,
                                                        ResourceLocationArgument.getId(c, "rarity"),
                                                        ResourceLocationArgument.getId(c, "affix"),
                                                        0.5f)))))));

        dispatcher.register(builder);
    }

    private static int giveScroll(CommandContext<CommandSourceStack> ctx,
                                  ResourceLocation rarityId,
                                  ResourceLocation affixId,
                                  float level) throws CommandSyntaxException {
        DynamicHolder<LootRarity> rarityHolder = RarityRegistry.INSTANCE.holder(rarityId);
        if (!rarityHolder.isBound()) {
            ctx.getSource().sendFailure(Component.literal("Unknown rarity: " + rarityId));
            return -1;
        }

        DynamicHolder<Affix> affixHolder = AffixRegistry.INSTANCE.holder(affixId);
        if (!affixHolder.isBound()) {
            ctx.getSource().sendFailure(Component.literal("Unknown affix: " + affixId));
            return -2;
        }

        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ItemStack scroll = AffixScrollItem.createScroll(affixId, rarityHolder.get(), level);

        if (!player.addItem(scroll)) {
            player.drop(scroll, false);
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Gave " + player.getName().getString() +
                        " a scroll: " + affixId.getPath() +
                        " (" + rarityId.getPath() + ", level " + String.format("%.2f", level) + ")"), true);
        return 1;
    }

    private static int clearAugments(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Player has no item in mainhand."));
            return 0;
        } else if (stack.hasTag() && stack.getTag().contains(AUGMENT_DATA)) {
            CompoundTag augmentData = stack.getTagElement(AUGMENT_DATA);
            ListTag augments = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);
            augments.clear();
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("Cleared mainhand augments for " + player.getName().getString()),
                false);
        return 1;
    }

    private static int addAugmentToMainHandItem(CommandContext<CommandSourceStack> ctx, float extraData) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ResourceLocation augID = ctx.getArgument("Augment", ResourceLocation.class);
        IAugment augment = augID != null ? AugmentRegistry.get(augID) : null;

        if (augment == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown Augment"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Player has no item in mainhand."));
            return 0;
        }

        boolean hasTag = stack.hasTag() && stack.getTag().contains(AUGMENT_DATA);
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag augmentData = tag.getCompound(AUGMENT_DATA);
        ListTag augments = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);

        for (int i = 0; i < augments.size(); i++) {
            CompoundTag aug = augments.getCompound(i);
            String type = aug.getString(TYPE);
            ResourceLocation id = ResourceLocation.tryParse(type);
            if (id != null) {
                IAugment existing = AugmentRegistry.get(id);
                if (existing != null && augID.equals(id) && augment.isUnique()) {
                    ctx.getSource().sendFailure(Component.literal("Failed adding Augment, Augment is unique."));
                    return 0;
                }
            }
        }

        CompoundTag augTag = new CompoundTag();
        CompoundTag innerData = new CompoundTag();
        augTag.putString(TYPE, augID.toString());
        if (augment == Fallen.Augments.GEM_POWER) {
            innerData.putFloat("power", extraData);
        }
        augTag.put(INNER_DATA, innerData);
        augments.add(augTag);

        if (!hasTag) {
            augmentData.put(AUGMENTS, augments);
            tag.put(AUGMENT_DATA, augmentData);
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("Added Augment " + augID + " to " + player.getName().getString()),
                false);
        return 1;
    }
}