package com.auroali.bloodlust.common.registry;

import com.auroali.bloodlust.BLResources;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public class BLTags {
    public static class Entities {
        public static final TagKey<EntityType<?>> HAS_BLOOD = TagKey.of(Registry.ENTITY_TYPE_KEY, BLResources.HAS_BLOOD_ID);
    }

    public static class Items {
        public static final TagKey<Item> VAMPIRE_FOOD = TagKey.of(Registry.ITEM_KEY, BLResources.VAMPIRE_FOOD_ID);
    }
}
