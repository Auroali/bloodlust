package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class VampireAbility {
    private final VampireAbility parent;
    private final Supplier<ItemStack> icon;
    private final List<Supplier<VampireAbility>> incompatibilities;
    private final List<VampireAbilityCondition> conditions = new ArrayList<>();
    private int skillPoints;
    private String transKey;
    private String descTransKey;
    private final RegistryEntry.Reference<VampireAbility> holder = BLRegistries.VAMPIRE_ABILITIES.createEntry(this);

    public VampireAbility(Supplier<ItemStack> icon, VampireAbility parent) {
        this.icon = icon;
        this.parent = parent;
        this.incompatibilities = new ArrayList<>();
        this.skillPoints = 1;
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

    /**
     * @return whether this ability can be bound to a key
     */
    public boolean isKeybindable() {
        return false;
    }

    public boolean isIn(TagKey<VampireAbility> tag) {
        return this.getRegistryEntry().isIn(tag);
    }

    /**
     * Whether this ability should be hidden from an entity.
     * A hidden ability cannot be unlocked or viewed in the skill tree.
     *
     * @param entity the entity to check
     * @return if this ability should be hidden
     */
    public boolean isHidden(LivingEntity entity) {
        if (this.conditions.isEmpty())
            return false;
        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(entity);
        for (VampireAbilityCondition condition : this.conditions) {
            if (!condition.test(entity, vampire, vampire.getAbilties()))
                return true;
        }
        return false;
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
     * Gets this ability's parent
     *
     * @return this ability's parent, or null if it doesn't have one
     */
    public VampireAbility getParent() {
        return this.parent;
    }

    /**
     * Gets the icon for this ability
     *
     * @return the icon, as an item stack.
     * @implNote this calls the supplier passed in to the constructor
     * and does not cache the result
     */
    public ItemStack getIcon() {
        return this.icon.get();
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
     * Gets the amount of skill points required to unlock this ability
     *
     * @return the required skill points amount
     */
    public int getRequiredSkillPoints() {
        return this.skillPoints;
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
     * Returns if this ability is incompatible with another ability
     *
     * @param ability the other ability
     * @return if this ability is compatible with the other ability
     */
    public boolean incompatibleWith(VampireAbility ability) {
        return this.incompatibilities
          .stream()
          .map(Supplier::get)
          .anyMatch(a -> a == ability)
          || ability.incompatibilities
          .stream()
          .map(Supplier::get)
          .anyMatch(a -> a == ability);
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

    /**
     * Gets all abilities that this one is incompatible with
     *
     * @return a list of abilities this one is incompatible with
     */
    public List<VampireAbility> getIncompatibilities() {
        return this.incompatibilities.stream().map(Supplier::get).toList();
    }

    /**
     * Marks this ability as incompatible with another
     *
     * @param abilitySupplier the ability this one is incompatible with
     * @return this ability
     */
    public VampireAbility incompatible(Supplier<VampireAbility> abilitySupplier) {
        this.incompatibilities.add(abilitySupplier);
        return this;
    }

    /**
     * Sets the amount of skill points required to unlock this ability
     *
     * @param points the amount of skill points required for this ability
     * @return this ability
     */
    public VampireAbility skillPoints(int points) {
        this.skillPoints = points;
        return this;
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
