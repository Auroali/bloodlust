package com.auroali.sanguinisluxuria.client;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.BloodlustClient;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.blood.BloodConstants;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.components.impl.PlayerVampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLStatusEffects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

public class BLHud {
    public static void render(DrawContext context, float deltaTick) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;
        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(client.player);
        if (!vampire.isVampire())
            return;
        drawBloodDrainIndicator(context, client, vampire, context.getScaledWindowWidth(), context.getScaledWindowHeight());
        showAbilityCooldowns(context, client, context.getScaledWindowHeight(), vampire.getAbilties());
    }

    private static boolean targetHasBleeding(VampireComponent component, LivingEntity entity) {
        return component instanceof PlayerVampireComponent p ? p.targetHasBleeding : entity.hasStatusEffect(BLStatusEffects.BLEEDING);
    }

    private static void drawBloodDrainIndicator(DrawContext context, MinecraftClient client, VampireComponent vampire, int width, int height) {
        if (!BloodlustClient.isLookingAtValidTarget())
            return;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
          GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
        );


        Entity targetedEntity = ((EntityHitResult) client.crosshairTarget).getEntity();
        BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(targetedEntity);

        int timeToDrain = BloodConstants.BLOOD_DRAIN_TIME;
        if (targetedEntity instanceof LivingEntity entity && targetHasBleeding(vampire, entity))
            timeToDrain = BloodConstants.BLOOD_DRAIN_TIME_BLEEDING;

        double drainPercent = (double) vampire.getBloodDrainTimer() / timeToDrain;
        double bloodPercent = (double) blood.getBlood() / blood.getMaxBlood();

        int fangX = (width - 26) / 2;
        int fangY = (height - 9) / 2;

        int bloodBarX = (width - 14) / 2;
        int bloodBarY = height / 2 + 5;
        if (!VampireHelper.isMasked(client.player))
            context.drawTexture(BLResources.ICONS, fangX, fangY, 0, 0, 26, 7, 256, 256);
        context.drawTexture(BLResources.ICONS, bloodBarX, bloodBarY, 0, 14, 14, 3, 256, 256);

        RenderSystem.disableBlend();

        context.drawTexture(BLResources.ICONS, bloodBarX + 1, bloodBarY, 15, 14, (int) (bloodPercent * 13), 3, 256, 256);
        int drainPosY = (int) (7 * drainPercent);
        if (!VampireHelper.isMasked(client.player))
            context.drawTexture(BLResources.ICONS, fangX, fangY + (7 - drainPosY), 0, 7 + (7 - drainPosY), 26, drainPosY, 256, 256);
    }

    public static void showAbilityCooldowns(DrawContext context, MinecraftClient client, int height, VampireAbilityContainer container) {
        TextRenderer renderer = client.textRenderer;
        context.getMatrices().push();
        context.getMatrices().translate(2, -2, 0);
        for (VampireAbility ability : container) {
            if (!container.isOnCooldown(ability))
                continue;

            int cooldown = container.getCooldown(ability);
            int maxCooldown = container.getMaxCooldown(ability);
            float cooldownPercent = MathHelper.clamp(cooldown / (float) maxCooldown, 0.f, 1.f);
            context.drawTexture(BLResources.ICONS, 0, height - 17, 0, 46, 64, 17);
            context.drawText(client.textRenderer, Text.translatable(ability.getTranslationKey()), 4, height - 13, -1, false);
            context.drawTexture(BLResources.ICONS, 3, height - 3, 0, 63, (int) (56 * cooldownPercent), 1);
            context.getMatrices().translate(0, -20, 0);
        }
        context.getMatrices().pop();
    }
}
