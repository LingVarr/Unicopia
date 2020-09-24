package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Suppressable;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

/**
 * A spell for revealing changelings.
 */
public class RevealingSpell extends AbstractSpell {

    @Override
    public String getName() {
        return "reveal";
    }

    @Override
    public int getTint() {
        return 0x5CE81F;
    }

    @Override
    public void onPlaced(Caster<?> source) {
        source.setCurrentLevel(1);
    }

    @Override
    public boolean update(Caster<?> source) {
        source.findAllSpellsInRange(15).forEach(e -> {
            Suppressable spell = e.getSpell(Suppressable.class, false);

            if (spell != null && spell.isVulnerable(source, this)) {
                spell.onSuppressed(source);
                source.getWorld().playSound(null, e.getOrigin(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.2F, 0.5F);
            }
        });

        return true;
    }

    @Override
    public void render(Caster<?> source) {
        Shape area = new Sphere(false, 15);

        MagicParticleEffect effect = new MagicParticleEffect(getTint());

        source.spawnParticles(area, 5, pos -> {
            source.addParticle(effect, pos, Vec3d.ZERO);
        });
        source.spawnParticles(effect, 5);
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
    }

}