package com.minelittlepony.unicopia.ability.magic.spell.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.ProjectileSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvents;

public class FireBoltSpell extends AbstractSpell implements ProjectileSpell, HomingSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 10)
            .with(Trait.CHAOS, 1)
            .with(Trait.STRENGTH, 11)
            .with(Trait.FIRE, 60)
            .build();
    public static final SpellTraits HOMING_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 50)
            .with(Trait.CHAOS, 10)
            .with(Trait.STRENGTH, 11)
            .with(Trait.FIRE, 60)
            .build();

    @Nullable
    private Entity target;

    protected FireBoltSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, Entity entity) {
        entity.setOnFireFor(90);
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        if (situation == Situation.PROJECTILE) {
            if (caster instanceof MagicProjectileEntity && getTraits().get(Trait.FOCUS) >= 50) {
                caster.findAllEntitiesInRange(
                    getTraits().get(Trait.FOCUS) - 49,
                    EntityPredicates.VALID_LIVING_ENTITY.and(TargetSelecter.notOwnerOrFriend(this, caster))
                ).findFirst().ifPresent(target -> {
                    ((MagicProjectileEntity)caster).setHomingTarget(target);
                });
            }

            return true;
        }

        if (getTraits().get(Trait.FOCUS) >= 50 && target == null) {
            target = caster.findAllEntitiesInRange(
                getTraits().get(Trait.FOCUS) - 49,
                EntityPredicates.VALID_LIVING_ENTITY.and(TargetSelecter.notOwnerOrFriend(this, caster))
            ).findFirst().orElse(null);
        }

        for (int i = 0; i < getNumberOfBalls(caster); i++) {
            getType().create(getTraits()).toThrowable().throwProjectile(caster, 2).ifPresent(c -> {
                c.setHomingTarget(target);
            });

            caster.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 0.7F, 0.4F / (caster.getWorld().random.nextFloat() * 0.4F + 0.8F));
        }
        return false;
    }

    @Override
    public void configureProjectile(MagicProjectileEntity projectile, Caster<?> caster) {
        projectile.setItem(Items.FIRE_CHARGE.getDefaultStack());
        projectile.addThrowDamage(getTraits().get(Trait.POWER, 0, getTraits().get(Trait.FOCUS) >= 50 ? 500 : 50) / 10F);
        projectile.setFireTicks(900000);
        projectile.setVelocity(projectile.getVelocity().multiply(1.3 + getTraits().get(Trait.STRENGTH) / 11F));
    }

    protected int getNumberOfBalls(Caster<?> caster) {
        return 1 + caster.getWorld().random.nextInt(3) + (int)getTraits().get(Trait.EARTH) * 3;
    }

    @Override
    public boolean setTarget(Entity target) {
        if (getTraits().get(Trait.FOCUS) >= 50) {
            this.target = target;
            return true;
        }
        return false;
    }
}