package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class BLEntityAttributes {
    public static final UUID BLESSED_DAMAGE_UUID = UUID.fromString("514cfa77-6424-4ca6-b938-3d0b7b6e0e29");
    public static final EntityAttribute BLESSED_DAMAGE = new ClampedEntityAttribute(transFromId(BLResources.BLESSED_DAMAGE_ID), 0.0f, 0.0, 2048);
    public static final EntityAttribute BLINK_COOLDOWN = new ClampedEntityAttribute(transFromId(BLResources.BLINK_COOLDOWN_ID), 250, 0, 2048);
    public static final EntityAttribute BLINK_RANGE = new ClampedEntityAttribute(transFromId(BLResources.BLINK_RANGE_ID), 12, 0, 2048);
    public static final EntityAttribute SUN_RESISTANCE = new ClampedEntityAttribute(transFromId(BLResources.SUN_RESISTANCE_ID), 1.f, 0.f, 2.f);

    public static void register() {
        Registry.register(Registries.ATTRIBUTE, BLResources.BLESSED_DAMAGE_ID, BLESSED_DAMAGE);
        Registry.register(Registries.ATTRIBUTE, BLResources.BLINK_COOLDOWN_ID, BLINK_COOLDOWN);
        Registry.register(Registries.ATTRIBUTE, BLResources.BLINK_RANGE_ID, BLINK_RANGE);
        Registry.register(Registries.ATTRIBUTE, BLResources.SUN_RESISTANCE_ID, SUN_RESISTANCE);
    }

    public static String transFromId(Identifier id) {
        return "generic." + id.getNamespace() + "." + id.getPath();
    }
}
