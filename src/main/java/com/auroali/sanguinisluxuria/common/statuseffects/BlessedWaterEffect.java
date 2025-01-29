package com.auroali.sanguinisluxuria.common.statuseffects;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.registry.BLDamageSources;
import com.auroali.sanguinisluxuria.common.registry.BLStatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.Nullable;

public class BlessedWaterEffect extends StatusEffect {
    public BlessedWaterEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        super.applyInstantEffect(source, attacker, target, amplifier, proximity);
        if (!target.isUndead()) {
            target.addStatusEffect(new StatusEffectInstance(BLStatusEffects.BLOOD_PROTECTION, 3600, amplifier));
            return;
        }

        float damage = 2.f * (amplifier + 1);

        target.setOnFireFor(20 + 2 * amplifier);

        if (source != null)
            target.damage(BLDamageSources.blessedWater(source, attacker), damage);
        else
            target.damage(BLDamageSources.get(target.getWorld(), BLResources.BLESSED_WATER_DAMAGE_KEY), damage);
    }

    @Override
    public boolean isInstant() {
        return true;
    }
}
