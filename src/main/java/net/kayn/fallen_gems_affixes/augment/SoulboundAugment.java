package net.kayn.fallen_gems_affixes.augment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SoulboundAugment implements IAugment, INBTSerializable<CompoundTag> {
    private static final ResourceLocation SOULBOUND_ID = new ResourceLocation("fallen_gems_affixes", "soulbound");

    private final List<UUID> boundPlayers;

    private UUID originalOwner;

    private boolean allowMultipleBindings;

    public SoulboundAugment() {
        this.boundPlayers = new ArrayList<>();
        this.originalOwner = null;
        this.allowMultipleBindings = false;
    }

    @Override
    public ResourceLocation getId() {
        return SOULBOUND_ID;
    }
    public void bindToPlayer(UUID playerUUID) {
        if (playerUUID == null) return;

        if (originalOwner == null) {
            originalOwner = playerUUID;
            boundPlayers.add(playerUUID);
            return;
        }

        if (!allowMultipleBindings) {
            return;
        }

        if (!boundPlayers.contains(playerUUID)) {
            boundPlayers.add(playerUUID);
        }

    }

    public boolean unbindPlayer(UUID playerUUID) {
        if (playerUUID == null) return false;

        if (originalOwner != null && originalOwner.equals(playerUUID) && boundPlayers.size() > 1) {
            return false;
        }

        boolean removed = boundPlayers.remove(playerUUID);
        if (removed && originalOwner != null && originalOwner.equals(playerUUID) && !boundPlayers.isEmpty()) {
            originalOwner = boundPlayers.get(0);
        }

        if (boundPlayers.isEmpty()) {
            originalOwner = null;
        }

        return removed;
    }

    public boolean isBoundToPlayer(UUID playerUUID) {
        return playerUUID != null && boundPlayers.contains(playerUUID);
    }

    public boolean isBound() {
        return !boundPlayers.isEmpty();
    }

    public List<UUID> getBoundPlayers() {
        return new ArrayList<>(boundPlayers);
    }

    public UUID getOriginalOwner() {
        return originalOwner;
    }

    public void setAllowMultipleBindings(boolean allow) {
        this.allowMultipleBindings = allow;

        if (!allow && boundPlayers.size() > 1 && originalOwner != null) {
            boundPlayers.clear();
            boundPlayers.add(originalOwner);
        }
    }

    public boolean isMultipleBindingsAllowed() {
        return allowMultipleBindings;
    }

    public int getBoundPlayerCount() {
        return boundPlayers.size();
    }

    public void clearBindings() {
        boundPlayers.clear();
        originalOwner = null;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putString("id", getId().toString());
        tag.putBoolean("allowMultipleBindings", allowMultipleBindings);

        if (originalOwner != null) {
            tag.putUUID("originalOwner", originalOwner);
        }

        ListTag playersList = new ListTag();
        for (UUID playerUUID : boundPlayers) {
            playersList.add(StringTag.valueOf(playerUUID.toString()));
        }
        tag.put("boundPlayers", playersList);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        boundPlayers.clear();
        originalOwner = null;

        allowMultipleBindings = tag.getBoolean("allowMultipleBindings");

        if (tag.hasUUID("originalOwner")) {
            originalOwner = tag.getUUID("originalOwner");
        }

        if (tag.contains("boundPlayers", Tag.TAG_LIST)) {
            ListTag playersList = tag.getList("boundPlayers", Tag.TAG_STRING);
            for (int i = 0; i < playersList.size(); i++) {
                try {
                    String uuidString = playersList.getString(i);
                    UUID playerUUID = UUID.fromString(uuidString);
                    boundPlayers.add(playerUUID);
                } catch (IllegalArgumentException e) {
                    System.err.println("Failed to parse UUID in SoulboundAugment: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "SoulboundAugment{id=" + getId() + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SoulboundAugment other = (SoulboundAugment) obj;
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}