package com.auroali.sanguinisluxuria.config;

import com.auroali.configserializer.ConfigSerializer;
import com.auroali.sanguinisluxuria.Bloodlust;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BLConfig {
    public static BLConfig INSTANCE = new BLConfig();

    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("sanguinisluxuria.json");
    private static final Gson GSON = new Gson()
      .newBuilder()
      .setPrettyPrinting()
      .create();

    public float vampireDamageMultiplier = 1.5f;
    public float vampireExhaustionMultiplier = 0.15f;
    public float blessedWaterDamage = 5f;
    public int skillPointsPerLevel = 1;
    public float piercingExhaustion = 2.5f;
    public boolean generateSilverOre = true;

    public Screen generateScreen(Screen screen) {
        return YetAnotherConfigLib.createBuilder()
          .title(Text.translatable("sanguinisluxuria.config.title"))
          .category(ConfigCategory.createBuilder()
              .name(Text.translatable("sanguinisluxuria.config.category.gameplay"))
              .group(OptionGroup.createBuilder()
                  .option(Option.<Float>createBuilder()
                      .name(Text.translatable("sanguinisluxuria.config.option.vampire_damage_multiplier"))
                      .description(OptionDescription.of(Text.translatable("sanguinisluxuria.config.option.vampire_damage_multiplier.desc")))
                      .binding(1.5f, () -> this.vampireDamageMultiplier, f -> this.vampireDamageMultiplier = f)
                      .controller(FloatFieldControllerBuilder::create)
                      .build()
                         ).option(Option.<Float>createBuilder()
                      .name(Text.translatable("sanguinisluxuria.config.option.vampire_exhaustion_multiplier"))
                      .description(OptionDescription.of(Text.translatable("sanguinisluxuria.config.option.vampire_exhaustion_multiplier.desc")))
                      .binding(0.15f, () -> this.vampireExhaustionMultiplier, f -> this.vampireExhaustionMultiplier = f)
                      .controller(FloatFieldControllerBuilder::create)
                      .build()
                                 ).option(Option.<Float>createBuilder()
                      .name(Text.translatable("sanguinisluxuria.config.option.blessed_water_damage"))
                      .description(OptionDescription.of(Text.translatable("sanguinisluxuria.config.option.blessed_water_damage.desc")))
                      .binding(5f, () -> this.blessedWaterDamage, f -> this.blessedWaterDamage = f)
                      .controller(FloatFieldControllerBuilder::create)
                      .build()
                                         ).build()
                    ).build()
                   )
          .category(ConfigCategory.createBuilder()
              .name(Text.translatable("sanguinisluxuria.config.category.abilities"))
              .group(OptionGroup.createBuilder()
                  .option(Option.<Integer>createBuilder()
                      .name(Text.translatable("sanguinisluxuria.config.option.skill_points_per_level"))
                      .description(OptionDescription.of(Text.translatable("sanguinisluxuria.config.option.skill_points_per_level.desc")))
                      .binding(1, () -> this.skillPointsPerLevel, f -> this.skillPointsPerLevel = f)
                      .controller(IntegerFieldControllerBuilder::create)
                      .build()
                         )
                  .option(Option.<Float>createBuilder()
                      .name(Text.translatable("sanguinisluxuria.config.option.blink_piercing_exhaustion"))
                      .description(OptionDescription.of(Text.translatable("sanguinisluxuria.config.option.blink_piercing_exhaustion.desc")))
                      .binding(2f, () -> this.piercingExhaustion, f -> this.piercingExhaustion = f)
                      .controller(FloatFieldControllerBuilder::create)
                      .build()
                         )
                  .build()
                    ).build()
                   )
          .category(ConfigCategory.createBuilder()
              .name(Text.translatable("sanguinisluxuria.config.category.worldgen"))
              .group(OptionGroup.createBuilder()
                  .option(Option.<Boolean>createBuilder()
                      .name(Text.translatable("sanguinisluxuria.config.option.generate_silver_ore"))
                      .description(OptionDescription.of(Text.translatable("sanguinisluxuria.config.option.generate_silver_ore.desc")))
                      .binding(true, () -> this.generateSilverOre, v -> this.generateSilverOre = v)
                      .controller(BooleanControllerBuilder::create)
                      .build()
                         )
                  .build()
                    )
              .build()
                   )
          .save(INSTANCE::save)
          .build()
          .generateScreen(screen);
    }

    public void save() {
        JsonObject root = new JsonObject();
        ConfigSerializer.create(root)
          .category("gameplay")
          .writeValue("vampireDamageMultiplier", vampireDamageMultiplier, JsonObject::addProperty)
          .writeValue("vampireExhaustionMultiplier", vampireExhaustionMultiplier, JsonObject::addProperty)
          .writeValue("blessedWaterDamage", blessedWaterDamage, JsonObject::addProperty)
          .up()
          .category("abilities")
          .writeValue("skillPointsPerLevel", skillPointsPerLevel, JsonObject::addProperty)
          .writeValue("blinkPiercingExhaustion", piercingExhaustion, JsonObject::addProperty)
          .up()
          .category("worldgen")
          .writeValue("generateSilverOre", generateSilverOre, JsonObject::addProperty)
          .up();


        try {
            Files.writeString(CONFIG_FILE, GSON.toJson(root));
        } catch (IOException e) {
            Bloodlust.LOGGER.warn("An error occurred whilst saving the config file!", e);
        }
    }

    public void load() {
        if (!Files.exists(CONFIG_FILE)) {
            save();
            return;
        }
        JsonObject root;
        try {
            root = GSON.fromJson(Files.readString(CONFIG_FILE), JsonObject.class);
        } catch (IOException | JsonSyntaxException e) {
            Bloodlust.LOGGER.warn("Failed to load config file!", e);
            return;
        }

        ConfigSerializer.create(root)
          .category("gameplay")
          .readValue("vampireDamageMultiplier", v -> vampireDamageMultiplier = v, vampireDamageMultiplier, JsonElement::getAsFloat)
          .readValue("vampireExhaustionMultiplier", v -> vampireExhaustionMultiplier = v, vampireExhaustionMultiplier, JsonElement::getAsFloat)
          .readValue("blessedWaterDamage", v -> blessedWaterDamage = v, blessedWaterDamage, JsonElement::getAsFloat)
          .up()
          .category("abilities")
          .readValue("skillPointsPerLevel", v -> skillPointsPerLevel = v, skillPointsPerLevel, JsonElement::getAsInt)
          .readValue("blinkPiercingExhaustion", v -> piercingExhaustion = v, piercingExhaustion, JsonElement::getAsFloat)
          .up()
          .category("worldgen")
          .readValue("generateSilverOre", v -> generateSilverOre = v, generateSilverOre, JsonElement::getAsBoolean)
          .up()
          .saveIfNeeded(this::save);
    }

}
