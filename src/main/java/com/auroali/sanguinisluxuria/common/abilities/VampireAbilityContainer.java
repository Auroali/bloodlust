package com.auroali.sanguinisluxuria.common.abilities;

import com.auroali.sanguinisluxuria.Bloodlust;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VampireAbilityContainer implements Iterable<VampireAbility> {
    private Set<VampireAbility> abilities;
    private Map<VampireAbility, AbilityCooldown> cooldowns;
    @SuppressWarnings("rawtypes")
    private final Object2ObjectOpenHashMap<VampireAbility, VampireAbility.AbilityTicker> tickers = new Object2ObjectOpenHashMap<>();
    private boolean shouldSync = true;

    public VampireAbilityContainer() {
        this.abilities = new ObjectOpenHashSet<>();
        this.cooldowns = new Object2ObjectOpenHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public void tick(LivingEntity entity, VampireComponent vampire) {
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(entity);
        this.tickers.object2ObjectEntrySet().fastForEach(p -> p.getValue().tick(p.getKey(), entity.getWorld(), entity, vampire, this, blood));
        this.cooldowns.entrySet().removeIf(e -> {
            this.setShouldSync(true);
            return e.getKey().canTickCooldown(entity, vampire) && e.getValue().ticks-- == 0;
        });
    }

    public void addAbility(VampireAbility ability) {
        this.setShouldSync(true);
        this.abilities.add(ability);
        VampireAbility.AbilityTicker<?> ticker = ability.createTicker();
        if (ticker != null)
            this.tickers.put(ability, ticker);
    }

    public void removeAbility(VampireAbility ability) {
        this.setShouldSync(true);
        this.cooldowns.remove(ability);
        this.abilities.remove(ability);
        this.tickers.remove(ability);
    }


    public void setCooldown(VampireAbility ability, int cooldown) {
        if (!this.hasAbility(ability))
            return;

        this.setShouldSync(true);
        this.cooldowns.put(ability, new AbilityCooldown(cooldown));
    }

    public int getCooldown(VampireAbility ability) {
        AbilityCooldown cooldown = this.cooldowns.get(ability);
        if (cooldown == null)
            return 0;
        return cooldown.ticks;
    }

    public int getMaxCooldown(VampireAbility ability) {
        AbilityCooldown cooldown = this.cooldowns.get(ability);
        if (cooldown == null)
            return 0;
        return cooldown.maxTicks;
    }

    public boolean isOnCooldown(VampireAbility ability) {
        return this.getCooldown(ability) > 0;
    }

    public boolean hasAbility(VampireAbility ability) {
        if (ability == null)
            return true;
        return this.abilities.contains(ability);
    }

    public boolean hasAbilityIn(TagKey<VampireAbility> tag) {
        return this.abilities.stream().anyMatch(a -> a.isIn(tag));
    }

    public List<VampireAbility> getAbilitiesIn(TagKey<VampireAbility> tag) {
        return this.abilities.stream().filter(a -> a.isIn(tag)).toList();
    }

    public void save(NbtCompound compound) {
        NbtList abilityTag = new NbtList();
        NbtList cooldownsTag = new NbtList();
        for (VampireAbility ability : this.abilities) {
            Identifier id = BLRegistries.VAMPIRE_ABILITIES.getId(ability);
            if (id == null) {
                Bloodlust.LOGGER.warn("Could not find id for an ability!");
                continue;
            }
            abilityTag.add(NbtString.of(id.toString()));
        }

        this.cooldowns.forEach((ability, cooldown) -> {
            Identifier id = BLRegistries.VAMPIRE_ABILITIES.getId(ability);
            if (id == null) {
                Bloodlust.LOGGER.warn("Could not find id for an ability!");
                return;
            }
            NbtCompound tag = new NbtCompound();
            tag.putString("Ability", id.toString());
            tag.putInt("Ticks", cooldown.ticks);
            tag.putInt("MaxTicks", cooldown.maxTicks);
            cooldownsTag.add(tag);
        });

        compound.put("VampireAbilities", abilityTag);
        compound.put("Cooldowns", cooldownsTag);
    }

    public void load(NbtCompound compound) {
        this.abilities.clear();
        NbtList abilityTag = compound.getList("VampireAbilities", NbtElement.STRING_TYPE);
        NbtList cooldownsTag = compound.getList("Cooldowns", NbtElement.COMPOUND_TYPE);
        abilityTag.stream()
          .map(NbtString.class::cast)
          .forEach(s -> {
              Identifier id = Identifier.tryParse(s.asString());
              if (id == null) {
                  Bloodlust.LOGGER.warn("Could not get ability for {}", s.asString());
                  return;
              }

              VampireAbility ability = BLRegistries.VAMPIRE_ABILITIES.get(id);
              if (ability == null) {
                  Bloodlust.LOGGER.warn("Could not get ability for {}", id);
                  return;
              }

              this.abilities.add(ability);
              VampireAbility.AbilityTicker<?> ticker = ability.createTicker();
              if (ticker != null)
                  this.tickers.put(ability, ticker);
          });
        this.cooldowns.clear();
        for (int i = 0; i < cooldownsTag.size(); i++) {
            NbtCompound cooldown = cooldownsTag.getCompound(i);
            Identifier id = Identifier.tryParse(cooldown.getString("Ability"));
            int ticks = cooldown.getInt("Ticks");
            int maxTicks = cooldown.getInt("MaxTicks");

            if (id == null) {
                Bloodlust.LOGGER.warn("Could not get ability for {}", cooldownsTag.getString(i));
                continue;
            }

            VampireAbility ability = BLRegistries.VAMPIRE_ABILITIES.get(id);
            if (ability == null) {
                Bloodlust.LOGGER.warn("Could not get ability for {}", id);
                continue;
            }

            this.cooldowns.put(ability, new AbilityCooldown(ticks, maxTicks));
        }
        this.setShouldSync(true);
    }

    public void writePacket(PacketByteBuf buf) {
        // write unlocked abilities
        buf.writeCollection(this.abilities, (buffer, ability) -> buffer.writeRegistryValue(BLRegistries.VAMPIRE_ABILITIES, ability));

        // write cooldowns
        buf.writeMap(
          this.cooldowns,
          (buffer, key) ->
            buffer.writeRegistryValue(BLRegistries.VAMPIRE_ABILITIES, key)
          ,
          (buffer, value) -> {
              buffer.writeVarInt(value.ticks);
              buffer.writeVarInt(value.maxTicks);
          }
        );
    }

    public void readPacket(PacketByteBuf buf) {
        this.abilities = buf.readCollection(HashSet::new, buffer -> buffer.readRegistryValue(BLRegistries.VAMPIRE_ABILITIES));
        this.cooldowns = buf.readMap(HashMap::new, buffer -> buffer.readRegistryValue(BLRegistries.VAMPIRE_ABILITIES), buffer -> new AbilityCooldown(buffer.readVarInt(), buffer.readVarInt()));
    }

    public boolean needsSync() {
        return this.shouldSync;
    }

    public void setShouldSync(boolean shouldSync) {
        this.shouldSync = shouldSync;
    }

    @NotNull
    @Override
    public Iterator<VampireAbility> iterator() {
        return this.abilities.iterator();
    }

    @Override
    public Spliterator<VampireAbility> spliterator() {
        return this.abilities.spliterator();
    }

    private static class AbilityCooldown {
        int ticks;
        final int maxTicks;

        public AbilityCooldown(int ticks) {
            this(ticks, ticks);
        }

        public AbilityCooldown(int ticks, int maxTicks) {
            this.ticks = ticks;
            this.maxTicks = maxTicks;
        }
    }
}
