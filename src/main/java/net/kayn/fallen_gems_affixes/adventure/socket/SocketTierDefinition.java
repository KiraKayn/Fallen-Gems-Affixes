package net.kayn.fallen_gems_affixes.adventure.socket;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;

public final class SocketTierDefinition {

    private final ResourceLocation rarityId;
    private final int colorPacked;
    private final boolean rainbow;
    private final float chance;
    private final boolean enabled;

    private int resolvedOrdinal = -1;

    public SocketTierDefinition(ResourceLocation rarityId, int colorPacked, boolean rainbow,
                                float chance, boolean enabled) {
        this.rarityId    = rarityId;
        this.colorPacked = colorPacked;
        this.rainbow     = rainbow;
        this.chance      = chance;
        this.enabled     = enabled;
    }


    public ResourceLocation rarityId()    { return rarityId; }
    public int              colorPacked() { return colorPacked; }
    public boolean          rainbow()     { return rainbow; }
    public float            chance()      { return chance; }
    public boolean          enabled()     { return enabled; }


    public int ordinal() { return resolvedOrdinal; }

    void setResolvedOrdinal(int ordinal) { this.resolvedOrdinal = ordinal; }

    public String getEmptyTranslationKey() {
        return "socket_tier." + rarityId.getNamespace() + "." + rarityId.getPath() + ".empty";
    }


    @Nullable
    public static SocketTierDefinition parse(JsonObject obj) {
        try {
            ResourceLocation rarityId = new ResourceLocation(GsonHelper.getAsString(obj, "rarity"));

            String colorStr  = GsonHelper.getAsString(obj, "color", "#FFFFFF");
            boolean rainbow  = colorStr.equalsIgnoreCase("rainbow");
            int colorPacked  = rainbow
                    ? 0xFFFFFF
                    : Integer.parseInt(colorStr.startsWith("#") ? colorStr.substring(1) : colorStr, 16);

            float chance     = GsonHelper.getAsFloat(obj, "chance", 0f);
            boolean enabled  = GsonHelper.getAsBoolean(obj, "enabled", true);

            return new SocketTierDefinition(rarityId, colorPacked, rainbow, chance, enabled);
        } catch (Exception e) {
            return null;
        }
    }
}