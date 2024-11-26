package com.auroali.sanguinisluxuria.common.commands.arguments;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VampireAbilityArgument implements ArgumentType<VampireAbility> {
    public static final Collection<String> EXAMPLES = List.of(BLResources.VAMPIRE_HEALTH_1_ID.toString());
    private static final DynamicCommandExceptionType ID_INVALID_EXCEPTION = new DynamicCommandExceptionType(
      id -> Text.translatable("argument.sanguinisluxuria.id.invalid", id)
    );

    @Override
    public VampireAbility parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Identifier id = Identifier.fromCommandInput(reader);
        VampireAbility ability = BLRegistries.VAMPIRE_ABILITIES.get(id);
        if (ability == null) {
            reader.setCursor(cursor);
            throw ID_INVALID_EXCEPTION.createWithContext(reader, id);
        }
        return ability;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        try {
            this.parse(stringReader);
            return Suggestions.empty();
        } catch (Exception ignored) {
        }
        return CommandSource.suggestIdentifiers(BLRegistries.VAMPIRE_ABILITIES.getIds(), builder.createOffset(stringReader.getCursor()));
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static VampireAbilityArgument argument() {
        return new VampireAbilityArgument();
    }

    public static VampireAbility getAbility(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, VampireAbility.class);
    }
}
