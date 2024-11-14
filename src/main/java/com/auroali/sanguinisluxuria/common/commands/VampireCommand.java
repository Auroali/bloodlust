package com.auroali.sanguinisluxuria.common.commands;

import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class VampireCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("vampire")
          .requires(ctx -> ctx.hasPermissionLevel(2))
          .then(CommandManager.argument("isVampire", BoolArgumentType.bool())
              .executes(ctx -> {
                  if (!ctx.getSource().isExecutedByPlayer())
                      return 1;
                  boolean isVampire = BoolArgumentType.getBool(ctx, "isVampire");
                  VampireComponent component = BLEntityComponents.VAMPIRE_COMPONENT.get(ctx.getSource().getPlayer());
                  component.setIsVampire(isVampire);
                  return 0;
              })
               );
    }
}
