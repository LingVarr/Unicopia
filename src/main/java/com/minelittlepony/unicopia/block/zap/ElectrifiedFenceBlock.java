package com.minelittlepony.unicopia.block.zap;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ElectrifiedFenceBlock extends FenceBlock implements ElectrifiedBlock {

    public ElectrifiedFenceBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        spawnElectricalParticles(world, pos, random);
    }

    @Deprecated
    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        triggerLightning(state, world, pos);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return getBlockBreakingDelta(super.calcBlockBreakingDelta(state, player, world, pos), player);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity l && l.hurtTime == 0 && l.canTakeDamage()) {
            triggerLightning(state, world, pos, l, true);
        }
    }
}