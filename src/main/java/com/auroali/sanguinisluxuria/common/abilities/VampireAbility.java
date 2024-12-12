package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class VampireAbility {
    private final List<VampireAbilityCondition> conditions = new ArrayList<>();
    private String transKey;
    private String descTransKey;
    private final RegistryEntry.Reference<VampireAbility> holder = BLRegistries.VAMPIRE_ABILITIES.createEntry(this);

    public VampireAbility() {
    }

    /**
     * Creates a ticker for this ability
     */
    public AbilityTicker<?> createTicker() {
        return null;
    }

    public <T extends VampireAbility> AbilityTicker<?> checkType(AbilityTicker<T> ticker) {
        return ticker;
    }

    public RegistryEntry.Reference<VampireAbility> getRegistryEntry() {
        return this.holder;
    }

    public boolean isIn(TagKey<VampireAbility> tag) {
        return this.getRegistryEntry().isIn(tag);
    }

    /**
     * Adds a condition for this ability to be visible
     *
     * @param condition the condition to add
     * @return this ability, to allow chaining more functions
     */
    public VampireAbility condition(VampireAbilityCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    /**
     * @return the ability's translation key, usually in the form 'vampire_ability.modid.ability_id'
     */
    public String getTranslationKey() {
        if (this.transKey == null && this.getRegistryEntry().getKey().isPresent()) {
            Identifier id = this.getRegistryEntry().getKey().get().getValue();
            this.transKey = "vampire_ability.%s.%s".formatted(id.getNamespace(), id.getPath());
        }
        return this.transKey == null ? "" : this.transKey;
    }

    public String getDescTranslationKey() {
        return this.descTransKey == null ? this.descTransKey = this.getTranslationKey() + ".desc" : this.descTransKey;
    }


    /**
     * Activates the ability when the bound key is pressed
     *
     * @param entity    the entity using the ability
     * @param component the entity's vampire component
     */
    public void activate(LivingEntity entity, VampireComponent component) {
    }

    /**
     * If this ability's cooldown can be ticked
     *
     * @param entity           the entity with the ability
     * @param vampireComponent the entity's vampire component
     * @return if this ability's cooldown can be ticked
     * @see VampireTeleportAbility#canTickCooldown(LivingEntity, VampireComponent)
     */
    public boolean canTickCooldown(LivingEntity entity, VampireComponent vampireComponent) {
        return true;
    }

    /**
     * Called when an entity's state changes from vampire to non-vampire
     *
     * @param entity  the entity
     * @param vampire the entity's vampire component
     */
    public void onUnVampire(LivingEntity entity, VampireComponent vampire) {
        this.onAbilityRemoved(entity, vampire);
    }

    /**
     * Called when this ability is removed
     *
     * @param entity  the entity that used to have this ability
     * @param vampire the entity's vampire component
     */
    public void onAbilityRemoved(LivingEntity entity, VampireComponent vampire) {
    }

    public boolean testConditions(LivingEntity entity, VampireComponent component, VampireAbilityContainer container) {
        for (VampireAbilityCondition condition : this.conditions) {
            if (!condition.test(entity, component, container))
                return false;
        }
        return true;
    }

    @FunctionalInterface
    public interface VampireAbilityCondition {
        boolean test(LivingEntity entity, VampireComponent vampire, VampireAbilityContainer container);
    }

    @FunctionalInterface
    public interface AbilityTicker<T extends VampireAbility> {
        void tick(T ability, World world, LivingEntity entity, VampireComponent component, VampireAbilityContainer container, BloodComponent blood);
    }
}
