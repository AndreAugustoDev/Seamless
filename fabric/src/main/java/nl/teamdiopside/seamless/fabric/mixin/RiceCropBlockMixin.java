package nl.teamdiopside.seamless.fabric.mixin;

import com.nhoryzon.mc.farmersdelight.block.RiceCropBlock;
import com.nhoryzon.mc.farmersdelight.registry.BlocksRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import nl.teamdiopside.seamless.SeamlessShapes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RiceCropBlock.class)
public abstract class RiceCropBlockMixin extends BushBlock implements BonemealableBlock, LiquidBlockContainer {

    @Shadow @Final public static IntegerProperty AGE;

    public RiceCropBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    public void getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        int age = state.getValue(AGE);
        cir.setReturnValue(SeamlessShapes.rice(age, worldIn.getBlockState(pos.above()).getBlock() == BlocksRegistry.RICE_PANICLE.get() ? worldIn.getBlockState(pos.above()).getValue(AGE) : -1, true));
    }
}