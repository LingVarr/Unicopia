package com.minelittlepony.unicopia.magic.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.FollowCasterGoal;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.MagicEffect;

import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Box;

/**
 * Spike The Dragon, but in rock form.
 *
 * It follows you around and can pick up/carry other gems.
 *
 * @Deprecated Replace with a minion mob
 */
@Deprecated
public class FaithfulAssistantSpell extends AbstractSpell {

    private static final Box EFFECT_BOUNDS = new Box(-2, -2, -2, 2, 2, 2);

    @Nullable
    private MagicEffect piggyBackSpell;

    @Override
    public String getName() {
        return "drake";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
    }

    @Override
    public int getTint() {
        return 0xFAEBD7;
    }

    @Override
    public void setDead() {
        super.setDead();

        if (piggyBackSpell != null) {
            piggyBackSpell.setDead();
        }
    }

    @Override
    public boolean allowAI() {
        return true;
    }

    @Override
    public boolean isDead() {
        return super.isDead();
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || (piggyBackSpell != null && piggyBackSpell.isDirty());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPlaced(Caster<?> caster) {
        if (caster.getEntity() instanceof SpellcastEntity) {
            SpellcastEntity living = (SpellcastEntity)caster.getEntity();

            living.getNavigation().setCanSwim(false);
            living.getGoals().add(1, new SwimGoal(living));
            living.getGoals().add(2, new FollowCasterGoal<>((Caster<SpellcastEntity>)caster, 1, 4, 70));

            living.setPos(living.getX(), living.getY(), living.getZ());
        }
    }

    @Override
    public boolean update(Caster<?> source) {
        if (piggyBackSpell == null) {
            Box bb = EFFECT_BOUNDS.offset(source.getOriginVector());

            source.getWorld().getEntities(source.getEntity(), bb, e -> e instanceof SpellcastEntity).stream()
                .map(i -> (SpellcastEntity)i)
                .filter(i -> i.hasEffect() && !(i.getEffect() instanceof FaithfulAssistantSpell))
                .findFirst().ifPresent(i -> {
                    piggyBackSpell = i.getEffect().copy();
                    piggyBackSpell.onPlaced(source);
                    i.setEffect(null);
                    setDirty(true);
                });
        }

        if (piggyBackSpell != null) {
            piggyBackSpell.update(source);
        }

        return true;
    }

    @Override
    public void render(Caster<?> source) {
        if (piggyBackSpell != null) {
            piggyBackSpell.render(source);
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (piggyBackSpell != null) {
            compound.put("effect", SpellRegistry.instance().serializeEffectToNBT(piggyBackSpell));
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        if (compound.contains("effect")) {
            if (piggyBackSpell != null) {
                piggyBackSpell.fromNBT(compound.getCompound("effect"));
            } else {
                piggyBackSpell = SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect"));
            }
        }
    }
}
