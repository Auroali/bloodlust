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
    public static final BLConfig INSTANCE = new BLConfig();

    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("sanguinisluxuria.json");
    private static final Gson GSON = new Gson()
      .newBuilder()
      .setPrettyPrinting()
      .create();

    public float vampireDamageMultiplier = 1.5f;
    public float vampireExhaustionMultiplier = 0.45f;
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
                .binding(0.45f, () -> this.vampireExhaustionMultiplier, f -> this.vampireExhaustionMultiplier = f)
                .controller(FloatFieldControllerBuilder::create)
                .build()
              ).build()
            ).build()
          )
          .category(ConfigCategory.createBuilder()
            .name(Text.translatable("sanguinisluxuria.config.category.abilities"))
            .group(OptionGroup.createBuilder()
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
          .writeValue("vampireDamageMultiplier", this.vampireDamageMultiplier, JsonObject::addProperty)
          .writeValue("vampireExhaustionMultiplier", this.vampireExhaustionMultiplier, JsonObject::addProperty)
          .up()
          .category("abilities")
          .writeValue("blinkPiercingExhaustion", this.piercingExhaustion, JsonObject::addProperty)
          .up()
          .category("worldgen")
          .writeValue("generateSilverOre", this.generateSilverOre, JsonObject::addProperty)
          .up();


        try {
            Files.writeString(CONFIG_FILE, GSON.toJson(root));
        } catch (IOException e) {
            Bloodlust.LOGGER.warn("An error occurred whilst saving the config file!", e);
        }
    }

    public void load() {
        if (!Files.exists(CONFIG_FILE)) {
            this.save();
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
          .readValue("vampireDamageMultiplier", v -> this.vampireDamageMultiplier = v, this.vampireDamageMultiplier, JsonElement::getAsFloat)
          .readValue("vampireExhaustionMultiplier", v -> this.vampireExhaustionMultiplier = v, this.vampireExhaustionMultiplier, JsonElement::getAsFloat)
          .up()
          .category("abilities")
          .readValue("blinkPiercingExhaustion", v -> this.piercingExhaustion = v, this.piercingExhaustion, JsonElement::getAsFloat)
          .up()
          .category("worldgen")
          .readValue("generateSilverOre", v -> this.generateSilverOre = v, this.generateSilverOre, JsonElement::getAsBoolean)
          .up()
          .saveIfNeeded(this::save);
    }

}
