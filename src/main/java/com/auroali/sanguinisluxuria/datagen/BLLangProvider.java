package com.auroali.sanguinisluxuria.datagen;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.registry.*;
import com.auroali.sanguinisluxuria.common.rituals.RitualType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Util;

import java.util.function.BiConsumer;

public class BLLangProvider extends FabricLanguageProvider {
    public BLLangProvider(FabricDataOutput dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        translationBuilder.add(BLItemGroups.SANGUINIS_LUXURIA_TAB, "Sanguinis Luxuria");
        translationBuilder.add("fluids.sanguinisluxuria.blood", "Blood");
        translationBuilder.add("argument.sanguinisluxuria.id.invalid", "'%s' is not a valid id!");
        tags(translationBuilder);
        subtitles(translationBuilder);
        keybindings(translationBuilder);
        config(translationBuilder);
        statusEffects(translationBuilder);
        gui(translationBuilder);
        entities(translationBuilder);
        enchantments(translationBuilder);
        items(translationBuilder);
        blocks(translationBuilder);
        attributes(translationBuilder);
        potions(translationBuilder);
        abilities(translationBuilder);
        deathMessages(translationBuilder);
        advancements(translationBuilder);
        emiTranslations(translationBuilder);
        rituals(translationBuilder);
    }

    private static void generateTagTranslation(TranslationBuilder builder, TagKey<?> key, String translation) {
        String transKey = "tag.%s.%s.%s".formatted(key.registry().getValue().getPath(), key.id().getNamespace(), key.id().getPath().replace("/", "."));
        builder.add(transKey, translation);
    }

    private static void emiTranslations(TranslationBuilder builder) {
        builder.add("emi.category.sanguinisluxuria.altar", "Altar");
        builder.add("emi.category.sanguinisluxuria.blood_cauldron", "Cauldron Infusing");
    }

    private static void tags(TranslationBuilder builder) {
        generateTagTranslation(builder, BLTags.Items.VAMPIRE_MASKS, "Vampire Masks");
        generateTagTranslation(builder, BLTags.Items.SUN_BLOCKING_HELMETS, "Sun Blocking Helmets");
        generateTagTranslation(builder, BLTags.Items.VAMPIRES_GET_HUNGER_FROM, "Vampire Food");
    }

    private static void deathMessages(TranslationBuilder translationBuilder) {
        BiConsumer<RegistryKey<DamageType>, String> death = (key, name) -> translationBuilder.add("death.attack.%s.%s".formatted(key.getValue().getNamespace(), key.getValue().getPath()), name);
        BiConsumer<RegistryKey<DamageType>, String> deathItem = (key, name) -> translationBuilder.add("death.attack.%s.%s.item".formatted(key.getValue().getNamespace(), key.getValue().getPath()), name);
        BiConsumer<RegistryKey<DamageType>, String> deathPlayer = (key, name) -> translationBuilder.add("death.attack.%s.%s.player".formatted(key.getValue().getNamespace(), key.getValue().getPath()), name);
        death.accept(BLResources.BLESSED_WATER_DAMAGE_KEY, "%s was burned by blessed water");
        deathPlayer.accept(BLResources.BLESSED_WATER_DAMAGE_KEY, "%s was burned by blessed water whilst trying to escape %s");
        death.accept(BLResources.BITE_DAMAGE_KEY, "%s was bitten by %s");
        deathItem.accept(BLResources.BITE_DAMAGE_KEY, "%s was bitten by %s using %s");
        death.accept(BLResources.BLOOD_DRAIN_DAMAGE_KEY, "%s had their blood drained");
        deathPlayer.accept(BLResources.BLOOD_DRAIN_DAMAGE_KEY, "%s had their blood drained whilst trying to escape %s");
        death.accept(BLResources.TELEPORT_DAMAGE_KEY, "%s was pierced by %s");
        deathItem.accept(BLResources.TELEPORT_DAMAGE_KEY, "%s was pierced by %s using %s");
    }

    private static void abilities(TranslationBuilder translationBuilder) {
        generateAbilityKey(translationBuilder, BLVampireAbilities.TELEPORT, "Blink");
        generateAbilityKey(translationBuilder, BLVampireAbilities.INFECTIOUS, "Infectious");
        generateAbilityKey(translationBuilder, BLVampireAbilities.BITE, "Bite");
        generateAbilityKey(translationBuilder, BLVampireAbilities.MIST, "Mist");
        generateAbilityKey(translationBuilder, BLVampireAbilities.VULNERABILITY, "Vulnerability");
        generateAbilityKey(translationBuilder, BLVampireAbilities.SUN_RESIST, "Resilience");
    }

    public static void rituals(TranslationBuilder builder) {
        generateRitualKey(builder, BLRitualTypes.ABILITY_RITUAL_TYPE, "Ritual of Transformation");
        generateRitualKey(builder, BLRitualTypes.ABILITY_RESET_RITUAL_TYPE, "Ritual of Cleansing");
        generateRitualKey(builder, BLRitualTypes.ITEM_RITUAL_TYPE, "Ritual of Transmutation");
        generateRitualKey(builder, BLRitualTypes.ABILITY_REVEAL_RITUAL_TYPE, "Ritual of Revealing");
    }

    private static void generateRitualKey(TranslationBuilder builder, RitualType<?> type, String entry) {
        builder.add(Util.createTranslationKey("altar_ritual", BLRegistries.RITUAL_TYPES.getId(type)), entry);
    }

    private static void potions(TranslationBuilder translationBuilder) {
        generatePotionKey(translationBuilder, BLStatusEffects.BLESSED_WATER_POTION, Items.POTION, "Potion of Blessed Water");
        generatePotionKey(translationBuilder, BLStatusEffects.BLESSED_WATER_POTION, Items.SPLASH_POTION, "Splash Potion of Blessed Water");
        generatePotionKey(translationBuilder, BLStatusEffects.BLESSED_WATER_POTION, Items.LINGERING_POTION, "Lingering Potion of Blessed Water");
        generatePotionKey(translationBuilder, BLStatusEffects.BLESSED_WATER_POTION, Items.TIPPED_ARROW, "Arrow of Blessed Water");

        generatePotionKey(translationBuilder, BLStatusEffects.BLESSED_WATER_POTION_TWO, Items.POTION, "Potion of Blessed Water");
        generatePotionKey(translationBuilder, BLStatusEffects.BLESSED_WATER_POTION_TWO, Items.SPLASH_POTION, "Splash Potion of Blessed Water");
        generatePotionKey(translationBuilder, BLStatusEffects.BLESSED_WATER_POTION_TWO, Items.LINGERING_POTION, "Lingering Potion of Blessed Water");
        generatePotionKey(translationBuilder, BLStatusEffects.BLESSED_WATER_POTION_TWO, Items.TIPPED_ARROW, "Arrow of Blessed Water");
    }

    private static void enchantments(TranslationBuilder translationBuilder) {
        translationBuilder.add(BLEnchantments.SUN_PROTECTION, "Sun Protection");
        enchantmentDescription(translationBuilder, BLEnchantments.SUN_PROTECTION, "Increases the amount of time a vampire can stay in the sun");
        translationBuilder.add(BLEnchantments.BLOOD_DRAIN, "Blood Transfer");
        enchantmentDescription(translationBuilder, BLEnchantments.BLOOD_DRAIN, "Allows a trident to latch on and slowly transfer blood from a target to the thrower");
        translationBuilder.add(BLEnchantments.SERRATED, "Serrated");
        enchantmentDescription(translationBuilder, BLEnchantments.SERRATED, "Has a chance of inflicting bleeding on targets");
    }

    private static void enchantmentDescription(TranslationBuilder builder, Enchantment enchantment, String description) {
        builder.add(enchantment.getTranslationKey() + ".desc", description);
    }

    private static void attributes(TranslationBuilder translationBuilder) {
        translationBuilder.add(BLEntityAttributes.BLESSED_DAMAGE.getTranslationKey(), "Blessed Damage");
        translationBuilder.add(BLEntityAttributes.BLINK_COOLDOWN.getTranslationKey(), "Blink Cooldown");
        translationBuilder.add(BLEntityAttributes.BLINK_RANGE.getTranslationKey(), "Blink Range");
        translationBuilder.add(BLEntityAttributes.SUN_RESISTANCE.getTranslationKey(), "Sun Resistance");
        translationBuilder.add(BLEntityAttributes.VULNERABILITY.getTranslationKey(), "Vulnerability");
    }

    private static void entities(TranslationBuilder translationBuilder) {
        translationBuilder.add(BLEntities.VAMPIRE_VILLAGER, "Vampiric Villager");
        translationBuilder.add(BLEntities.VAMPIRE_MERCHANT, "Vampiric Merchant");
    }

    private static void keybindings(TranslationBuilder translationBuilder) {
        translationBuilder.add("key.sanguinisluxuria.drain_blood", "Drain Blood");
        translationBuilder.add("key.sanguinisluxuria.activate_bite", "Bite");
        translationBuilder.add("key.sanguinisluxuria.activate_blink", "Blink");
        translationBuilder.add("category.sanguinisluxuria.sanguinisluxuria", "Sanguinis Luxuria");
    }

    private static void subtitles(TranslationBuilder translationBuilder) {
        translationBuilder.add("subtitles.sanguinisluxuria.drain_blood", "Vampire feeding");
        translationBuilder.add("subtitles.sanguinisluxuria.altar_beats", "Altar beats");
        translationBuilder.add("subtitles.sanguinisluxuria.bleeding", "Something bleeds");
        translationBuilder.add("subtitles.sanguinisluxuria.entity_converted_to_vampire", "Something transforms");
    }

    private static void gui(TranslationBuilder translationBuilder) {
        translationBuilder.add("gui.sanguinisluxuria.abilities", "Abilities");
        translationBuilder.add("gui.sanguinisluxuria.skill_points", "Skill Points: %d");

        translationBuilder.add("gui.sanguinisluxuria.abilities.bound", "Bound to [%s]");
        translationBuilder.add("gui.sanguinisluxuria.abilities.bind_prompt", "Right click to bind...");
        translationBuilder.add("gui.sanguinisluxuria.abilities.binding", "Press a key...");

        translationBuilder.add("gui.sanguinisluxuria.abilities.incompatibilites", "Incompatible With:");
        translationBuilder.add("gui.sanguinisluxuria.abilities.incompatibilites_entry", "  - %s");

        translationBuilder.add("gui.sanguinisluxuria.abilities.required_skill_points", "Requires %d skill point(s)");

        translationBuilder.add("gui.sanguinisluxuria.blood_bottle_tooltip", "%d Blood Bottle(s)");
    }

    private static void config(TranslationBuilder translationBuilder) {
        translationBuilder.add("sanguinisluxuria.config.title", "Sanguinis Luxuria");
        translationBuilder.add("sanguinisluxuria.config.category.gameplay", "Gameplay");
        translationBuilder.add("sanguinisluxuria.config.option.vampire_damage_multiplier", "Damage Multiplier");
        translationBuilder.add("sanguinisluxuria.config.option.vampire_damage_multiplier.desc", "How much damage is multiplied for vampires from damage types they are weak to, such as fire.");
        translationBuilder.add("sanguinisluxuria.config.option.vampire_exhaustion_multiplier", "Exhaustion Multiplier");
        translationBuilder.add("sanguinisluxuria.config.option.vampire_exhaustion_multiplier.desc", "How much exhaustion is multiplied by for vampires");
        translationBuilder.add("sanguinisluxuria.config.option.blessed_water_damage", "Blessed Water Damage");
        translationBuilder.add("sanguinisluxuria.config.option.blessed_water_damage.desc", "The base amount of damage blessed water does against vampires and undead entities");
        translationBuilder.add("sanguinisluxuria.config.category.abilities", "Abilities");
        translationBuilder.add("sanguinisluxuria.config.option.skill_points_per_level", "Skill Points Per Level");
        translationBuilder.add("sanguinisluxuria.config.option.skill_points_per_level.desc", "How many skill points are gained per level");
        translationBuilder.add("sanguinisluxuria.config.category.worldgen", "Worldgen");
        translationBuilder.add("sanguinisluxuria.config.option.generate_silver_ore", "Generate Silver Ore");
        translationBuilder.add("sanguinisluxuria.config.option.generate_silver_ore.desc", "If silver ore should generate naturally in the world");
        translationBuilder.add("sanguinisluxuria.config.option.blink_piercing_exhaustion", "Blink Piercing Exhaustion");
        translationBuilder.add("sanguinisluxuria.config.option.blink_piercing_exhaustion.desc", "The amount of exhaustion per entity pierced when using the Pendant of Piercing");
    }

    private static void blocks(TranslationBuilder translationBuilder) {
        translationBuilder.add(BLBlocks.DEEPSLATE_SILVER_ORE, "Deepslate Silver Ore");
        translationBuilder.add(BLBlocks.RAW_SILVER_BLOCK, "Block of Raw Silver");
        translationBuilder.add(BLBlocks.SILVER_BLOCK, "Silver Block");
        translationBuilder.add(BLBlocks.SILVER_ORE, "Silver Ore");
        translationBuilder.add(BLBlocks.PEDESTAL, "Pedestal");
        translationBuilder.add(BLBlocks.ALTAR, "Altar");
        translationBuilder.add(BLBlocks.BLOOD_CAULDRON, "Blood Cauldron");
        translationBuilder.add(BLBlocks.BLOOD_SPLATTER, "Blood");
        translationBuilder.add(BLBlocks.HUNGRY_DECAYED_LOG, "Hungry Decayed Log");
        translationBuilder.add(BLBlocks.STRIPPED_HUNGRY_DECAYED_LOG, "Stripped Hungry Decayed Log");
        translationBuilder.add(BLBlocks.STRIPPED_DECAYED_LOG, "Stripped Decayed Log");
        translationBuilder.add(BLBlocks.DECAYED_LOG, "Decayed Log");
        translationBuilder.add(BLBlocks.DECAYED_WOOD, "Decayed Wood");
        translationBuilder.add(BLBlocks.STRIPPED_DECAYED_WOOD, "Stripped Decayed Wood");
        translationBuilder.add(BLBlocks.DECAYED_TWIGS, "Decayed Twigs");
        translationBuilder.add(BLBlocks.GRAFTED_SAPLING, "Grafted Sapling");
        translationBuilder.add(BLBlocks.DECAYED_PRESSURE_PLATE, "Decayed Pressure Plate");
        translationBuilder.add(BLBlocks.SILVER_PRESSURE_PLATE, "Silver Pressure Plate");
        translationBuilder.add(BLBlocks.DECAYED_PLANKS, "Decayed Planks");
        translationBuilder.add(BLBlocks.DECAYED_SLAB, "Decayed Slab");
        translationBuilder.add(BLBlocks.DECAYED_FENCE, "Decayed Fence");
        translationBuilder.add(BLBlocks.DECAYED_FENCE_GATE, "Decayed Fence Gate");
        translationBuilder.add(BLBlocks.DECAYED_TRAPDOOR, "Decayed Trapdoor");
        translationBuilder.add(BLBlocks.DECAYED_DOOR, "Decayed Door");
        translationBuilder.add(BLBlocks.DECAYED_BUTTON, "Decayed Button");
        translationBuilder.add(BLBlocks.DECAYED_SIGN, "Decayed Sign");
        translationBuilder.add(BLBlocks.DECAYED_HANGING_SIGN, "Decayed Hanging Sign");
        translationBuilder.add(BLBlocks.DECAYED_STAIRS, "Decayed Stairs");
    }

    private static void items(TranslationBuilder translationBuilder) {
        translationBuilder.add(BLItems.BLOOD_PETAL, "Blood Petal");
        translationBuilder.add(BLItems.PENDANT_OF_PIERCING, "Pendant of Piercing");
        translationBuilder.add(BLItems.TWISTED_BLOOD, "Twisted Blood Bottle");
        translationBuilder.add(BLItems.SILVER_INGOT, "Silver Ingot");
        translationBuilder.add(BLItems.RAW_SILVER, "Raw Silver");
        translationBuilder.add(BLItems.SILVER_SWORD, "Silver Sword");
        translationBuilder.add(BLItems.SILVER_AXE, "Silver Axe");
        translationBuilder.add(BLItems.SILVER_PICKAXE, "Silver Pickaxe");
        translationBuilder.add(BLItems.SILVER_SHOVEL, "Silver Shovel");
        translationBuilder.add(BLItems.SILVER_HOE, "Silver Hoe");
        translationBuilder.add(BLItems.VAMPIRE_VILLAGER_SPAWN_EGG, "Vampire Villager Spawn Egg");
        translationBuilder.add(BLItems.MASK_1, "Carved Mask");
        translationBuilder.add(BLItems.MASK_2, "Carved Mask");
        translationBuilder.add(BLItems.MASK_3, "Carved Mask");
        translationBuilder.add(BLItems.BLOOD_BAG, "Blood Bag");
        translationBuilder.add(BLItems.BLOOD_BOTTLE, "Blood Bottle");
        translationBuilder.add("item.sanguinisluxuria.book", "Sanguinis Luxuria");
        translationBuilder.add("sanguinisluxuria.landing", "A book of vampires.");
    }

    private static void statusEffects(TranslationBuilder translationBuilder) {
        translationBuilder.add(BLStatusEffects.BLOOD_SICKNESS, "Blood Sickness");
        translationBuilder.add(BLStatusEffects.BLESSED_WATER, "Blessed Water");
        translationBuilder.add(BLStatusEffects.BLOOD_PROTECTION, "Blessed Blood");
        translationBuilder.add(BLStatusEffects.BLEEDING, "Bleeding");
    }

    public static void advancements(TranslationBuilder translationBuilder) {
        translationBuilder.add(BLAdvancementsProvider.title("become_vampire"), "Bloodlust");
        translationBuilder.add(BLAdvancementsProvider.desc("become_vampire"), "Transform into a vampire after drinking enough blood");

        translationBuilder.add(BLAdvancementsProvider.title("drink_twisted_blood"), "Consumption");
        translationBuilder.add(BLAdvancementsProvider.desc("drink_twisted_blood"), "Drink twisted blood");

        translationBuilder.add(BLAdvancementsProvider.title("blood_sickness"), "Feeling Ill");
        translationBuilder.add(BLAdvancementsProvider.desc("blood_sickness"), "Get blood sickness from drinking blood");

        translationBuilder.add(BLAdvancementsProvider.title("unlock_ability"), "Abilities");
        translationBuilder.add(BLAdvancementsProvider.desc("unlock_ability"), "Allocate a skill point to an ability");

        translationBuilder.add(BLAdvancementsProvider.title("reset_abilities"), "Clean Slate");
        translationBuilder.add(BLAdvancementsProvider.desc("reset_abilities"), "Convert unlocked abilities back into skill points using blessed blood");

        translationBuilder.add(BLAdvancementsProvider.title("transfer_effects"), "No Need for Bottles");
        translationBuilder.add(BLAdvancementsProvider.desc("transfer_effects"), "Transfer a potion effect while draining blood");

        translationBuilder.add(BLAdvancementsProvider.title("transfer_more_effects"), "Alchemist");
        translationBuilder.add(BLAdvancementsProvider.desc("transfer_more_effects"), "Transfer 4 potions effects at once");

        translationBuilder.add(BLAdvancementsProvider.title("infect_other"), "Infectious");
        translationBuilder.add(BLAdvancementsProvider.desc("infect_other"), "Inflict blood sickness on something with Weakness");

        translationBuilder.add(BLAdvancementsProvider.title("unbecome_vampire"), "Humanity");
        translationBuilder.add(BLAdvancementsProvider.desc("unbecome_vampire"), "Become human again after drinking Blessed Water with weakness");

        translationBuilder.add(BLAdvancementsProvider.title("craft_hungry_sapling"), "Grafted Petal");
        translationBuilder.add(BLAdvancementsProvider.desc("craft_hungry_sapling"), "Craft a Hungry Sapling");

        translationBuilder.add(BLAdvancementsProvider.title("grow_decayed_tree"), "Decayed");
        translationBuilder.add(BLAdvancementsProvider.desc("grow_decayed_tree"), "Grow a Grafted Sapling");

        translationBuilder.add(BLAdvancementsProvider.title("obtain_hungry_decayed_log"), "Blood Collector");
        translationBuilder.add(BLAdvancementsProvider.desc("obtain_hungry_decayed_log"), "Obtain a Hungry Decayed Log");
    }

    public static void generatePotionKey(TranslationBuilder builder, Potion potion, Item item, String entry) {
        ItemStack stack = new ItemStack(item);
        PotionUtil.setPotion(stack, potion);
        builder.add(stack.getTranslationKey(), entry);
    }

    public static void generateAbilityKey(TranslationBuilder builder, VampireAbility ability, String entry) {
        builder.add(ability.getTranslationKey(), entry);
    }
}
