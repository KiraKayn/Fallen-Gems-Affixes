package net.kayn.fallen_gems_affixes.event;

import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

    private static final String KEY_PLAYER_PLACED = "fga:player_placed";
    private static final String KEY_PROCESSED = "fga:processed";

    @SubscribeEvent
    public static void onSpawnerPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.getPlacedBlock().is(Blocks.SPAWNER)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockEntity be = level.getBlockEntity(event.getPos());
        if (!(be instanceof SpawnerBlockEntity spawner)) return;

        spawner.getPersistentData().putBoolean(KEY_PLAYER_PLACED, true);
        spawner.setChanged();
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!ModConfig.ENABLE_BOSS_SPAWNER_CONVERSION.get()) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        double chance = ModConfig.BOSS_SPAWNER_CHANCE.get();

        for (BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values())) {
            if (!(be instanceof SpawnerBlockEntity spawner)) continue;

            CompoundTag persistent = spawner.getPersistentData();

            if (persistent.getBoolean(KEY_PLAYER_PLACED)) continue;
            if (persistent.getBoolean(KEY_PROCESSED)) continue;

            persistent.putBoolean(KEY_PROCESSED, true);
            spawner.setChanged();

            if (level.getRandom().nextDouble() < chance) {
                BlockPos pos = be.getBlockPos().immutable();
                DelayedTaskScheduler.schedule(level, 1, () -> {
                    Block bossSpawnerBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("apotheosis", "boss_spawner"));
                    if (bossSpawnerBlock == null) return;
                    if (level.isLoaded(pos)) {
                        level.setBlock(pos, bossSpawnerBlock.defaultBlockState(), 3);
                    }
                });
            }
        }
    }
}