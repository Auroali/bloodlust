package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.world.World;

import java.util.*;

public class VampireAttributeModifierAbility extends VampireAbility {
    private final Map<EntityAttribute, VampireAttributeModifier> modifiers;

    protected VampireAttributeModifierAbility(Map<EntityAttribute, VampireAttributeModifier> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public void onAbilityRemoved(LivingEntity entity, VampireComponent vampire) {
        super.onAbilityRemoved(entity, vampire);
        AttributeContainer attributes = entity.getAttributes();
        this.modifiers.forEach((attribute, modifier) -> {
            EntityAttributeInstance instance = attributes.getCustomInstance(attribute);
            if (instance == null)
                return;

            if (instance.hasModifier(modifier.modifier()))
                instance.removeModifier(modifier.modifier());
        });
    }

    @Override
    public AbilityTicker<?> createTicker() {
        return this.checkType(VampireAttributeModifierAbility::tick);
    }

    public static void tick(VampireAttributeModifierAbility ability, World world, LivingEntity entity, VampireComponent component, VampireAbilityContainer container, BloodComponent blood) {
        ability.modifiers.forEach((attribute, modifier) ->
          VampireHelper.applyModifierFromBlood(entity, attribute, modifier.modifier(), blood, b -> b.getBlood() >= modifier.minBloodLevel())
        );
    }

    public static VampireAttributeModifierAbilityBuilder builder(UUID uuid) {
        return new VampireAttributeModifierAbilityBuilder(uuid);
    }

    public static class VampireAttributeModifierAbilityBuilder {
        private final Map<EntityAttribute, VampireAttributeModifier> modifiers = new HashMap<>();
        private final List<VampireAbilityCondition> conditions = new ArrayList<>();
        private final UUID uuid;

        protected VampireAttributeModifierAbilityBuilder(UUID uuid) {
            this.uuid = uuid;
        }

        public VampireAttributeModifierAbilityBuilder addModifier(EntityAttribute attribute, double value, EntityAttributeModifier.Operation operation) {
            return this.addModifier(attribute, value, operation, 0);
        }

        public VampireAttributeModifierAbilityBuilder addModifier(EntityAttribute attribute, double value, EntityAttributeModifier.Operation operation, int minBlood) {
            this.modifiers.put(attribute, new VampireAttributeModifier(
              new EntityAttributeModifier(
                this.uuid,
                () -> "sanguinisluxuria.vampire_ability",
                value,
                operation
              ),
              minBlood
            ));
            return this;
        }

        public VampireAttributeModifierAbilityBuilder condition(VampireAbilityCondition condition) {
            this.conditions.add(condition);
            return this;
        }

        public VampireAttributeModifierAbility build() {
            VampireAttributeModifierAbility ability = new VampireAttributeModifierAbility(this.modifiers);
            for (VampireAbilityCondition condition : this.conditions) {
                ability.condition(condition);
            }
            return ability;
        }
    }

    protected record VampireAttributeModifier(EntityAttributeModifier modifier, int minBloodLevel) {
    }
}
