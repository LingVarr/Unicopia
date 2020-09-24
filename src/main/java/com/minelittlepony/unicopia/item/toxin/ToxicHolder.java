package com.minelittlepony.unicopia.item.toxin;

import java.util.Optional;

import net.minecraft.item.FoodComponent;

public interface ToxicHolder {
    void setFood(FoodComponent food);

    void setToxic(Toxic toxic);

    default Optional<Toxic> getToxic() {
        return Optional.empty();
    }
}