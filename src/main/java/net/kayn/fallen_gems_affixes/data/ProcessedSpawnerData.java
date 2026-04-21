//package net.kayn.fallen_gems_affixes.data;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.nbt.LongTag;
//import net.minecraft.nbt.Tag;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.level.saveddata.SavedData;
//
//import java.util.HashSet;
//import java.util.Set;
//
//public class ProcessedSpawnerData extends SavedData {
//
//    private static final String NAME = "fallen_gems_affixes_processed_spawners";
//    private final Set<Long> processedPositions = new HashSet<>();
//    private final Set<Long> playerPlacedPositions = new HashSet<>();
//
//    public static ProcessedSpawnerData get(ServerLevel level) {
//        return level.getDataStorage().computeIfAbsent(
//                ProcessedSpawnerData::load,
//                ProcessedSpawnerData::new,
//                NAME
//        );
//    }
//
//    public boolean isPlayerPlaced(BlockPos pos) {
//        return playerPlacedPositions.contains(pos.asLong());
//    }
//
//    public void markPlayerPlaced(BlockPos pos) {
//        playerPlacedPositions.add(pos.asLong());
//        setDirty();
//    }
//
//    public boolean isProcessed(BlockPos pos) {
//        return processedPositions.contains(pos.asLong());
//    }
//
//    public void markProcessed(BlockPos pos) {
//        processedPositions.add(pos.asLong());
//        setDirty();
//    }
//
//    @Override
//    public CompoundTag save(CompoundTag tag) {
//        ListTag processed = new ListTag();
//        for (long l : processedPositions) processed.add(LongTag.valueOf(l));
//        tag.put("positions", processed);
//
//        ListTag placed = new ListTag();
//        for (long l : playerPlacedPositions) placed.add(LongTag.valueOf(l));
//        tag.put("player_placed_positions", placed);
//
//        return tag;
//    }
//
//    private static ProcessedSpawnerData load(CompoundTag tag) {
//        ProcessedSpawnerData data = new ProcessedSpawnerData();
//
//        ListTag processed = tag.getList("positions", Tag.TAG_LONG);
//        for (Tag entry : processed) data.processedPositions.add(((LongTag) entry).getAsLong());
//
//        ListTag placed = tag.getList("player_placed_positions", Tag.TAG_LONG);
//        for (Tag entry : placed) data.playerPlacedPositions.add(((LongTag) entry).getAsLong());
//
//        return data;
//    }
//}