package me.mekb.slabplacement.mixin;

import me.mekb.slabplacement.SlabPlacement;
import me.mekb.slabplacement.SlabPlacement.SlabPlacementMode;
import me.mekb.slabplacement.client.SlabPlacementClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.SlabBlock.TYPE;
import static net.minecraft.block.SlabBlock.WATERLOGGED;

@Mixin(SlabBlock.class)
public class BlockPlacementMixin extends Block implements Waterloggable {
    private BlockPlacementMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
    private void injectPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        SlabPlacementMode slabPlacementMode = SlabPlacementMode.DEFAULT;

        if (ctx.getWorld().isClient) {
            if (SlabPlacementClient.getIsServerInitialized()) {
                slabPlacementMode = SlabPlacementClient.slabPlacementMode;
            }
        } else {
            slabPlacementMode = SlabPlacement.getSlabPlacementMode(ctx.getPlayer());
        }

        SlabType slabType;
        switch (slabPlacementMode) {
            case TOP -> slabType = SlabType.TOP;
            case BOTTOM -> slabType = SlabType.BOTTOM;
            default -> {
                return;
            }
        };

        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());

        BlockState newBlockState = this.getDefaultState()
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
                .with(TYPE, slabType);

        cir.setReturnValue(newBlockState);
    }

    @Inject(method = "canReplace(Lnet/minecraft/block/BlockState;Lnet/minecraft/item/ItemPlacementContext;)Z", at = @At("HEAD"), cancellable = true)
    private void injectCanReplace(BlockState state, ItemPlacementContext ctx, CallbackInfoReturnable<Boolean> cir) {
        SlabPlacementMode slabPlacementMode = SlabPlacementMode.DEFAULT;

        if (ctx.getWorld().isClient) {
            if (SlabPlacementClient.getIsServerInitialized()) {
                slabPlacementMode = SlabPlacementClient.slabPlacementMode;
            }
        } else {
            slabPlacementMode = SlabPlacement.getSlabPlacementMode(ctx.getPlayer());
        }

        if (slabPlacementMode == SlabPlacementMode.DEFAULT) return;

        ItemStack itemStack = ctx.getStack();
        if (itemStack.isOf(this.asItem())) {
            cir.setReturnValue(false);
        }
    }
}
