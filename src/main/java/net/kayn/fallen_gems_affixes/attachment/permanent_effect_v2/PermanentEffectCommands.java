package net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kayn.fallen_gems_affixes.network.ClientlikeClearPermanentEffectPacket;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;

public class PermanentEffectCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("fallen_debug")
                        .requires(sourceStack -> sourceStack.hasPermission(2))
                        .then(
                                Commands.literal("clearPermanentEffects")
                                        .executes(context1 -> clearEffects(context1.getSource(), ImmutableList.of(context1.getSource().getEntityOrException())))
                                        .then(
                                                Commands.argument("targets", EntityArgument.entities())
                                                        .executes(context1 -> clearEffects(context1.getSource(), EntityArgument.getEntities(context1, "targets")))
                                        )
                        )
        );
    }

    private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.clear.everything.failed"));


    private static int clearEffects(CommandSourceStack source, Collection<? extends Entity> targets) throws CommandSyntaxException {
        int i = 0;

        for (Entity entity : targets) {
            if (entity instanceof LivingEntity && PermanentEffectCapability.clearEffects((LivingEntity) entity)) {
                if (entity instanceof ServerPlayer player) {
                    player.connection.send(new ClientlikeClearPermanentEffectPacket());
                }
                i++;
            }
        }

        if (i == 0) {
            throw ERROR_CLEAR_EVERYTHING_FAILED.create();
        } else {
            if (targets.size() == 1) {
                source.sendSuccess(() -> Component.translatable("commands.effect.clear.everything.success.single", targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.effect.clear.everything.success.multiple", targets.size()), true);
            }

            return i;
        }
    }
}
