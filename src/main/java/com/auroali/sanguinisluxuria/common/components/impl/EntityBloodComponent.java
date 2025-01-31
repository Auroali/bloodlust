package com.auroali.sanguinisluxuria.common.components.impl;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
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
    private boolean wasBaby;

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
        this.wasBaby = this.holder.isBaby();

        // if an entity isn't in the good blood tag, half the max amount of blood
        this.maxBlood = this.recalculateMaxBlood();
        if (this.currentBlood == -1)
            this.currentBlood = this.maxBlood;
        this.currentBlood = Math.min(this.currentBlood, this.maxBlood);

        BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
    }

    int recalculateMaxBlood() {
        if (this.holder.isBaby())
            return 1;

        float maxBloodFromHealth = this.holder.getMaxHealth();
        if (!this.holder.getType().isIn(BLTags.Entities.GOOD_BLOOD))
            maxBloodFromHealth = MathHelper.clamp(maxBloodFromHealth / 2.f, 1.f, Float.MAX_VALUE);
        return (int) Math.ceil(maxBloodFromHealth);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.currentBlood = tag.getInt("Blood");
        this.bloodGainTimer = tag.getInt("BloodTimer");
        // only do this if it exists in the tag cuz otherwise older worlds will have all entities set to zero blood
        if (tag.contains("MaxBlood"))
            this.maxBlood = tag.getInt("MaxBlood");
        this.wasBaby = tag.getBoolean("Baby");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("Blood", this.currentBlood);
        tag.putInt("BloodTimer", this.bloodGainTimer);
        tag.putInt("MaxBlood", this.maxBlood);
        tag.putBoolean("Baby", this.wasBaby);
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
        this.bloodGainTimer = 0;
        if (this.currentBlood == 0)
            this.killHolderFromBloodloss(null);
        return bloodAdded;
    }

    @Override
    public void setBlood(int amount) {
        this.currentBlood = amount;
        this.bloodGainTimer = 0;
        if (this.currentBlood == 0)
            this.killHolderFromBloodloss(null);
        BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
    }

    @Override
    public boolean drainBlood(LivingEntity drainer) {
        if (!this.isEmpty())
            return false;

        this.bloodGainTimer = 0;
        if (this.currentBlood > 1) {
            this.currentBlood--;
            BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
            return true;
        }

        this.currentBlood = 0;
        BLEntityComponents.BLOOD_COMPONENT.sync(this.holder);
        this.killHolderFromBloodloss(drainer);
        return true;
    }

    @Override
    public boolean drainBlood() {
        return this.drainBlood(null);
    }

    @Override
    public boolean isEmpty() {
        return this.getMaxBlood() > 0;
    }

    public void killHolderFromBloodloss(LivingEntity drainer) {
        // vampires can't die from blood loss
        if (VampireHelper.isVampire(this.holder))
            return;

        if (drainer == null)
            this.holder.damage(BLDamageSources.get(this.holder.getWorld(), BLResources.BLOOD_DRAIN_DAMAGE_KEY), Float.MAX_VALUE);
        else
            this.holder.damage(BLDamageSources.bloodDrain(drainer), Float.MAX_VALUE);
    }

    @Override
    public void serverTick() {
        // have to do this here instead of the constructor, as health values aren't available there
        if (this.maxBlood == -1 || this.wasBaby != this.holder.isBaby())
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
