package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class GemCaseBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D);

    private static final DecimalFormat FMT = new DecimalFormat("##.#");

    protected final java.util.function.BiFunction<BlockPos, BlockState, GemCaseTile> tileFactory;
    protected final int maxCount;

    public GemCaseBlock(java.util.function.BiFunction<BlockPos, BlockState, GemCaseTile> tileFactory, BlockBehaviour.Properties props, int maxCount) {
        super(props);
        this.tileFactory = tileFactory;
        this.maxCount = maxCount;
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((id, inv, p) -> new GemCaseMenu(id, inv, pos), Component.translatable("menu.fallen_gems_affixes.gem_case")), buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inv, player) -> new GemCaseMenu(id, inv, pos), Component.translatable("menu.fallen_gems_affixes.gem_case"));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return tileFactory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof GemCaseTile tile) tile.clientTick(lvl, pos, st);
            };
        }
        return null;
    }

    public ItemStack getCloneItemStack(BlockState state, BlockHitResult target, BlockGetter level, BlockPos pos, Player player) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = level.getBlockEntity(pos);
        if (te != null) {
            CompoundTag tag = te.saveWithFullMetadata();
            if (!tag.isEmpty()) BlockItem.setBlockEntityData(s, te.getType(), tag);
        }
        return s;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        CompoundTag data = BlockItem.getBlockEntityData(stack);
        if (data != null && !data.isEmpty()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GemCaseTile tile) tile.load(data);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder ctx) {
        ItemStack s = new ItemStack(this);
        BlockEntity te = ctx.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (te instanceof GemCaseTile tile) {
            CompoundTag tag = tile.saveWithFullMetadata();
            if (!tag.isEmpty()) BlockItem.setBlockEntityData(s, tile.getType(), tag);
        }
        return Arrays.asList(s);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("tooltip.fallen_gems_affixes.gem_case.capacity", format(maxCount)).withStyle(ChatFormatting.GOLD));
        CompoundTag data = BlockItem.getBlockEntityData(stack);
        if (data != null && data.contains("gems")) {
            int count = data.getCompound("gems").size();
            if (count > 0)
                list.add(Component.translatable("tooltip.fallen_gems_affixes.gem_case.unique_gems", count).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) world.removeBlockEntity(pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    public static String format(int n) {
        int log = (int) StrictMath.log10(n);
        if (log <= 3) return String.valueOf(n);
        else if (log <= 6) return FMT.format(n / 1000.0) + "K";
        else if (log <= 8) return FMT.format(n / 1_000_000.0) + "M";
        else return FMT.format(n / 1_000_000_000.0) + "B";
    }
}
