package net.kayn.fallen_gems_affixes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.context.CommandContext;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.stream.Stream;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

public class AugmentCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("fallen_gems_affixes").requires(c -> c.hasPermission(2));
        builder.then(Commands.literal("augment")
                .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("Augment", ResourceLocationArgument.id()).suggests(SUGGEST_APPLICABLE_AUGMENT)
                        .then(Commands.argument("ExtraData", FloatArgumentType.floatArg(1f))
                                .executes(c -> addAugmentToMainHandItem(c, FloatArgumentType.getFloat(c, "ExtraData"))))
                        .executes(c -> addAugmentToMainHandItem(c, 1f)))));
        builder.then(Commands.literal("clearAugment")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(AugmentCommands::clearAugments)));
        dispatcher.register(builder);
    }
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_APPLICABLE_AUGMENT = (ctx, builder) -> {
        Entity entity = ctx.getSource().getEntity();
        if (entity instanceof ServerPlayer) {
            Stream<String> suggestions = AugmentRegistry.BY_ID.keySet().stream().map(ResourceLocation::toString);
            return SharedSuggestionProvider.suggest(suggestions, builder);
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    };

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
                () -> Component.literal("Cleared mainhand augments for" + player.getName().getString()),
                false
        );
        return 1;
    }

    private static int addAugmentToMainHandItem(CommandContext<CommandSourceStack> ctx, float extraData) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ResourceLocation augID = ctx.getArgument("Augment", ResourceLocation.class);
        IAugment augment;
        if (augID != null) {
            augment = AugmentRegistry.get(augID);
        } else {
            augment = null;
        }
        if (augment == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown Augment"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Player has no item in mainhand."));
            return 0;
        } else {
            // Main logic part.
            boolean hasTag = stack.hasTag() && stack.getTag().contains(AUGMENT_DATA);
            CompoundTag tag = stack.getOrCreateTag();
            CompoundTag augmentData = tag.getCompound(AUGMENT_DATA);
            ListTag augments = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < augments.size(); i++) {
                CompoundTag aug = augments.getCompound(i);
                String type = aug.getString(TYPE);
                ResourceLocation id = ResourceLocation.tryParse(type);
                if (id != null) {
                    IAugment augment1 = AugmentRegistry.get(id);
                    if (augment1 != null) {
                        if (augID.equals(id) && augment.isUnique()) {
                            ctx.getSource().sendFailure(Component.literal("Failed adding Augment, Augment is unique."));
                            return 0;
                        }
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
            // End
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("Added Augment " + augID + " to " + player.getName().getString()),
                false
        );

        return 1;
    }
}