package com.auroali.sanguinisluxuria.client;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.BloodlustClient;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.BloodConstants;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.components.impl.PlayerVampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLStatusEffects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;

public class BLHud {
    private static final AbilityIconHolder[] CACHED_ICONS = new AbilityIconHolder[3];

    public static void render(DrawContext context, float deltaTick) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;
        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(client.player);
        if (!vampire.isVampire())
            return;
        drawBloodDrainIndicator(context, client, vampire, context.getScaledWindowWidth(), context.getScaledWindowHeight());
        drawBoundAbilities(context, client, context.getScaledWindowHeight(), vampire.getAbilties());

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
            context.drawTexture(BLResources.ICONS, fangX, fangY, 0, 0, 26, 9, 256, 256);
        context.drawTexture(BLResources.ICONS, bloodBarX, bloodBarY, 0, 17, 14, 3, 256, 256);

        RenderSystem.disableBlend();

        context.drawTexture(BLResources.ICONS, bloodBarX + 1, bloodBarY, 15, 17, (int) (bloodPercent * 13), 3, 256, 256);
        if (!VampireHelper.isMasked(client.player))
            context.drawTexture(BLResources.ICONS, fangX, fangY + (int) (9 * (1 - drainPercent)), 0, 9 + (int) (9 * (1 - drainPercent)), 26, (int) (9 * drainPercent), 256, 256);
    }

    public static void drawBoundAbilities(DrawContext context, MinecraftClient client, int height, VampireAbilityContainer container) {
        int x = 16;
        for (int i = 0; i < 3; i++) {
            VampireAbility ability = container.getBoundAbility(i);
            if (ability == null)
                continue;

            ItemStack stack = getOrCreateIcon(i, ability);
            Text text = getTextForSlot(i);
            float offset = (16 - client.textRenderer.getWidth(text)) / 2.0f;

            context.drawTextWithShadow(client.textRenderer, text, (int) (x + offset), height - client.textRenderer.fontHeight, -1);
            context.drawItem(stack, x, height - 16 - client.textRenderer.fontHeight);
            if (container.isOnCooldown(ability)) {
                double progress = (double) container.getCooldown(ability) / container.getMaxCooldown(ability);
                RenderSystem.disableDepthTest();
                context.fill(x, height - client.textRenderer.fontHeight, x + 16, height - (int) (16 * progress) - client.textRenderer.fontHeight, 0x7AFFFFFF);
            }
            x += 22;
        }
    }

    public static Text getTextForSlot(int slot) {
        return switch (slot) {
            case 0 -> Text.keybind(BloodlustClient.ABILITY_1.getTranslationKey());
            case 1 -> Text.keybind(BloodlustClient.ABILITY_2.getTranslationKey());
            default -> null;
        };
    }

    public static ItemStack getOrCreateIcon(int slot, VampireAbility ability) {
        if (CACHED_ICONS[slot] != null && CACHED_ICONS[slot].ability == ability)
            return CACHED_ICONS[slot].icon;

        CACHED_ICONS[slot] = new AbilityIconHolder(ability, ability.getIcon());
        return CACHED_ICONS[slot].icon;
    }

    public record AbilityIconHolder(VampireAbility ability, ItemStack icon) {
    }
}
