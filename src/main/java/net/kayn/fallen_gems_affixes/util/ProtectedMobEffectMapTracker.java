//package net.kayn.fallen_gems_affixes.util;
//
//import net.minecraft.world.entity.player.Player;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Map;
//import java.util.WeakHashMap;
//
//public class ProtectedMobEffectMapTracker {
//    private static final Map<Player, ProtectedMobEffectMap<?>> instances = new WeakHashMap<>();
//
//    public static void register(Player player) {
//        if (!instances.containsKey(player)) {
//            ProtectedMobEffectMap<?> wrapped = new ProtectedMobEffectMap<>(player, player.getActiveEffectsMap());
//            instances.put(player, wrapped);
//        }
//    }
//
//    @Nullable
//    public static ProtectedMobEffectMap<?> get(Player player) {
//        return instances.get(player);
//    }
//}
