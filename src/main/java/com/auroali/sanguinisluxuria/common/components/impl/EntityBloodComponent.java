package com.auroali.sanguinisluxuria.common.components.impl;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.BloodConstants;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.InitializableBloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLDamageSources;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class EntityBloodComponent implements InitializableBloodComponent, ServerTickingComponent {
    private final LivingEntity holder;
    private int maxBlood;
    private int currentBlood;
    private int bloodGainTimer;

    public EntityBloodComponent(LivingEntity holder) {
        this.holder = holder;
        this.maxBlood = -1;
        this.currentBlood = -1;
    }

    public void initializeBloodValues() {
        if (!this.holder.getType().isIn(BLTags.Entities.HAS_BLOOD)) {
            this.maxBlood = 0;
            this.currentBlood = 0;
            BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
            return;
        }

        // if an entity isn't in the good blood tag, half the max amount of blood
        float maxBloodFromHealth = this.holder.getMaxHealth();
        if (!this.holder.getType().isIn(BLTags.Entities.GOOD_BLOOD))
            maxBloodFromHealth = MathHelper.clamp(maxBloodFromHealth / 2.f, 1.f, Float.MAX_VALUE);

        this.maxBlood = (int) Math.ceil(maxBloodFromHealth);
        if (this.currentBlood == -1)
            this.currentBlood = this.maxBlood;
        this.currentBlood = Math.min(this.currentBlood, this.maxBlood);

        BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.currentBlood = tag.getInt("Blood");
        this.bloodGainTimer = tag.getInt("BloodTimer");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("Blood", this.currentBlood);
        tag.putInt("BloodTimer", this.bloodGainTimer);
    }

    @Override
    public int getBlood() {
        return this.currentBlood;
    }

    @Override
    public int getMaxBlood() {
        return this.maxBlood;
    }

    @Override
    public int addBlood(int amount) {
        // ultrakill??????
        int newBlood = Math.min(this.maxBlood, amount + this.currentBlood);
        int bloodAdded = newBlood - this.currentBlood;
        this.currentBlood = newBlood;
        BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
        if (VampireHelper.isVampire(this.holder) && bloodAdded > 0) {
            VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(this.holder);
            vampire.setDowned(false);
        }
        return bloodAdded;
    }

    @Override
    public void setBlood(int amount) {
        this.currentBlood = amount;
        BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
    }

    @Override
    public boolean drainBlood(LivingEntity drainer) {
        if (!this.hasBlood())
            return false;

        this.bloodGainTimer = 0;
        if (this.currentBlood > 1) {
            this.currentBlood--;
            BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
            return true;
        }

        this.currentBlood = 0;
        BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
        if (drainer == null)
            this.holder.damage(BLDamageSources.get(this.holder.getWorld(), BLResources.BLOOD_DRAIN_DAMAGE_KEY), Float.MAX_VALUE);
        else
            this.holder.damage(BLDamageSources.bloodDrain(drainer), Float.MAX_VALUE);
        return true;
    }

    @Override
    public boolean drainBlood() {
        return this.drainBlood(null);
    }

    @Override
    public boolean hasBlood() {
        return this.getMaxBlood() > 0;
    }

    @Override
    public void serverTick() {
        // have to do this here instead of the constructor, as health values aren't available there
        if (this.maxBlood == -1)
            this.initializeBloodValues();

        if (this.getMaxBlood() == 0 || VampireHelper.isVampire(this.holder))
            return;

        if (this.getBlood() < this.getMaxBlood() && this.bloodGainTimer < BloodConstants.BLOOD_GAIN_RATE)
            this.bloodGainTimer++;

        if (this.bloodGainTimer >= BloodConstants.BLOOD_GAIN_RATE) {
            this.currentBlood++;
            this.bloodGainTimer = 0;
            BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
        }
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarInt(this.getMaxBlood());
        buf.writeVarInt(this.getBlood());
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.maxBlood = buf.readVarInt();
        this.currentBlood = buf.readVarInt();
    }
}
