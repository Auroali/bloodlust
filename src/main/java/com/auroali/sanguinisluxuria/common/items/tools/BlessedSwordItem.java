package com.auroali.sanguinisluxuria.common.items.tools;

import com.auroali.sanguinisluxuria.common.registry.BLEntityAttributes;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;

public class BlessedSwordItem extends SwordItem {
    private final ImmutableMultimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public BlessedSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, float blessedDamage, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(
          EntityAttributes.GENERIC_ATTACK_DAMAGE,
          new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", this.getAttackDamage(), EntityAttributeModifier.Operation.ADDITION)
        );
        builder.put(
          EntityAttributes.GENERIC_ATTACK_SPEED,
          new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", attackSpeed, EntityAttributeModifier.Operation.ADDITION)
        );
        builder.put(
          BLEntityAttributes.BLESSED_DAMAGE,
          new EntityAttributeModifier(BLEntityAttributes.BLESSED_DAMAGE_UUID, "Weapon Modifier", blessedDamage, EntityAttributeModifier.Operation.ADDITION)
        );
        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }
}
