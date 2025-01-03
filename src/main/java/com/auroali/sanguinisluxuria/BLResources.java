package com.auroali.sanguinisluxuria;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class BLResources {
    public static final Identifier HAS_BLOOD_ID = id("has_blood");
    public static final Identifier GOOD_BLOOD_ID = id("good_blood");
    public static final Identifier TOXIC_BLOOD_ID = id("toxic_blood");
    public static final Identifier BLOOD_COMPONENT_ID = id("blood");
    public static final Identifier VAMPIRE_COMPONENT_ID = id("vampire");
    public static final Identifier ICONS = id("textures/gui/icons.png");
    public static final Identifier MASK_ONE_ID = id("mask_1");
    public static final Identifier MASK_TWO_ID = id("mask_2");
    public static final Identifier MASK_THREE_ID = id("mask_3");
    public static final Identifier BLOOD_BAG_ID = id("blood_bag");
    public static final Identifier BLOOD_DRAIN_SOUND = id("drain_blood");
    public static final Identifier BLOOD_STORAGE_ITEM_MODEL_PREDICATE = id("blood_storage_item_fill");
    public static final Identifier VAMPIRES_GET_HUNGER_FROM_ID = id("vampires_get_hunger_from");
    public static final Identifier SUN_BLOCKING_HELMETS = id("sun_blocking_helmets");
    public static final Identifier BLOOD_BOTTLE_ID = id("blood_bottle");
    public static final Identifier ITEM_GROUP_ID = id(Bloodlust.MODID);
    public static final Identifier BLOOD_SICKNESS_ID = id("blood_sickness");
    public static final Identifier BLOOD_SPLATTER_ID = id("blood_splatter");
    public static final Identifier CAN_DROP_BLOOD = id("can_drop_blood");
    public static final Identifier BLESSED_WATER_ID = id("blessed_water");
    public static final Identifier BLOOD_PROTECTION_ID = id("blessed_blood");
    public static final Identifier VAMPIRE_MASKS_ID = id("vampire_masks");
    public static final Identifier VAMPIRE_ABILITY_REGISTRY_ID = id("vampire_ability");
    public static final Identifier VAMPIRE_ABILITY_ARGUMENT_ID = id("ability_argument");
    public static final Identifier TELEPORT_ID = id("teleport");
    public static final Identifier BECOME_VAMPIRE_CRITERION_ID = id("become_vampire");
    public static final Identifier ALTAR_ID = id("altar");
    public static final Identifier ALTAR_RECIPE_ID = id("ritual");
    public static final Identifier TWISTED_BLOOD_ID = id("twisted_blood_bottle");
    public static final Identifier PEDESTAL_ID = id("pedestal");
    public static final Identifier TRANSFER_EFFECTS_ID = id("transfer_effects");
    public static final Identifier BITE_ID = id("bite");
    public static final Identifier SUN_PROTECTION_ID = id("sun_protection");
    public static final Identifier BLOOD_DRAIN_ID = id("blood_drain");
    public static final Identifier ALTAR_BEATS_SOUND = id("altar_beats");
    public static final Identifier BLOOD_STILL = id("blood");
    public static final Identifier BLOOD_STILL_TEXTURE = id("block/blood_still");
    public static final Identifier BLOOD_FLOWING_TEXTURE = id("block/blood_flowing");
    public static final Identifier BLOOD_CAULDRON_ID = id("blood_cauldron");
    public static final Identifier UNLOCK_VAMPIRE_ABILITY_ID = id("unlock_ability");
    public static final Identifier RESET_ABILITIES_ID = id("reset_abilities");
    public static final Identifier ABILITY_SYNC_CHANNEL = id("ability_sync_id");
    public static final Identifier TELEPORTS_ON_DRAIN_ID = id("teleporting_blood");
    public static final Identifier BLEEDING_ID = id("bleeding");
    public static final Identifier BLEEDING_SOUND = id("bleeding");
    public static final Identifier INFECT_ENTITY_ID = id("infect_entity");
    public static final Identifier VAMPIRE_VILLAGER = id("vampire_villager");
    public static final Identifier VAMPIRE_VILLAGER_TEXTURE = id("textures/entity/vampire_villager.png");
    public static final Identifier VAMPIRE_MERCHANT_TEXTURE = id("textures/entity/vampire_merchant.png");
    public static final Identifier VAMPIRE_VILLAGER_SPAWN_EGG = id("vampire_villager_spawn_egg");
    public static final Identifier VAMPIRE_VILLAGER_SPAWN = id("vampire_villager_spawn");
    public static final Identifier VAMPIRE_CONVERT_SOUND = id("entity_converted_to_vampire");
    public static final Identifier PENDANT_OF_PIERCING = id("pendant_of_piercing");
    public static final Identifier UNBECOME_VAMPIRE_CRITERION_ID = id("unbecome_vampire");
    public static final Identifier BLESSED_WATER_TWO_ID = id("strong_blessed_water");
    public static final Identifier SERRATED_ID = id("serrated");
    public static final Identifier VAMPIRE_MERCHANT = id("vampire_merchant");
    public static final Identifier BLOOD_PETAL_ID = id("blood_petal");
    public static final Identifier SILVER_INGOT_ID = id("silver_ingot");
    public static final Identifier SILVER_BLOCK_ID = id("silver_block");
    public static final Identifier RAW_SILVER_ID = id("raw_silver");
    public static final Identifier SILVER_ORE_ID = id("silver_ore");
    public static final Identifier DEEPSLATE_SILVER_ORE_ID = id("deepslate_silver_ore");
    public static final Identifier SILVER_SHOVEL_ID = id("silver_shovel");
    public static final Identifier SILVER_PICKAXE_ID = id("silver_pickaxe");
    public static final Identifier SILVER_SWORD_ID = id("silver_sword");
    public static final Identifier SILVER_AXE_ID = id("silver_axe");
    public static final Identifier SILVER_HOE_ID = id("silver_hoe");
    public static final Identifier BLESSED_DAMAGE_ID = id("blessed_damage");
    public static final Identifier RAW_SILVER_BLOCK_ID = id("raw_silver_block");
    public static final RegistryKey<DamageType> BLOOD_DRAIN_DAMAGE_KEY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("blood_drain"));
    public static final RegistryKey<DamageType> BLESSED_WATER_DAMAGE_KEY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("blessed_water"));
    public static final RegistryKey<DamageType> BITE_DAMAGE_KEY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("bite"));
    public static final RegistryKey<DamageType> TELEPORT_DAMAGE_KEY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("blink_piercing"));
    public static final Identifier VAMPIRES_WEAK_TO_ID = id("vampires_weak_to");
    public static final Identifier ACTIVATE_ABILITY_C2S = id("activate_ability");
    public static final Identifier DRAIN_BLOOD_C2S = id("drain_blood");
    public static final Identifier ALTAR_RECIPE_START_S2C = id("altar_recipe_start");
    public static final Identifier BLOOD_CAULDRON_FILL_ID = id("blood_cauldron_fill");
    public static final Identifier BLINK_COOLDOWN_ID = id("blink_cooldown");
    public static final Identifier BLINK_RANGE_ID = id("blink_range");
    public static final Identifier HUNGRY_DECAYED_LOG = id("hungry_decayed_log");
    public static final Identifier STRIPPED_HUNGRY_DECAYED_LOG = id("stripped_hungry_decayed_log");
    public static final Identifier DECAYED_LOG = id("decayed_log");
    public static final Identifier STRIPPED_DECAYED_LOG = id("stripped_decayed_log");
    public static final Identifier DECAYED_WOOD = id("decayed_wood");
    public static final Identifier STRIPPED_DECAYED_WOOD = id("stripped_decayed_wood");
    public static final Identifier DECAYED_LOGS_ID = id("decayed_logs");
    public static final Identifier DECAYED_TWIGS = id("decayed_twigs");
    public static final Identifier GRAFTED_SAPLING = id("grafted_sapling");
    public static final Identifier HUNGRY_DECAYED_LOG_VFX_S2C = id("hungry_decayed_log_vfx");
    public static final Identifier SILVER_BLOCK_SET = id("silver");
    public static final Identifier DECAYED_WOOD_BLOCK_SET = id("decayed");
    public static final Identifier SILVER_PRESSURE_PLATE = id("silver_pressure_plate");
    public static final Identifier DECAYED_PRESSURE_PLATE = id("decayed_pressure_plate");
    public static final Identifier HUNGRY_DECAYED_LOGS_ID = id("hungry_decayed_logs");
    public static final Identifier DRIPPING_BLOOD = id("dripping_blood");
    public static final Identifier FALLING_BLOOD = id("falling_blood");
    public static final Identifier LANDING_BLOOD = id("landing_blood");
    public static final Identifier BLOOD_STORING_BOTTLES = id("blood_storing_bottles");
    public static final Identifier RITUAL_TYPE_REGISTRY_ID = id("ritual_types");
    public static final Identifier ITEM_RITUAL_TYPE = id("item_ritual");
    public static final Identifier ABILITY_RITUAL_TYPE = id("ability");
    public static final Identifier ABILITY_RESET_RITUAL_TYPE = id("reset_abilities");
    public static final Identifier ALTAR_BEAT_PARTICLE = id("altar_beat");
    public static final Identifier SPAWN_ALTAR_BEAT_PARTICLE_S2C = id("spawn_altar_beat_particle_s2c");
    public static final Identifier DECAYED_TREE = id("decayed_tree");
    public static final Identifier ABILITY_REVEAL_RITUAL_TYPE = id("ability_reveal");
    public static final Identifier ENTITY_SPAWNING_RITUAL_TYPE = id("entity");
    public static final Identifier STATUS_EFFECT_RITUAL_TYPE = id("effect");
    public static final Identifier PERFORM_RITUAL_ID = id("perform_ritual");
    public static final Identifier DECAYED_PLANKS = id("decayed_planks");
    public static final Identifier DECAYED_FENCE = id("decayed_fence");
    public static final Identifier DECAYED_FENCE_GATE = id("decayed_fence_gate");
    public static final Identifier DECAYED_STAIRS = id("decayed_stairs");
    public static final Identifier DECAYED_SLAB = id("decayed_slab");
    public static final Identifier DECAYED_SIGN = id("decayed_sign");
    public static final Identifier DECAYED_WALL_SIGN = id("decayed_wall_sign");
    public static final Identifier DECAYED_BUTTON = id("decayed_button");
    public static final Identifier DECAYED_HANGING_SIGN = id("decayed_hanging_sign");
    public static final Identifier DECAYED_WALL_HANGING_SIGN = id("decayed_wall_hanging_sign");
    public static final Identifier DECAYED_DOOR = id("decayed_door");
    public static final Identifier DECAYED_TRAPDOOR = id("decayed_trapdoor");
    public static final Identifier BLOOD_DRAIN_EFFECTS = id("blood_drain_effects");
    public static final Identifier NO_MIST_COLLISION_ID = id("mist_phaseable");
    public static final Identifier MIST_ID = id("mist");
    public static final Identifier SUN_RESISTANCE_ID = id("sun_resistance");
    public static final Identifier VAMPIRE_MERCHANT_OFFERS_ID = id("vampire_merchant_enchantments");
    public static final Identifier VULNERABILITY_ID = id("vulnerability");
    public static final Identifier CONVERSION_TRANSFORMERS = id("conversion_transformers");
    public static final Identifier CONVERSION_CONDITIONS = id("conversion_conditions");
    public static final Identifier COPY_ID = id("copy");
    public static final Identifier CONVERSION_DATA = id("conversion_data");
    public static final Identifier CONVERSION_TYPES = id("conversion_types");
    public static final Identifier SPAWN_TYPE = id("spawn");
    public static final Identifier SET_VAMPIRE_TYPE = id("set_vampire");
    public static final Identifier REVERT_VAMPIRE_TYPE = id("revert_vampire");

    public static Identifier id(String path) {
        return new Identifier(Bloodlust.MODID, path);
    }
}
