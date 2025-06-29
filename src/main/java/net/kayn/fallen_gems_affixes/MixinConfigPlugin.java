package net.kayn.fallen_gems_affixes;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MixinConfigPlugin implements IMixinConfigPlugin {

    private static final String SOCKET_HELPER_MIXIN = "net.kayn.fallen_gems_affixes.mixin.SocketHelperMixin";
    private static final String GUN_MIXIN = "net.kayn.fallen_gems_affixes.mixin.GunModifierHelperMixin";

    private static boolean enableSocketMixin = true;

    @Override
    public void onLoad(String mixinPackage) {
        readTomlConfigEarly();
    }

    private void readTomlConfigEarly() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("fallen_gems_affixes-common.toml");
        if (Files.notExists(configPath)) return;

        try (BufferedReader reader = Files.newBufferedReader(configPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("enableSocketHelperMixin")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        enableSocketMixin = Boolean.parseBoolean(parts[1].trim());
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Fallen Gems Affixes] Failed to read enableSocketHelperMixin from config: " + e.getMessage());
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals(SOCKET_HELPER_MIXIN)) {
            return enableSocketMixin;
        }
        if (mixinClassName.equals(GUN_MIXIN)) {
            return isModLoaded("scguns");
        }
        return true;
    }

    private static boolean isModLoaded(String modId) {
        return LoadingModList.get().getMods().stream()
                .anyMatch(modInfo -> modId.equals(modInfo.getModId()));
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}