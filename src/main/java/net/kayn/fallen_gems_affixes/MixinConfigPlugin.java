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
    private static final String PE_CLIENT_LIVING_ENTITY_MIXIN = "net.kayn.fallen_gems_affixes.mixin.permanent_effect.client.LivingEntityMixin";
    private static final String PE_CLIENT_ABSTRACT_CONTAINER_MENU_MIXIN = "net.kayn.fallen_gems_affixes.mixin.permanent_effect.client.AbstractContainerMenuMixin";
    private static final String PE_CLIENT_PLAYER_MIXIN = "net.kayn.fallen_gems_affixes.mixin.permanent_effect.client.PlayerMixin";
    private static final String PE_CLIENT_SLOT_MIXIN = "net.kayn.fallen_gems_affixes.mixin.permanent_effect.client.SlotMixin";
    private static final String PE_CLIENT_MINECRAFT_MIXIN = "net.kayn.fallen_gems_affixes.mixin.permanent_effect.client.MinecraftMixin";
    private static final String PE_INVENTORY_MIXIN = "net.kayn.fallen_gems_affixes.mixin.permanent_effect.InventoryMixin";
    private static final String PE_LIVING_ENTITY_MIXIN = "net.kayn.fallen_gems_affixes.mixin.permanent_effect.LivingEntityMixin";

    private static boolean enableSocketMixin = true;
    private static boolean enablePermanentEffectDefaultMixin = true;

    @Override
    public void onLoad(String mixinPackage) {
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
                        return;
                    }
                }
                if (line.startsWith("permanentEffectUseTickEvent")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        enablePermanentEffectDefaultMixin = Boolean.parseBoolean(parts[1].trim());
                        return;
                    }

                }
            }
        } catch (IOException ignored) {
            enableSocketMixin = true;
            enablePermanentEffectDefaultMixin = true;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return switch (mixinClassName) {
            case SOCKET_HELPER_MIXIN -> enableSocketMixin;
            case PE_CLIENT_LIVING_ENTITY_MIXIN -> enablePermanentEffectDefaultMixin;
            case PE_CLIENT_ABSTRACT_CONTAINER_MENU_MIXIN -> enablePermanentEffectDefaultMixin;
            case PE_CLIENT_PLAYER_MIXIN -> enablePermanentEffectDefaultMixin;
            case PE_CLIENT_SLOT_MIXIN -> enablePermanentEffectDefaultMixin;
            case PE_CLIENT_MINECRAFT_MIXIN -> enablePermanentEffectDefaultMixin;
            case PE_INVENTORY_MIXIN -> enablePermanentEffectDefaultMixin;
            case PE_LIVING_ENTITY_MIXIN -> enablePermanentEffectDefaultMixin;
            case GUN_MIXIN -> isModLoaded();
            default -> true;
        };
    }

    private static boolean isModLoaded() {
        return LoadingModList.get().getMods().stream()
                .anyMatch(modInfo -> "scguns".equals(modInfo.getModId()));
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}