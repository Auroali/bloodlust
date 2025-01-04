package com.auroali.sanguinisluxuria.common.components;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.components.impl.EntityBloodComponent;
import com.auroali.sanguinisluxuria.common.components.impl.EntityVampireComponent;
import com.auroali.sanguinisluxuria.common.components.impl.PlayerBloodComponent;
import com.auroali.sanguinisluxuria.common.components.impl.PlayerVampireComponent;
import com.auroali.sanguinisluxuria.common.entities.VampireMerchant;
import com.auroali.sanguinisluxuria.common.entities.VampireVillagerEntity;
import com.auroali.sanguinisluxuria.common.registry.BLVampireAbilities;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.TridentEntity;

public class BLEntityComponents implements EntityComponentInitializer {
    public static final ComponentKey<BloodComponent> BLOOD_COMPONENT = ComponentRegistry.getOrCreate(BLResources.BLOOD_COMPONENT_ID, BloodComponent.class);
    public static final ComponentKey<VampireComponent> VAMPIRE_COMPONENT = ComponentRegistry.getOrCreate(BLResources.VAMPIRE_COMPONENT_ID, VampireComponent.class);

    public static final ComponentKey<BloodTransferComponent> BLOOD_TRANSFER_COMPONENT = ComponentRegistry.getOrCreate(BLResources.BLOOD_DRAIN_ID, BloodTransferComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // we don't need to copy the player component, as it stores no actual data
        registry.registerForPlayers(BLOOD_COMPONENT, PlayerBloodComponent::new, RespawnCopyStrategy.NEVER_COPY);
        registry.registerForPlayers(VAMPIRE_COMPONENT, PlayerVampireComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.beginRegistration(LivingEntity.class, BLOOD_COMPONENT)
          .impl(EntityBloodComponent.class)
          .end(EntityBloodComponent::new);
        registry.registerFor(TridentEntity.class, BLOOD_TRANSFER_COMPONENT, BloodTransferComponent::new);
        registry.registerFor(VampireVillagerEntity.class, VAMPIRE_COMPONENT, e -> new EntityVampireComponent<>(e, BLVampireAbilities.TELEPORT));
        registry.registerFor(VampireMerchant.class, VAMPIRE_COMPONENT, e -> new EntityVampireComponent<>(e, BLVampireAbilities.MIST));
    }
}
