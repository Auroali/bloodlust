package com.auroali.sanguinisluxuria.common.abilities;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Utility class for creating common ability conditions
 */
public class AbilityConditions {
    public static VampireAbility.VampireAbilityCondition hasAbility(VampireAbility ability) {
        return (entity, vampire, container) -> container.hasAbility(ability);
    }

    public static VampireAbility.VampireAbilityCondition lacksAbility(VampireAbility ability) {
        return (entity, vampire, container) -> !container.hasAbility(ability);
    }

    public static VampireAbility.VampireAbilityCondition hasAdvancement(Identifier advancementId) {
        return (entity, vampire, container) -> {
            if (entity instanceof ServerPlayerEntity player) {
                MinecraftServer server = player.getServer();
                PlayerAdvancementTracker tracker = player.getAdvancementTracker();
                if (server == null)
                    return false;
                Advancement advancement = server.getAdvancementLoader().get(advancementId);
                if (advancement == null)
                    return false;
                return tracker.getProgress(advancement).isDone();
            }
            return false;
        };
    }

    public static VampireAbility.VampireAbilityCondition or(VampireAbility.VampireAbilityCondition... conditions) {
        return (entity, vampire, container) -> {
            for (VampireAbility.VampireAbilityCondition condition : conditions) {
                if (condition.test(entity, vampire, container))
                    return true;
            }
            return false;
        };
    }

    public static VampireAbility.VampireAbilityCondition and(VampireAbility.VampireAbilityCondition... conditions) {
        return (entity, vampire, container) -> {
            if (conditions.length == 0)
                return false;
            boolean result = true;
            for (VampireAbility.VampireAbilityCondition condition : conditions) {
                result = result && condition.test(entity, vampire, container);
            }
            return result;
        };
    }

    public static VampireAbility.VampireAbilityCondition not(VampireAbility.VampireAbilityCondition condition) {
        return (entity, vampire, container) -> !condition.test(entity, vampire, container);
    }
}
