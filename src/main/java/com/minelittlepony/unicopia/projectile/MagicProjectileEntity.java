package com.minelittlepony.unicopia.projectile;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.UEntities;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.ability.magic.Magical;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;
import com.minelittlepony.unicopia.entity.EntityPhysics;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A generalised version of Mojang's projectile entity class with added support for a custom appearance and water phobia.
 *
 * Can also carry a spell if needed.
 */
public class MagicProjectileEntity extends ThrownItemEntity implements Magical, Caster<LivingEntity> {

    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> GRAVITY = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> HYDROPHOBIC = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(MagicProjectileEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);
    private static final LevelStore LEVELS = Levelled.fixed(1);

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private final EntityPhysics<MagicProjectileEntity> physics = new EntityPhysics<>(this, GRAVITY, false);

    private BlockPos lastBlockPos;

    public MagicProjectileEntity(EntityType<MagicProjectileEntity> type, World world) {
        super(type, world);
    }

    public MagicProjectileEntity(World world, LivingEntity thrower) {
        super(UEntities.THROWN_ITEM, thrower, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(GRAVITY, 1F);
        getDataTracker().startTracking(DAMAGE, 0F);
        getDataTracker().startTracking(EFFECT, new CompoundTag());
        getDataTracker().startTracking(HYDROPHOBIC, false);
    }

    @Override
    protected Item getDefaultItem() {
        Spell spell = this.getSpell(false);
        return spell == null ? Items.AIR : spell.getAffinity() == Affinity.BAD ? Items.MAGMA_CREAM : Items.SNOWBALL;
     }

    @Override
    public Entity getEntity() {
        return this;
    }

    @Override
    public void setMaster(LivingEntity owner) {
        setOwner(owner);
    }

    @Override
    public LivingEntity getMaster() {
        return (LivingEntity)getOwner();
    }

    @Override
    public LevelStore getLevel() {
        return LEVELS;
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public Affinity getAffinity() {
        return hasSpell() ? Affinity.NEUTRAL : getSpell(true).getAffinity();
    }

    @Override
    public EffectSync getPrimarySpellSlot() {
        return effectDelegate;
    }

    @Override
    public void setSpell(Spell effect) {
        Caster.super.setSpell(effect);

        if (effect != null) {
            effect.onPlaced(this);
        }
    }

    public void setThrowDamage(float damage) {
        getDataTracker().set(DAMAGE, Math.max(0, damage));
    }

    public float getThrowDamage() {
        return getDataTracker().get(DAMAGE);
    }

    public void setHydrophobic() {
        getDataTracker().set(HYDROPHOBIC, true);
    }

    public boolean getHydrophobic() {
        return getDataTracker().get(HYDROPHOBIC);
    }

    @Override
    public void tick() {
        if (!world.isClient()) {
            if (Math.abs(getVelocity().x) < 0.01 && Math.abs(getVelocity().x) < 0.01 && Math.abs(getVelocity().y) < 0.01) {
                remove();
            }
        }

        super.tick();

        if (age % 1000 == 0) {
            setNoGravity(false);
        }

        if (hasSpell()) {
            if (lastBlockPos == null || !lastBlockPos.equals(getBlockPos())) {
                lastBlockPos = getBlockPos();
            }

            Spell spell = getSpell(true);

            if (spell.isDead()) {
                remove();
            } else {
                spell.update(this);

                if (world.isClient()) {
                    spell.render(this);
                }
            }
        }

        if (getHydrophobic()) {
            if (world.getBlockState(getBlockPos()).getMaterial().isLiquid()) {
                Vec3d vel = getVelocity();

                double velY = vel.y;

                velY *= -1;

                if (!hasNoGravity()) {
                    velY += 0.16;
                }

                setVelocity(new Vec3d(vel.x, velY, vel.z));
            }
        }
    }

    private ParticleEffect getParticleParameters() {
       ItemStack stack = getItem();

       if (stack.isEmpty()) {
           return ParticleTypes.ITEM_SNOWBALL;
       }

       if (stack.getItem() == UItems.FILLED_JAR) {
           stack = UItems.EMPTY_JAR.getDefaultStack();
       }

       return new ItemStackParticleEffect(ParticleTypes.ITEM, stack);
    }

    @Override
    public void handleStatus(byte id) {
       if (id == 3) {
          ParticleEffect effect = getParticleParameters();

          for(int i = 0; i < 8; i++) {
             world.addParticle(effect, getX(), getY(), getZ(), 0, 0, 0);
          }
       }

    }

    @Override
    public void readCustomDataFromTag(CompoundTag compound) {
        super.readCustomDataFromTag(compound);
        physics.fromNBT(compound);
        if (compound.contains("effect")) {
            setSpell(SpellType.fromNBT(compound.getCompound("effect")));
        }
    }

    @Override
    public void writeCustomDataToTag(CompoundTag compound) {
        super.writeCustomDataToTag(compound);
        physics.toNBT(compound);

        if (hasSpell()) {
            compound.put("effect", SpellType.toNBT(getSpell(true)));
        }
    }

    @Override
    protected void onCollision(HitResult result) {
        if (!removed) {
            remove();
            super.onCollision(result);

            if (!world.isClient()) {
                world.sendEntityStatus(this, (byte)3);
                remove();
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        if (hasSpell()) {
            Spell effect = getSpell(true);

            if (effect instanceof ProjectileDelegate) {
                ((ProjectileDelegate)effect).onImpact(this, hit.getBlockPos(), world.getBlockState(hit.getBlockPos()));
            }
        }

        if (getItem().getItem() instanceof ProjectileDelegate) {
            ((ProjectileDelegate)getItem().getItem()).onImpact(this, hit.getBlockPos(), world.getBlockState(hit.getBlockPos()));
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        Entity entity = hit.getEntity();

        if (entity instanceof ProjectileEntity) {
            return;
        }

        if (entity != null) {
            entity.damage(DamageSource.thrownProjectile(this, getOwner()), getThrowDamage());
        }

        if (getItem().getItem() instanceof ProjectileDelegate) {
            ((ProjectileDelegate)getItem().getItem()).onImpact(this, entity);
        }
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return Channel.SERVER_SPAWN_PROJECTILE.toPacket(new MsgSpawnProjectile(this));
    }
}
