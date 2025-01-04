package com.auroali.sanguinisluxuria.common.statuseffects;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.conversions.ConversionContext;
import com.auroali.sanguinisluxuria.common.registry.BLAdvancementCriterion;
import com.auroali.sanguinisluxuria.common.registry.BLConversions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

public class BloodProtectionEffect extends StatusEffect {
    public BloodProtectionEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        super.applyUpdateEffect(entity, amplifier);
        if (entity.getWorld().isClient || !VampireHelper.isVampire(entity))
            return;

        int duration = entity.getStatusEffect(this).getDuration();

        if (entity.getRandom().nextInt(200) == 0)
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100));

        if (duration == 1 && BLConversions.convertEntity(ConversionContext.from(entity, ConversionContext.Conversion.DECONVERTING))) {
            if (entity instanceof ServerPlayerEntity p)
                BLAdvancementCriterion.UNBECOME_VAMPIRE.trigger(p);
            entity.clearStatusEffects();
        }
    }
}
