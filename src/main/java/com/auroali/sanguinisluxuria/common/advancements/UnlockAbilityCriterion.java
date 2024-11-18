package com.auroali.sanguinisluxuria.common.advancements;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UnlockAbilityCriterion extends AbstractCriterion<UnlockAbilityCriterion.Conditions> {
    @Override
    protected UnlockAbilityCriterion.Conditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        VampireAbility ability = null;
        if (obj.has("ability")) {
            Identifier identifier = Identifier.tryParse(obj.get("ability").getAsString());
            if (identifier != null)
                ability = BLRegistries.VAMPIRE_ABILITIES.get(identifier);
        }
        return new Conditions(playerPredicate, ability);
    }

    @Override
    public Identifier getId() {
        return BLResources.UNLOCK_VAMPIRE_ABILITY_ID;
    }

    public void trigger(ServerPlayerEntity entity, VampireAbility ability) {
        this.trigger(entity, e -> e.matches(ability));
    }

    public static class Conditions extends AbstractCriterionConditions {
        final VampireAbility ability;

        public Conditions(LootContextPredicate entity, VampireAbility ability) {
            super(BLResources.UNLOCK_VAMPIRE_ABILITY_ID, entity);
            this.ability = ability;
        }

        public static Conditions create() {
            return new Conditions(LootContextPredicate.EMPTY, null);
        }

        public static Conditions create(VampireAbility ability) {
            return new Conditions(LootContextPredicate.EMPTY, ability);
        }

        public boolean matches(VampireAbility ability) {
            return this.ability == null || this.ability == ability;
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject obj = super.toJson(predicateSerializer);
            if (ability != null) {
                ability.getRegistryEntry().getKey().ifPresent(k ->
                  obj.addProperty("ability", k.getValue().toString())
                );
            }
            return obj;
        }
    }
}
