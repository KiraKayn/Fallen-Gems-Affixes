package net.kayn.fallen_gems_affixes.adventure.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.util.Optional;

public final class CatalystSocketConfig extends SimplePreparableReloadListener<Optional<JsonObject>> {

    public static final CatalystSocketConfig INSTANCE = new CatalystSocketConfig();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String PATH = "socket_tiers/catalyst.json";

    private float powerPerSocket = 0.1f;
    private int colorPacked = 0x55FFFF;
    private boolean rainbow = false;

    private CatalystSocketConfig() {}

    @Override
    protected Optional<JsonObject> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return manager.getResource(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", PATH))
                .map(resource -> {
                    try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                        return GSON.fromJson(reader, JsonObject.class);
                    } catch (Exception e) {
                        LOGGER.error("[FGA] Failed to read catalyst socket config: {}", e.getMessage());
                        return null;
                    }
                });
    }

    @Override
    protected void apply(Optional<JsonObject> data, ResourceManager manager, ProfilerFiller profiler) {
        powerPerSocket = 0.1f;
        colorPacked    = 0x55FFFF;
        rainbow        = false;

        data.ifPresent(obj -> {
            powerPerSocket = GsonHelper.getAsFloat(obj, "power_per_socket", 0.1f);
            String colorStr = GsonHelper.getAsString(obj, "color", "#55FFFF");
            rainbow = colorStr.equalsIgnoreCase("rainbow");
            if (!rainbow) {
                colorPacked = Integer.parseInt(colorStr.startsWith("#") ? colorStr.substring(1) : colorStr, 16);
            }
            LOGGER.info("[FGA] Loaded catalyst socket config: power_per_socket={}, color={}", powerPerSocket, colorStr);
        });
    }

    public float getPowerPerSocket() { return powerPerSocket; }
    public int   getColorPacked()   { return colorPacked; }
    public boolean isRainbow()      { return rainbow; }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }
}
