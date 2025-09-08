package net.kayn.fallen_gems_affixes.augment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public class SoulboundAugment implements IAugment, INBTSerializable<CompoundTag> {
    private static final ResourceLocation SOULBOUND_ID = new ResourceLocation("fallen_gems_affixes", "soulbound");

    private EquipmentSlot equipmentSlot;
    private int slotIndex;
    private ItemStack storedItemStack;

    public SoulboundAugment() {
        this.equipmentSlot = null;
        this.slotIndex = -1;
        this.storedItemStack = ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return SOULBOUND_ID;
    }


    //Store the equipment slot and item data
    public void storeItemData(EquipmentSlot slot, int index, ItemStack stack) {
        this.equipmentSlot = slot;
        this.slotIndex = index;
        this.storedItemStack = stack.copy();
    }


    //Get the stored equipment slot

    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }


    //Get the stored slot index
    public int getSlotIndex() {
        return slotIndex;
    }


    //Get the stored item stack

    public ItemStack getStoredItemStack() {
        return storedItemStack.copy();
    }


    //Check if this augment has stored item data
    public boolean hasStoredData() {
        return equipmentSlot != null && !storedItemStack.isEmpty();
    }


    //Clear stored data

    public void clearStoredData() {
        this.equipmentSlot = null;
        this.slotIndex = -1;
        this.storedItemStack = ItemStack.EMPTY;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putString("id", getId().toString());

        if (equipmentSlot != null) {
            tag.putString("equipmentSlot", equipmentSlot.getName());
        }

        tag.putInt("slotIndex", slotIndex);

        if (!storedItemStack.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            storedItemStack.save(itemTag);
            tag.put("storedItemStack", itemTag);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        equipmentSlot = null;
        slotIndex = -1;
        storedItemStack = ItemStack.EMPTY;

        // Read equipment slot
        if (tag.contains("equipmentSlot")) {
            String slotName = tag.getString("equipmentSlot");
            try {
                equipmentSlot = EquipmentSlot.byName(slotName);
            } catch (Exception e) {
                // If slot name is invalid leave as null
            }
        }

        // Read slot index
        slotIndex = tag.getInt("slotIndex");

        // Read stored item stack
        if (tag.contains("storedItemStack")) {
            CompoundTag itemTag = tag.getCompound("storedItemStack");
            storedItemStack = ItemStack.of(itemTag);
        }
    }

    @Override
    public String toString() {
        return "SoulboundAugment{" + "id=" + getId() + ", slot=" + (equipmentSlot != null ? equipmentSlot.getName() : "none") + ", index=" + slotIndex + ", hasItem=" + !storedItemStack.isEmpty() + "}";
    }

}