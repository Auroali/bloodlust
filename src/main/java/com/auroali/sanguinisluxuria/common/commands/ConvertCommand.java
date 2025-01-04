package com.auroali.sanguinisluxuria.common.commands;

import com.auroali.sanguinisluxuria.common.commands.arguments.ConversionArgument;
import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;
import java.util.List;

public class ConvertCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("convert")
          .requires(ctx -> ctx.hasPermissionLevel(2))
          .then(CommandManager.argument("targets", EntityArgumentType.entities())
            .then(CommandManager.argument("conversion", ConversionArgument.conversion())
              .executes(ctx -> convert(
                EntityArgumentType.getEntities(ctx, "targets"),
                ConversionArgument.getConversion(ctx, "conversion")
              ))
            )
          );
    }

    public static int convert(Collection<? extends Entity> entities, ConversionContext.Conversion conversion) {
        for (Entity entity : entities) {
            BLConversions.convertEntity(ConversionContext.from(entity, conversion));
        }
        return 0;
    }
}
