package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.auroali.sanguinisluxuria.common.registry.BLRitualTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VampireAbilityRitual implements Ritual {
    public static final Codec<VampireAbilityRitual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      BLRegistries.VAMPIRE_ABILITIES.getCodec().fieldOf("ability").forGetter(VampireAbilityRitual::getAbility)
    ).apply(instance, VampireAbilityRitual::new));

    public final VampireAbility ability;

    public VampireAbilityRitual(VampireAbility ability) {
        this.ability = ability;
    }

    public VampireAbility getAbility() {
        return this.ability;
    }


    @Override
    public void onCompleted(World world, LivingEntity initiator, BlockPos pos, Inventory inventory) {
        if (!VampireHelper.isVampire(initiator))
            return;

        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(initiator);
        VampireAbilityContainer abilities = vampire.getAbilties();
        if (abilities.hasAbility(this.ability))
            return;

        vampire.unlockAbility(this.ability);
    }

    @Override
    public RitualType<?> getType() {
        return BLRitualTypes.ABILITY_RITUAL_TYPE;
    }
}
