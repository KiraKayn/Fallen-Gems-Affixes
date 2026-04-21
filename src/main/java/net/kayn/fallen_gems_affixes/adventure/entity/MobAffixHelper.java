package net.kayn.fallen_gems_affixes.adventure.entity;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffixRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MobAffixHelper {

    public static final String TAG = "fga.mob_affixes";

    private MobAffixHelper() {}

    public record ResolvedMobAffix(EntityAffix affix, float level) {}

    public static void addAffix(LivingEntity entity, ResourceLocation affixId, float level) {
        CompoundTag data = entity.getPersistentData();
        ListTag list = data.contains(TAG, Tag.TAG_LIST)
                ? data.getList(TAG, Tag.TAG_COMPOUND)
                : new ListTag();

        CompoundTag entry = new CompoundTag();
        entry.putString("affix", affixId.toString());
        entry.putFloat("level", Math.max(0f, Math.min(1f, level)));
        list.add(entry);
        data.put(TAG, list);
    }

    public static List<ResolvedMobAffix> getAffixes(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(TAG, Tag.TAG_LIST)) return Collections.emptyList();

        ListTag list = data.getList(TAG, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return Collections.emptyList();

        List<ResolvedMobAffix> result = new ArrayList<>(list.size());
        for (Tag t : list) {
            if (!(t instanceof CompoundTag entry)) continue;
            try {
                ResourceLocation id = ResourceLocation.parse(entry.getString("affix"));
                float level = entry.getFloat("level");
                EntityAffix affix = EntityAffixRegistry.getInstance(id);
                if (affix != null) result.add(new ResolvedMobAffix(affix, level));
            } catch (Exception e) {
                FallenGemsAffixes.LOGGER.warn("[FGA] Failed to read mob affix entry: {}", e.getMessage());
            }
        }
        return result;
    }

    public static boolean hasAffixes(LivingEntity entity) {
        return entity.getPersistentData().contains(TAG, Tag.TAG_LIST)
                && !entity.getPersistentData().getList(TAG, Tag.TAG_COMPOUND).isEmpty();
    }
}