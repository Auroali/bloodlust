package com.auroali.sanguinisluxuria.common.conversions;

import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class VampireSettingConversionType implements ConversionType {
    private final boolean setToVampire;

    public VampireSettingConversionType(boolean toVampire) {
        this.setToVampire = toVampire;
    }

    @Override
    public Entity apply(World world, Entity original, EntityType<?> targetType, NbtCompound tag) {
        if (!BLEntityComponents.VAMPIRE_COMPONENT.isProvidedBy(original))
            return original;

        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(original);
        boolean isVampire = vampire.isVampire();
        if (isVampire == this.setToVampire)
            return original;

        vampire.setIsVampire(this.setToVampire);
        return original;
    }
}
