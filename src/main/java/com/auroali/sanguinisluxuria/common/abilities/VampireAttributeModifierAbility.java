package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class VampireAttributeModifierAbility extends VampireAbility {
    private final Map<EntityAttribute, VampireAttributeModifier> modifiers;

    protected VampireAttributeModifierAbility(Supplier<ItemStack> icon, VampireAbility parent, Map<EntityAttribute, VampireAttributeModifier> modifiers) {
        super(icon, parent);
        this.modifiers = modifiers;
    }

    @Override
    public void onAbilityRemoved(LivingEntity entity, VampireComponent vampire) {
        super.onAbilityRemoved(entity, vampire);
        AttributeContainer attributes = entity.getAttributes();
        modifiers.forEach((attribute, modifier) -> {
            EntityAttributeInstance instance = attributes.getCustomInstance(attribute);
            if(instance == null)
                return;

            if(instance.hasModifier(modifier.modifier()))
                instance.removeModifier(modifier.modifier());
        });
    }

    @Override
    public AbilityTicker<?> createTicker() {
        return checkType(VampireAttributeModifierAbility::tick);
    }

    public static void tick(VampireAttributeModifierAbility ability, World world, LivingEntity entity, VampireComponent component, VampireAbilityContainer container, BloodComponent blood) {
        AttributeContainer attributes = entity.getAttributes();
        ability.modifiers.forEach((attribute, modifier) -> {
            EntityAttributeInstance instance = attributes.getCustomInstance(attribute);
            if(instance == null)
                return;

            if(blood.getBlood() >= modifier.minBloodLevel() && !instance.hasModifier(modifier.modifier()))
                instance.addPersistentModifier(modifier.modifier());
            if(blood.getBlood() < modifier.minBloodLevel() && instance.hasModifier(modifier.modifier()))
                instance.removeModifier(modifier.modifier());
        });
    }

    public static VampireAttributeModifierAbilityBuilder builder(Supplier<ItemStack> icon, VampireAbility parent) {
        return new VampireAttributeModifierAbilityBuilder(icon, parent);
    }

    public static VampireAttributeModifierAbilityBuilder builder(Supplier<ItemStack> icon) {
        return new VampireAttributeModifierAbilityBuilder(icon, null);
    }

    public static class VampireAttributeModifierAbilityBuilder {
        private final Map<EntityAttribute, VampireAttributeModifier> modifiers = new HashMap<>();

        Supplier<ItemStack> icon;
        VampireAbility parent;

        protected VampireAttributeModifierAbilityBuilder(Supplier<ItemStack> icon, VampireAbility parent) {
            this.icon = icon;
            this.parent = parent;
        }

        public VampireAttributeModifierAbilityBuilder addModifier(EntityAttribute attribute, String uuid, double value, EntityAttributeModifier.Operation operation) {
            return addModifier(attribute, uuid, value, operation, 0);
        }

        public VampireAttributeModifierAbilityBuilder addModifier(EntityAttribute attribute, String uuid, double value, EntityAttributeModifier.Operation operation, int minBlood) {
            modifiers.put(attribute, new VampireAttributeModifier(
                    new EntityAttributeModifier(
                            UUID.fromString(uuid),
                            () -> "sanguinisluxuria.vampire_ability",
                            value,
                            operation
                    ),
                    minBlood
            ));
            return this;
        }

        public VampireAttributeModifierAbility build() {
            return new VampireAttributeModifierAbility(icon, parent, modifiers);
        }
    }
    protected record VampireAttributeModifier(EntityAttributeModifier modifier, int minBloodLevel) {}
}
