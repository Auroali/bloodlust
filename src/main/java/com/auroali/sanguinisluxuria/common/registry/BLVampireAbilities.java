package com.auroali.sanguinisluxuria.common.registry;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.common.abilities.BiteAbility;
import com.auroali.sanguinisluxuria.common.abilities.InfectiousAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireTeleportAbility;
import net.minecraft.registry.Registry;

public class BLVampireAbilities {
    public static final VampireTeleportAbility TELEPORT = new VampireTeleportAbility();
    public static final InfectiousAbility INFECTIOUS = new InfectiousAbility();
    public static final VampireAbility BITE = new BiteAbility();

    public static void register() {
        Registry.register(BLRegistries.VAMPIRE_ABILITIES, BLResources.TELEPORT_ID, TELEPORT);
        Registry.register(BLRegistries.VAMPIRE_ABILITIES, BLResources.TRANSFER_EFFECTS_ID, INFECTIOUS);
        Registry.register(BLRegistries.VAMPIRE_ABILITIES, BLResources.BITE_ID, BITE);
    }
}
