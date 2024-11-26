package com.auroali.sanguinisluxuria.compat;

import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.registry.BLTags;
import net.minecraft.client.MinecraftClient;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.HUDOverlayEvent;

public class AppleskinCompat implements AppleSkinApi {
    @Override
    public void registerEvents() {
        HUDOverlayEvent.Saturation.EVENT.register(this::cancelEventIfVampire);
        HUDOverlayEvent.HungerRestored.EVENT.register(this::cancelEventIfVampire);
        HUDOverlayEvent.HealthRestored.EVENT.register(event -> {
            if (!event.itemStack.isIn(BLTags.Items.VAMPIRES_GET_HUNGER_FROM))
                this.cancelEventIfVampire(event);
        });

    }

    public void cancelEventIfVampire(HUDOverlayEvent event) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (VampireHelper.isVampire(client.player))
            event.isCanceled = true;
    }
}
