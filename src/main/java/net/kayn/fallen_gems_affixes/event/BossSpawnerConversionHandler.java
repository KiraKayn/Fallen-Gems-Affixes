package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.boss.BossSpawnerBlock;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Mod.EventBusSubscriber
public class BossSpawnerConversionHandler {

//    private static final String KEY_PLAYER_PLACED = "fga:player_placed";
//    private static final String KEY_PROCESSED = "fga:processed";
    private static final ResourceLocation BOSS_SPAWNER = ResourceLocation.fromNamespaceAndPath("apotheosis", "boss_spawner");

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk()) return;
        if (!ModConfig.ENABLE_BOSS_SPAWNER_CONVERSION.get()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        BossSpawnerBlock bossSpawnerBlock = Adventure.Blocks.BOSS_SPAWNER.get();
        double chance = ModConfig.BOSS_SPAWNER_CHANCE.get();
        var blocks = chunk.getBlockEntities();
        for (Map.Entry<BlockPos, BlockEntity> entry : blocks.entrySet()) {
            if (entry.getValue() instanceof SpawnerBlockEntity spawner) {
                computeConversion(level, bossSpawnerBlock, spawner.getBlockPos(), entry, chance);
            }
        }
    }

    private static void computeConversion(Level level, BossSpawnerBlock block, BlockPos pos, Map.Entry<BlockPos, BlockEntity> posEntity, double chance) {
        if (level.getRandom().nextDouble() < chance) {
            posEntity.setValue(block.newBlockEntity(pos, block.defaultBlockState()));
        }
    }
}