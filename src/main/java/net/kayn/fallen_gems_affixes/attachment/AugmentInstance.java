package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.augment.SoulboundAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public class AugmentInstance implements INBTSerializable<CompoundTag> {
    private IAugment augment;

    public AugmentInstance(IAugment augment) {
        this.augment = augment;
    }

    public AugmentInstance() {
        this.augment = null;
    }

    public ResourceLocation getId() {
        return augment != null ? augment.getId() : null;
    }

    public IAugment getAugment() {
        return augment;
    }

    public void setAugment(IAugment augment) {
        this.augment = augment;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        if (augment != null) {
            tag.putString("augmentId", augment.getId().toString());

            if (augment instanceof INBTSerializable) {
                CompoundTag augmentData = ((INBTSerializable<CompoundTag>) augment).serializeNBT();
                tag.put("augmentData", augmentData);
            }
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("augmentId")) {
            String augmentIdString = tag.getString("augmentId");
            ResourceLocation augmentId = new ResourceLocation(augmentIdString);

            IAugment template = AugmentRegistry.get(augmentId);
            if (template != null) {
                this.augment = createNewInstance(template);

                if (augment instanceof INBTSerializable && tag.contains("augmentData")) {
                    CompoundTag augmentData = tag.getCompound("augmentData");
                    ((INBTSerializable<CompoundTag>) augment).deserializeNBT(augmentData);
                }
            }
        }
    }

    private IAugment createNewInstance(IAugment template) {
        if (template instanceof SoulboundAugment) {
            return new SoulboundAugment();
        }

        return null;
    }

    @Override
    public String toString() {
        return "AugmentInstance{" +
                "augment=" + (augment != null ? augment.toString() : "null") +
                "}";
    }
}