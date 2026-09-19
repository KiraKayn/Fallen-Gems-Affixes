package net.kayn.fallen_gems_affixes.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = "fallen_gems_affixes")
public final class DelayedTaskScheduler {

    private static final List<ScheduledTask> PENDING = new ArrayList<>();

    private DelayedTaskScheduler() {}

    static {
        NeoForge.EVENT_BUS.register(DelayedTaskScheduler.class);
    }

    public static void schedule(Level level, int delayTicks, Runnable task) {
        if (level.isClientSide()) return;
        long executeAt = level.getGameTime() + Math.max(1, delayTicks);
        PENDING.add(new ScheduledTask(executeAt, (ServerLevel) level, task));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) return;

        List<ScheduledTask> snapshot = new ArrayList<>(PENDING);
        for (ScheduledTask task : snapshot) {
            if (task.level.getGameTime() >= task.executeAt) {
                PENDING.remove(task);
                try { task.runnable.run(); }
                catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    public static void clear() { PENDING.clear(); }

    private static final class ScheduledTask {
        final long executeAt;
        final ServerLevel level;
        final Runnable runnable;
        ScheduledTask(long executeAt, ServerLevel level, Runnable runnable) {
            this.executeAt = executeAt;
            this.level = level;
            this.runnable = runnable;
        }
    }
}