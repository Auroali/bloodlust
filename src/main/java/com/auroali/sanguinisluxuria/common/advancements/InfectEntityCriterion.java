package com.auroali.sanguinisluxuria.common.advancements;

import com.auroali.sanguinisluxuria.BLResources;
import com.google.common.base.Predicates;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class InfectEntityCriterion extends AbstractCriterion<InfectEntityCriterion.Conditions> {
    @Override
    protected InfectEntityCriterion.Conditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(playerPredicate);
    }

    @Override
    public Identifier getId() {
        return BLResources.INFECT_ENTITY_ID;
    }

    public void trigger(ServerPlayerEntity entity) {
        this.trigger(entity, Predicates.alwaysTrue());
    }

    public static class Conditions extends AbstractCriterionConditions {

        public Conditions(LootContextPredicate entity) {
            super(BLResources.INFECT_ENTITY_ID, entity);
        }

        public static Conditions create() {
            return new Conditions(LootContextPredicate.EMPTY);
        }
    }
}
