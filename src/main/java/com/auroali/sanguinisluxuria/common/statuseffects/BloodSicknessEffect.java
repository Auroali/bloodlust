package com.auroali.sanguinisluxuria.common.statuseffects;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.registry.BLAdvancementCriterion;
import com.auroali.sanguinisluxuria.common.registry.BLStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

public class BloodSicknessEffect extends StatusEffect {
    public BloodSicknessEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        super.applyUpdateEffect(entity, amplifier);
        int duration = entity.getStatusEffect(this).getDuration();

        if(amplifier > 2 && entity.getRandom().nextInt(15) == 0)
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100));

        if(amplifier >= 4 && duration < 1000 && entity.getRandom().nextInt(25) == 0)
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200));

        if(duration == 1 && isRightConditions(entity, amplifier) && VampireHelper.canBeConvertedToVampire(entity)) {
            if(entity instanceof ServerPlayerEntity p)
                BLAdvancementCriterion.BECOME_VAMPIRE.trigger(p);
            BLEntityComponents.VAMPIRE_COMPONENT.get(entity).setIsVampire(true);
        }
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
    }

    public boolean isRightConditions(LivingEntity entity, int amplifier) {
        return !entity.hasStatusEffect(BLStatusEffects.BLOOD_PROTECTION) && !VampireHelper.isVampire(entity) && amplifier >= 4;
    }
}
