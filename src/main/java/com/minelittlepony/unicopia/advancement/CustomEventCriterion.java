package com.minelittlepony.unicopia.advancement;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CustomEventCriterion extends AbstractCriterion<CustomEventCriterion.Conditions> {

    private static final Identifier ID = Unicopia.id("custom");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {
        return new Conditions(
                playerPredicate,
                JsonHelper.getString(json, "event"),
                RacePredicate.fromJson(json.get("race")),
                json.has("flying") ? json.get("flying").getAsBoolean() : null,
                JsonHelper.getInt(json, "repeats", 0)
        );
    }

    public CustomEventCriterion.Trigger createTrigger(String name) {
        return player -> {
            if (player instanceof ServerPlayerEntity p) {
                int counter = Pony.of(p).getAdvancementProgress().compute(name, (key, i) -> i == null ? 1 : i + 1);

                trigger(p, c -> c.test(name, counter, p));
            }
        };
    }

    public interface Trigger {
        void trigger(@Nullable Entity player);
    }

    public static Conditions create(String name) {
        return new Conditions(LootContextPredicate.EMPTY, name, RacePredicate.EMPTY, null, 1);
    }

    public static Conditions create(String name, int count) {
        return new Conditions(LootContextPredicate.EMPTY, name, RacePredicate.EMPTY, null, count);
    }

    public static Conditions createFlying(String name) {
        return new Conditions(LootContextPredicate.EMPTY, name, RacePredicate.EMPTY, true, 1);
    }

    public static Conditions createFlying(String name, int count) {
        return new Conditions(LootContextPredicate.EMPTY, name, RacePredicate.EMPTY, true, count);
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final String event;

        private final RacePredicate races;

        private final Boolean flying;

        private final int repeatCount;

        public Conditions(LootContextPredicate playerPredicate, String event, RacePredicate races, Boolean flying, int repeatCount) {
            super(ID, playerPredicate);
            this.event = event;
            this.races = races;
            this.flying = flying;
            this.repeatCount = repeatCount;
        }

        public boolean test(String event, int count, ServerPlayerEntity player) {
            return this.event.equalsIgnoreCase(event)
                    && races.test(player)
                    && (flying == null || flying == Pony.of(player).getPhysics().isFlying())
                    && (repeatCount <= 0 || (count > 0 && count % repeatCount == 0));
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.addProperty("event", event);
            if (!races.isEmpty()) {
                json.add("race", races.toJson());
            }
            if (flying != null) {
                json.addProperty("flying", flying);
            }
            if (repeatCount > 1) {
                json.addProperty("repeats", repeatCount);
            }
            return json;
        }
    }
}
