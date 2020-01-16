package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockDutchDoor extends UDoor {

    public BlockDutchDoor(Material material, String domain, String name, Supplier<Item> theItem) {
        super(material, domain, name, theItem);
    }

    @Override
    protected BlockPos getPrimaryDoorPos(BlockState state, BlockPos pos) {
        return pos;
    }

    @Override
    public boolean isPassable(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).getValue(OPEN);
    }

    @Override
    protected boolean onPowerStateChanged(World world, BlockState state, BlockPos pos, boolean powered) {
        boolean result = super.onPowerStateChanged(world, state, pos, powered);

        BlockState upper = world.getBlockState(pos.up());
        if (upper.getBlock() == this && upper.getValue(OPEN) != powered) {
            world.setBlockState(pos.up(), upper.with(OPEN, powered));

            return true;
        }

        return result;
    }

    // UPPER - HALF/HINGE/POWER{/OPEN}
    // LOWER - HALF/FACING/FACING/OPEN

    @Override
    public BlockState getActualState(BlockState state, BlockView world, BlockPos pos) {

        // copy properties in stored by the sibling block
        if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER) {
            BlockState other = world.getBlockState(pos.up());

            if (other.getBlock() == this) {
                return state.with(HINGE, other.getValue(HINGE))
                    .with(POWERED, other.getValue(POWERED));
            }
        } else {
            BlockState other = world.getBlockState(pos.down());

            if (other.getBlock() == this) {
                return state.with(FACING, other.getValue(FACING));
            }
        }


        return state;
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        boolean upper = (meta & 8) != 0;

        BlockState state = getDefaultState()
                .with(HALF, upper ? EnumDoorHalf.UPPER : EnumDoorHalf.LOWER)
                .with(OPEN, (meta & 4) != 0);

        if (upper) {
            return state.with(POWERED, (meta & 1) != 0)
                    .with(HINGE, (meta & 2) != 0 ? EnumHingePosition.RIGHT : EnumHingePosition.LEFT);
        }

        return state.with(FACING, Direction.byHorizontalIndex(meta & 3).rotateYCCW());
    }

    @Override
    public int getMetaFromState(BlockState state) {
        int i = 0;

        if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
            i |= 8;

            if (state.getValue(POWERED)) {
                i |= 1;
            }

            if (state.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT) {
                i |= 2;
            }
        } else {
            i |= state.getValue(FACING).rotateY().getHorizontalIndex();
        }

        if (state.getValue(OPEN)) {
            i |= 4;
        }

        return i;
    }
}
