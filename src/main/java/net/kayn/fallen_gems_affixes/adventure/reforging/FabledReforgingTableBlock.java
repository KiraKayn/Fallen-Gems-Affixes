package net.kayn.fallen_gems_affixes.adventure.reforging;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.block_entity.TickingEntityBlock;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.menu.SimplerMenuProvider;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class FabledReforgingTableBlock extends Block implements TickingEntityBlock {

    public static final Component TITLE = Component.translatable("container." + FallenGemsAffixes.MOD_ID + ".fabled_reforge");
    public static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public FabledReforgingTableBlock(Properties properties, int i) {
        super(properties);
    }

    public LootRarity getFabledRarity() {
        return RarityRegistry.byLegacyId("fallen_gems_affixes:fabled").get();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        return MenuUtil.openGui(player, pos, FabledReforgingMenu::new);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return new SimplerMenuProvider<>(world, pos, FabledReforgingMenu::new);
    }

    @Override
    public void appendHoverText(ItemStack pStack, BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(Component.translatable("block." + FallenGemsAffixes.MOD_ID + ".fabled_reforging_table.desc").withStyle(ChatFormatting.GRAY));
        list.add(Component.translatable("block." + FallenGemsAffixes.MOD_ID + ".fabled_reforging_table.desc2", getFabledRarity().toComponent()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FabledReforgingTableTile(pPos, pState);
    }

    @Override
    @Deprecated
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() == this && newState.getBlock() == this) return;
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof FabledReforgingTableTile tile) {
            for (int i = 0; i < tile.inv.getSlots(); i++) {
                popResource(world, pos, tile.inv.getStackInSlot(i));
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }
}