package com.auroali.sanguinisluxuria.common.items;

import com.auroali.sanguinisluxuria.common.registry.BLEntityAttributes;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class PendantOfPiercingItem extends TrinketItem {
    public PendantOfPiercingItem(Settings settings) {
        super(settings);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = super.getModifiers(stack, slot, entity, uuid);
        modifiers.put(BLEntityAttributes.BLINK_RANGE, new EntityAttributeModifier(uuid, "sanguinisluxuria.reduce_range", -0.25, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(BLEntityAttributes.BLINK_COOLDOWN, new EntityAttributeModifier(uuid, "sanguinisluxuria.increase_cooldown", 0.25, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }
}
