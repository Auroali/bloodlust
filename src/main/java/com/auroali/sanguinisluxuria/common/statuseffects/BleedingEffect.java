package com.auroali.sanguinisluxuria.common.statuseffects;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.registry.BLSounds;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.sound.SoundCategory;

public class BleedingEffect extends StatusEffect {
    public BleedingEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!VampireHelper.hasBlood(entity))
            return;

        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(entity);
        if (blood.drainBlood())
            entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), BLSounds.BLEEDING, SoundCategory.NEUTRAL, 1.0f, 1.0f);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 15 == 0;
    }
}
