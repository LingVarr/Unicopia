package com.minelittlepony.unicopia.ability;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public interface Ability<T extends Hit> {

    /**
     * The amount of energy this ability is expected to cost if the player were to cast it.
     */
    double getCostEstimate(Pony player);

    /**
     * Returns the number of ticks the player must hold the ability key to trigger this ability.
     */
    int getWarmupTime(Pony player);

    /**
     * Returns the number of ticks allowed for cooldown
     */
    int getCooldownTime(Pony player);

    /**
     * Called to check preconditions for activating the ability.
     *
     * @param w         The world
     * @param player    The player
     * @return  True to allow activation
     */
    default boolean canActivate(World w, Pony player) {
        return true;
    }

    /**
     * Checks if the given race is permitted to use this ability
     * @param playerSpecies The player's species
     */
    boolean canUse(Race playerSpecies);

    /**
     * Called on the client to activate the ability.
     *
     * @param player    The player activating the ability
     * @return  Data to be sent, or null if activation failed
     */
    @Nullable
    T tryActivate(Pony player);

    Hit.Serializer<T> getSerializer();

    /**
     * The icon representing this ability on the UI and HUD.
     * @return
     */
    default Identifier getIcon(Pony player) {
        Identifier id = Abilities.REGISTRY.getId(this);
        return new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath() + ".png");
    }

    /**
     * Called to actually apply the ability.
     * Only called on the server side.
     *
     * @param player    The player that triggered the ability
     * @param data      Data previously sent from the client
     */
    void apply(Pony player, T data);

    /**
     * Called every tick until the warmup timer runs out.
     * @param player    The current player
     */
    void preApply(Pony player, AbilitySlot slot);

    /**
     * Called every tick until the cooldown timer runs out.
     * @param player    The current player
     */
    void postApply(Pony player, AbilitySlot slot);
}
