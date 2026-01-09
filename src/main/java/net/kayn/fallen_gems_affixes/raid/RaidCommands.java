package net.kayn.fallen_gems_affixes.raid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class RaidCommands {

    // Suggestion provider for raid IDs
    private static final SuggestionProvider<CommandSourceStack> RAID_SUGGESTER = (context, builder) -> {
        for (RaidData rd : RaidManager.get().knownRaids()) {
            builder.suggest(rd.id.toString());
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("raid")
                        // /raid list
                        .then(Commands.literal("list").executes(ctx -> {
                            Collection<RaidData> raids = RaidManager.get().knownRaids();
                            if (raids.isEmpty()) {
                                ctx.getSource().sendSuccess(() -> Component.literal("No raids are currently loaded."), false);
                                return 1;
                            }
                            StringBuilder sb = new StringBuilder("Loaded raids: ");
                            for (RaidData rd : raids) sb.append(rd.id).append(", ");
                            ctx.getSource().sendSuccess(() -> Component.literal(sb.substring(0, sb.length() - 2)), false);
                            return 1;
                        }))

                        // /raid start
                        .then(Commands.literal("start")
                                .then(Commands.argument("raid", StringArgumentType.word())
                                        .suggests(RAID_SUGGESTER)
                                        .executes(ctx -> startRaid(ctx.getSource(), ctx.getSource().getEntity(), StringArgumentType.getString(ctx, "raid")))
                                )
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("raid", StringArgumentType.word())
                                                .suggests(RAID_SUGGESTER)
                                                .executes(ctx -> {
                                                    String raidId = StringArgumentType.getString(ctx, "raid");
                                                    for (ServerPlayer player : EntityArgument.getPlayers(ctx, "player")) {
                                                        startRaid(ctx.getSource(), player, raidId);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // /raid stop
                        .then(Commands.literal("stop")
                                .then(Commands.argument("raid", StringArgumentType.word())
                                        .suggests(RAID_SUGGESTER)
                                        .executes(ctx -> stopRaid(ctx.getSource(), ctx.getSource().getEntity(), StringArgumentType.getString(ctx, "raid")))
                                )
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("raid", StringArgumentType.word())
                                                .suggests(RAID_SUGGESTER)
                                                .executes(ctx -> {
                                                    String raidId = StringArgumentType.getString(ctx, "raid");
                                                    for (ServerPlayer player : EntityArgument.getPlayers(ctx, "player")) {
                                                        stopRaid(ctx.getSource(), player, raidId);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private static int startRaid(CommandSourceStack src, net.minecraft.world.entity.Entity target, String raidId) {
        if (!(target instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("Only players can have raids."));
            return 0;
        }

        if (!src.hasPermission(2) && target != src.getEntity()) return 0;

        ResourceLocation rl = raidId.contains(":")
                ? new ResourceLocation(raidId)
                : new ResourceLocation("fallen_gems_affixes", raidId);

        RaidManager.get().getRaid(rl).ifPresentOrElse(rd -> {
            RaidManager.get().startRaid(player, rd, true);
            src.sendSuccess(() -> Component.literal("Started raid " + rl + " for " + player.getName().getString()), false);
        }, () -> src.sendFailure(Component.literal("Raid " + rl + " not found")));

        return 1;
    }

    private static int stopRaid(CommandSourceStack src, net.minecraft.world.entity.Entity target, String raidId) {
        if (!(target instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("Only players can have raids."));
            return 0;
        }

        if (!src.hasPermission(2) && target != src.getEntity()) return 0;

        ResourceLocation rl = raidId.contains(":")
                ? new ResourceLocation(raidId)
                : new ResourceLocation("fallen_gems_affixes", raidId);

        RaidManager.get().getActiveFor(player.getUUID()).ifPresentOrElse(inst -> {
            if (inst.getRaid().id.equals(rl)) {
                RaidManager.get().stopRaid(player.getUUID(), true, "raid.stopped_by_command");
                src.sendSuccess(() -> Component.literal("Stopped raid " + rl + " for " + player.getName().getString()), false);
            } else {
                src.sendFailure(Component.literal(player.getName().getString() + " is not currently in raid " + rl));
            }
        }, () -> src.sendFailure(Component.literal(player.getName().getString() + " has no active raid.")));

        return 1;
    }
}