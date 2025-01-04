package com.auroali.sanguinisluxuria.common.commands.arguments;

import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;

public class ConversionArgument extends EnumArgumentType<ConversionContext.Conversion> {
    protected ConversionArgument() {
        super(ConversionContext.Conversion.CODEC, ConversionContext.Conversion::values);
    }

    public static ConversionArgument conversion() {
        return new ConversionArgument();
    }

    public static ConversionContext.Conversion getConversion(CommandContext<?> context, String name) {
        return context.getArgument(name, ConversionContext.Conversion.class);
    }
}
