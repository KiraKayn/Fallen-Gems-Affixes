package net.kayn.fallen_gems_affixes.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.kayn.fallen_gems_affixes.adventure.affix.ProspectorAffix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ProspectorHighlightHandler {

    private static final List<BlockPos> ORE_POSITIONS = new ArrayList<>();
    private static int scanCooldown = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;

        if (--scanCooldown > 0) return;
        scanCooldown = 20;

        Player player = mc.player;
        int range = getRange(player);

        if (range <= 0) {
            ORE_POSITIONS.clear();
            return;
        }

        Level level = mc.level;
        BlockPos playerPos = player.blockPosition();
        List<BlockPos> found = new ArrayList<>();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    mutable.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                    BlockState state = level.getBlockState(mutable);
                    if (state.is(BlockTags.COAL_ORES)
                            || state.is(BlockTags.IRON_ORES)
                            || state.is(BlockTags.GOLD_ORES)
                            || state.is(BlockTags.DIAMOND_ORES)
                            || state.is(BlockTags.EMERALD_ORES)
                            || state.is(BlockTags.LAPIS_ORES)
                            || state.is(BlockTags.REDSTONE_ORES)
                            || state.is(BlockTags.COPPER_ORES)
                            || state.is(net.minecraft.tags.BlockTags.create(
                            new net.minecraft.resources.ResourceLocation("c", "ores")))) {
                        found.add(mutable.immutable());
                    }
                }
            }
        }

        ORE_POSITIONS.clear();
        ORE_POSITIONS.addAll(found);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (ORE_POSITIONS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.player.isCrouching()) return;

        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(2.0f);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.getBuilder();
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float r = 1.0f, g = 0.85f, b = 0.0f, a = 0.6f;

        Matrix4f mat = pose.last().pose();

        for (BlockPos pos : ORE_POSITIONS) {
            double x = pos.getX() - cam.x;
            double y = pos.getY() - cam.y;
            double z = pos.getZ() - cam.z;
            renderBox(buf, mat, x, y, z, x + 1, y + 1, z + 1, r, g, b, a);
        }

        tesselator.end();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderBox(BufferBuilder buf, Matrix4f mat,
                                  double x1, double y1, double z1,
                                  double x2, double y2, double z2,
                                  float r, float g, float b, float a) {
        // Bottom face
        buf.vertex(mat, (float)x1, (float)y1, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y1, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y1, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y1, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y1, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x1, (float)y1, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x1, (float)y1, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x1, (float)y1, (float)z1).color(r,g,b,a).endVertex();
        // Top face
        buf.vertex(mat, (float)x1, (float)y2, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y2, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y2, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y2, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y2, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x1, (float)y2, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x1, (float)y2, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x1, (float)y2, (float)z1).color(r,g,b,a).endVertex();
        // Vertical edges
        buf.vertex(mat, (float)x1, (float)y1, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x1, (float)y2, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y1, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y2, (float)z1).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y1, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x2, (float)y2, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x1, (float)y1, (float)z2).color(r,g,b,a).endVertex();
        buf.vertex(mat, (float)x1, (float)y2, (float)z2).color(r,g,b,a).endVertex();
    }

    private static int getRange(Player player) {
        for (var stack : player.getAllSlots()) {
            for (var inst : AffixHelper.getAffixes(stack).values()) {
                if (!inst.isValid()) continue;
                if (inst.affix().get() instanceof ProspectorAffix affix) {
                    return affix.getRange(inst.rarity().get());
                }
            }
        }
        return 0;
    }
}