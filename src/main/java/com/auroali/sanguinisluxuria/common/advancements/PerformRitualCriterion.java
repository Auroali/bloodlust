package com.auroali.sanguinisluxuria.common.advancements;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.auroali.sanguinisluxuria.common.registry.BLRitualTypes;
import com.auroali.sanguinisluxuria.common.rituals.Ritual;
import com.auroali.sanguinisluxuria.common.rituals.RitualType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.item.ItemConvertible;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class PerformRitualCriterion extends AbstractCriterion<PerformRitualCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        if (obj.has("ritual")) {
            Identifier id = Identifier.tryParse(obj.get("ritual").getAsString());
            RitualType<?> ritualType = BLRegistries.RITUAL_TYPES.get(id);
            NbtCompound nbt = null;
            if (obj.has("nbt")) {
                nbt = NbtCompound.CODEC.parse(JsonOps.INSTANCE, obj.get("nbt"))
                  .resultOrPartial(Bloodlust.LOGGER::error)
                  .orElseThrow(() -> new JsonParseException("Failed to deserialize nbt"));
            }
            return new Conditions(ritualType, nbt, playerPredicate);
        }
        return new Conditions(null, null, playerPredicate);
    }

    public void trigger(ServerPlayerEntity player, Ritual ritual) {
        this.trigger(player, conditions -> conditions.test(ritual));
    }

    @Override
    public Identifier getId() {
        return BLResources.PERFORM_RITUAL_ID;
    }

    public static class Conditions extends AbstractCriterionConditions {
        final RitualType<?> ritual;
        final NbtCompound nbt;

        public Conditions(RitualType<?> ritual, NbtCompound nbt, LootContextPredicate entity) {
            super(BLResources.PERFORM_RITUAL_ID, entity);
            this.ritual = ritual;
            this.nbt = nbt;
        }

        @SuppressWarnings("unchecked")
        public boolean test(Ritual ritual) {
            if (this.ritual == null)
                return true;

            if (this.ritual != ritual.getType())
                return false;

            NbtCompound toCompare = ((Codec<Ritual>) ritual.getType().getCodec()).encodeStart(NbtOps.INSTANCE, ritual)
              .resultOrPartial(Bloodlust.LOGGER::error)
              .flatMap(nbtElement -> nbtElement instanceof NbtCompound compound ? Optional.of(compound) : Optional.empty())
              .orElse(null);

            return NbtHelper.matches(this.nbt, toCompare, true);
        }

        public static Conditions create() {
            return new Conditions(null, null, LootContextPredicate.EMPTY);
        }

        public static Conditions create(RitualType<?> ritual) {
            return new Conditions(ritual, null, LootContextPredicate.EMPTY);
        }

        public static Conditions createForItem(ItemConvertible item) {
            NbtCompound nbt = new NbtCompound();
            NbtCompound resultNbt = new NbtCompound();
            resultNbt.putString("id", Registries.ITEM.getId(item.asItem()).toString());
            nbt.put("result", resultNbt);
            return create(BLRitualTypes.ITEM_RITUAL_TYPE, nbt);
        }

        @SuppressWarnings("unchecked")
        public static Conditions create(Ritual ritual) {
            NbtCompound nbt = ((Codec<Ritual>) ritual.getType().getCodec())
              .encodeStart(NbtOps.INSTANCE, ritual)
              .resultOrPartial(Bloodlust.LOGGER::error)
              .flatMap(element -> element instanceof NbtCompound compound ? Optional.of(compound) : Optional.empty())
              .orElse(null);
            return new Conditions(ritual.getType(), nbt, LootContextPredicate.EMPTY);
        }

        public static Conditions create(RitualType<?> ritual, NbtCompound compound) {
            return new Conditions(ritual, compound, LootContextPredicate.EMPTY);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject object = super.toJson(predicateSerializer);
            if (this.ritual != null) {
                object.addProperty("ritual", BLRegistries.RITUAL_TYPES.getId(this.ritual).toString());
                if (this.nbt != null) {
                    NbtCompound.CODEC.encodeStart(JsonOps.INSTANCE, this.nbt)
                      .resultOrPartial(Bloodlust.LOGGER::error)
                      .ifPresent(element -> object.add("nbt", element));
                }
            }
            return object;
        }
    }
}
