package net.kayn.fallen_gems_affixes.attachment.augment;

import net.kayn.fallen_gems_affixes.types.augment.IAugmentAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AugmentCapability implements ICapabilitySerializable<CompoundTag> {
    private final LivingEntity entity;

    public static final Capability<IAugmentAccessor> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>(){});

    private final IAugmentAccessor accessor;

    public AugmentCapability(LivingEntity entity) {
        this.entity = entity;
        this.accessor = new AugmentAccessor(entity);
    }

    public AugmentCapability() {
        this.entity = null;
        this.accessor = new AugmentAccessor(null);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? LazyOptional.of(() -> this.accessor).cast() : LazyOptional.empty();
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

    public LivingEntity getEntity() {
        return entity;
    }
}