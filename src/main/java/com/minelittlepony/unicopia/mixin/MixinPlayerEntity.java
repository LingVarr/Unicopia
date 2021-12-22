package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.entity.CapeHolder;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.datafixers.util.Either;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
abstract class MixinPlayerEntity extends LivingEntity implements PonyContainer<Pony>, CapeHolder {
    private MixinPlayerEntity() { super(null, null); }
    @Override
    @Invoker("updateCapeAngles")
    public abstract void callUpdateCapeAngles();

    @Override
    public Equine<?> create() {
        return new Pony((PlayerEntity)(Object)this);
    }

    @ModifyVariable(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"), ordinal = 0)
    protected float modifyDamageAmount(float amount, DamageSource source) {
        return get().modifyDamage(source, amount).orElse(amount);
    }

    @Inject(method = "handleFallDamage(FFLnet/minecraft/entity/damage/DamageSource;)Z", at = @At("HEAD"), cancellable = true)
    private void onHandleFallDamage(float distance, float damageMultiplier, DamageSource cause, CallbackInfoReturnable<Boolean> info) {
        get().onImpact(fallDistance, damageMultiplier, cause).ifPresent(newDistance -> {
            PlayerEntity self = (PlayerEntity)(Object)this;

            if (distance >= 2) {
                self.increaseStat(Stats.FALL_ONE_CM, Math.round(distance * 100));
            }

            info.setReturnValue(super.handleFallDamage(newDistance, damageMultiplier, cause));
        });
    }

    @Inject(method = "createPlayerAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;", at = @At("RETURN"))
    private static void onCreateAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        Pony.registerAttributes(info.getReturnValue());
    }

    @Inject(method = "eatFood(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"))
    private void onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> info) {
        if (stack.isFood()) {
            get().onEat(stack);
        }
    }

    @Inject(method = "trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;",
            at = @At("HEAD"),
            cancellable = true)
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {
        if (!world.isClient) {
            get().trySleep(pos).ifPresent(reason -> {
                ((PlayerEntity)(Object)this).sendMessage(reason, true);

                info.setReturnValue(Either.right(Unit.INSTANCE));
            });
        }
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At("RETURN"))
    private void onDropItem(ItemStack itemStack_1, boolean scatter, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> info) {
        Equine.of(info.getReturnValue()).ifPresent(eq -> {
            eq.setSpecies(get().getSpecies());
            eq.getPhysics().setBaseGravityModifier(get().getPhysics().getGravityModifier());
        });
    }

    @Inject(method = "getActiveEyeHeight(Lnet/minecraft/entity/EntityPose;Lnet/minecraft/entity/EntityDimensions;)F",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetActiveEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> info) {
        get().getMotion().getDimensions().calculateActiveEyeHeight(dimensions).ifPresent(info::setReturnValue);
    }

    @Inject(method = "getDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> info) {
        get().getMotion().getDimensions().calculateDimensions().ifPresent(info::setReturnValue);
    }

    @Inject(method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetBlockBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> info) {
        info.setReturnValue(info.getReturnValue() * get().getBlockBreakingSpeed());
    }
}
