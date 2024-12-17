package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.common.advancements.*;
import net.minecraft.advancement.criterion.Criteria;

public class BLAdvancementCriterion {
    public static final BecomeVampireCriterion BECOME_VAMPIRE = new BecomeVampireCriterion();
    public static final UnbecomeVampireCriterion UNBECOME_VAMPIRE = new UnbecomeVampireCriterion();
    public static final UnlockAbilityCriterion UNLOCK_ABILITY = new UnlockAbilityCriterion();
    public static final ResetAbilitiesCriterion RESET_ABILITIES = new ResetAbilitiesCriterion();
    public static final InfectEntityCriterion INFECT_ENTITY = new InfectEntityCriterion();
    public static final TransferEffectsCriterion TRANSFER_EFFECTS = new TransferEffectsCriterion();
    public static final PerformRitualCriterion PERFORM_RITUAL = new PerformRitualCriterion();

    public static void register() {
        Criteria.register(BECOME_VAMPIRE);
        Criteria.register(UNBECOME_VAMPIRE);
        Criteria.register(UNLOCK_ABILITY);
        Criteria.register(RESET_ABILITIES);
        Criteria.register(INFECT_ENTITY);
        Criteria.register(TRANSFER_EFFECTS);
        Criteria.register(PERFORM_RITUAL);
    }
}
