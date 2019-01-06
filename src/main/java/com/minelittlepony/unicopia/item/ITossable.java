package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.EntityProjectile;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITossable {
    IBehaviorDispenseItem dispenserBehavior = new ITossable.DispenserBehaviour();

    boolean canBeThrown(ItemStack stack);

    void onImpact(World world, BlockPos pos, IBlockState state);

    default Item setDispenseable() {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject((Item)(Object)this, dispenserBehavior);

        return (Item)(Object)this;
    }

    default SoundEvent getThrowSound(ItemStack stack) {
        return SoundEvents.ENTITY_SNOWBALL_THROW;
    }

    default int getThrowDamage(ItemStack stack) {
        return 0;
    }

    default void toss(World world, ItemStack itemstack, EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        world.playSound(null, player.posX, player.posY, player.posZ, getThrowSound(itemstack), SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            EntityProjectile projectile = new EntityProjectile(world, player);

            projectile.setItem(itemstack);
            projectile.setThrowDamage(getThrowDamage(itemstack));
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, 1.5F, 1);

            world.spawnEntity(projectile);
        }

        player.addStat(StatList.getObjectUseStats(itemstack.getItem()));
    }

    default ItemStack toss(World world, IPosition pos, EnumFacing facing, ItemStack stack, float velocity, float inaccuracy) {
        EntityProjectile iprojectile = new EntityProjectile(world, pos.getX(), pos.getY(), pos.getZ());

        iprojectile.setItem(stack);
        iprojectile.setThrowDamage(getThrowDamage(stack));

        iprojectile.shoot(facing.getXOffset(), facing.getYOffset() + 0.1F, facing.getZOffset(), velocity, inaccuracy);

        world.spawnEntity(iprojectile);

        stack.shrink(1);

        return stack;
    }

    class DispenserBehaviour extends BehaviorDefaultDispenseItem {
        @Override
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            ITossable tossable = (ITossable)stack.getItem();

            if (tossable.canBeThrown(stack)) {
                return shootStack(source, stack);
            }

            return super.dispenseStack(source, stack);
        }

        public ItemStack shootStack(IBlockSource source, ItemStack stack) {
            return ((ITossable)stack.getItem()).toss(source.getWorld(),
                    BlockDispenser.getDispensePosition(source),
                    (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING),
                    stack, getProjectileInaccuracy(), getProjectileVelocity());
        }

        protected float getProjectileInaccuracy() {
            return 6.0F;
        }

        protected float getProjectileVelocity() {
            return 1.1F;
        }
    }
}
