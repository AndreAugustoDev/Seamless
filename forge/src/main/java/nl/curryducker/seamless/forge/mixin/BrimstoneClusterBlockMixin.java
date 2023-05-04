package nl.curryducker.seamless.forge.mixin;

import biomesoplenty.common.block.BrimstoneClusterBlock;
import biomesoplenty.common.block.DoublePlantBlockBOP;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import nl.curryducker.seamless.SeamlessShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrimstoneClusterBlock.class)
public class BrimstoneClusterBlockMixin extends DoublePlantBlockBOP {

    public BrimstoneClusterBlockMixin(Properties arg2) {
        super(arg2);
    }

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    public void getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(SeamlessShapes.brimstoneClusterBlock(blockState.getValue(HALF) == DoubleBlockHalf.LOWER));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
        VoxelShape SHAPE_TOP = Block.box(6.0, 0.0, 6.0, 10.0, 8.0, 10.0);
        return blockState.getValue(HALF) == DoubleBlockHalf.UPPER ? SHAPE_TOP : SHAPE;
    }
}