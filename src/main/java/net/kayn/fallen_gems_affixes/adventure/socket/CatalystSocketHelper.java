package net.kayn.fallen_gems_affixes.adventure.socket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class CatalystSocketHelper {

    private CatalystSocketHelper() {}

    public static final String CATALYST_KEY = "catalyst_socket";
    public static final String CATALYST_POWER = "catalyst_power";
    public static final String CATALYST_SOCKET_COUNT = "catalyst_socket_count";

    public static boolean hasCatalystSocket(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        return afxData != null && afxData.getBoolean(CATALYST_KEY);
    }

    public static int getCatalystSocketCount(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        return afxData != null ? afxData.getInt(CATALYST_SOCKET_COUNT) : 0;
    }

    public static float getCatalystPower(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement("affix_data");
        return afxData != null ? afxData.getFloat(CATALYST_POWER) : 0.1f;
    }

    public static float getGemPowerMultiplier(ItemStack stack) {
        if (!hasCatalystSocket(stack)) return 1.0f;
        int count = getCatalystSocketCount(stack);
        float power = getCatalystPower(stack);
        return 1.0f + (count * power);
    }

    public static void apply(ItemStack stack, int totalSockets, float powerPerSocket) {
        CompoundTag afxData = stack.getOrCreateTagElement("affix_data");
        afxData.putBoolean(CATALYST_KEY, true);
        afxData.putInt(CATALYST_SOCKET_COUNT, totalSockets);
        afxData.putFloat(CATALYST_POWER, powerPerSocket);
        afxData.putInt("sockets", 1);
        TieredSocketHelper.setSocketTiers(stack, new int[]{TieredSocketHelper.REGULAR_SOCKET});
    }
}