package net.kayn.fallen_gems_affixes.event;

import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.data.ProcessedSpawnerData;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

@Mod.EventBusSubscriber
public class BossSpawnerConversionHandler {

    @SubscribeEvent
    public static void onSpawnerPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.getPlacedBlock().is(Blocks.SPAWNER)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        ProcessedSpawnerData data = ProcessedSpawnerData.get(level);
        data.markPlayerPlaced(event.getPos());
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!ModConfig.ENABLE_BOSS_SPAWNER_CONVERSION.get()) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        ProcessedSpawnerData data = ProcessedSpawnerData.get(level);
        double chance = ModConfig.BOSS_SPAWNER_CHANCE.get();

        for (BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values())) {
            if (!(be instanceof SpawnerBlockEntity)) continue;
            BlockPos pos = be.getBlockPos().immutable();

            if (data.isProcessed(pos)) continue;
            if (data.isPlayerPlaced(pos)) continue;

            data.markProcessed(pos);

            if (level.getRandom().nextDouble() < chance) {
                DelayedTaskScheduler.schedule(level, 1, () -> {
                    Block bossSpawnerBlock = ForgeRegistries.BLOCKS.getValue(
                            new ResourceLocation("apotheosis", "boss_spawner"));
                    if (bossSpawnerBlock == null) return;
                    if (level.isLoaded(pos)) {
                        level.setBlock(pos, bossSpawnerBlock.defaultBlockState(), 3);
                    }
                });
            }
        }
    }
}