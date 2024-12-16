package com.auroali.sanguinisluxuria.common.commands;

import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.commands.arguments.VampireAbilityArgument;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class AbilityCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("ability")
          .requires(ctx -> ctx.hasPermissionLevel(2))
          .then(CommandManager.argument("ability", VampireAbilityArgument.argument())
            .executes(ctx -> {
                if (!ctx.getSource().isExecutedByPlayer())
                    return 1;
                VampireAbility ability = VampireAbilityArgument.getAbility(ctx, "ability");
                VampireComponent component = BLEntityComponents.VAMPIRE_COMPONENT.get(ctx.getSource().getPlayer());
                component.getAbilties().addAbility(ability);
                BLEntityComponents.VAMPIRE_COMPONENT.sync(ctx.getSource().getPlayer());
                return 0;
            })
          ).then(CommandManager.literal("reset").executes(ctx -> {
              if (!ctx.getSource().isExecutedByPlayer())
                  return 1;
              VampireComponent component = BLEntityComponents.VAMPIRE_COMPONENT.get(ctx.getSource().getPlayer());
              for (VampireAbility a : component.getAbilties()) {
                  a.onAbilityRemoved(ctx.getSource().getPlayer(), component);
                  component.getAbilties().removeAbility(a);
              }
              BLEntityComponents.VAMPIRE_COMPONENT.sync(ctx.getSource().getPlayer());
              return 0;
          }));
    }
}
