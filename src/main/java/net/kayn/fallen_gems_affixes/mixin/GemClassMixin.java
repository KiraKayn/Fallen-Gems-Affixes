package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GemClass.class, remap = false)
public interface GemClassMixin {

    @Accessor("types")
    void setTypes(HolderSet<LootCategory> types);
}