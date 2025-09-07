package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.types.augment.IAugmentAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AugmentCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<IAugmentAccessor> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>(){});

    // test
    private final IAugmentAccessor instance = new AugmentAccessor();

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? LazyOptional.of(() -> instance).cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag listTag = new ListTag();
        {
            CompoundTag element = new CompoundTag();
            element.putString("Test", "test");
            listTag.add(element);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Augments", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag listTag = nbt.getList("Augments", Tag.TAG_COMPOUND);
        Optional<IAugmentAccessor> instOpt = getCapability(CAPABILITY).resolve();
        if (instOpt.isPresent()) {
            // we can create every augment by reading nbt here.
            IAugmentAccessor accessor = instOpt.get();
            IAugmentHandler handler = accessor.getHandler();
            IAugmentContainer container = accessor.getContainer();
        }
        else return;
        for (Tag tag : listTag) {
            CompoundTag tag1 = (CompoundTag) tag;
            String test = tag1.getString("Test");
        }
    }
}
