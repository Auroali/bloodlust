package com.auroali.sanguinisluxuria.client.screen;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.BloodlustClient;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.network.BindAbilityC2S;
import com.auroali.sanguinisluxuria.common.registry.BLRegistries;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class VampireAbilitiesScreen extends Screen {
    public VampireAbilityWidget bindingWidget;
    final List<VampireAbilityWidget> abilities;
    double scrollX = 0;
    double scrollY = -32;

    public VampireAbilitiesScreen() {
        super(NarratorManager.EMPTY);
        abilities = new ArrayList<>();
        for (VampireAbility ability : BLRegistries.VAMPIRE_ABILITIES) {
            abilities.add(new VampireAbilityWidget(ability, null));
        }

        abilities.forEach(a -> a.resolveParent(abilities));
        VampireAbilitiesPositioner.position(abilities);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        VampireComponent component = BLEntityComponents.VAMPIRE_COMPONENT.get(client.player);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        int panelX = (width - 252) / 2;
        int panelY = (height - 147) / 2;

        context.drawTexture(BLResources.ABILITIES_BG, panelX + 9, panelY + 18, 0, 0, 234, 113);

        int centerX = width / 2;
        int centerY = height / 2;

        context.enableScissor(panelX + 9, panelY + 18, panelX + 242, panelY + 131);
        abilities.forEach(a -> a.render(context, component.getAbilties(), (int) (centerX + scrollX), (int) (centerY + scrollY), mouseX, mouseY));
        context.disableScissor();

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        context.drawTexture(BLResources.ABILITIES_SCREEN, panelX, panelY, 0, 0, 252, 148);
        RenderSystem.disableBlend();

        context.drawText(client.textRenderer, Text.translatable("gui.sanguinisluxuria.abilities"), panelX + 8, panelY + 6, 4210752, false);
        context.drawText(client.textRenderer, Text.translatable("gui.sanguinisluxuria.skill_points", component.getSkillPoints()), panelX + 8, panelY + 134, 4210752, false);

        for (VampireAbilityWidget ability : abilities) {
            if (ability.isMouseOver(mouseX, mouseY, (int) (centerX + scrollX), (int) (centerY + scrollY))) {
                renderAbilityWidgetTooltip(context, mouseX, mouseY, component.getAbilties(), ability);
            }
        }
    }

    public void renderAbilityWidgetTooltip(DrawContext context, int mouseX, int mouseY, VampireAbilityContainer container, VampireAbilityWidget widget) {
        ArrayList<Text> text = new ArrayList<>();
        text.add(Text.translatable(widget.ability.getTranslationKey()));
        text.add(Text.translatable(widget.ability.getDescTranslationKey()));

        if (!container.hasAbility(widget.ability))
            text.add(Text.translatable("gui.sanguinisluxuria.abilities.required_skill_points", widget.ability.getRequiredSkillPoints())
              .formatted(Formatting.GRAY));

        List<VampireAbility> incompatibilities = widget.ability.getIncompatibilities();
        if (!incompatibilities.isEmpty()) {
            text.add(Text.translatable("gui.sanguinisluxuria.abilities.incompatibilites"));
            for (VampireAbility ability : incompatibilities) {
                text.add(Text.translatable(
                  "gui.sanguinisluxuria.abilities.incompatibilites_entry",
                  Text.translatable(ability.getTranslationKey())
                ).formatted(Formatting.DARK_RED, Formatting.ITALIC));
            }
        }

        int slot = container.getAbilityBinding(widget.ability);
        if (widget == bindingWidget)
            text.add(Text.translatable("gui.sanguinisluxuria.abilities.binding").formatted(Formatting.GRAY, Formatting.ITALIC));
        else if (slot == -1 && widget.ability.isKeybindable())
            text.add(Text.translatable("gui.sanguinisluxuria.abilities.bind_prompt").formatted(Formatting.GRAY, Formatting.ITALIC));
        else if (widget.ability.isKeybindable())
            text.add(Text.translatable("gui.sanguinisluxuria.abilities.bound", getTextForSlot(slot)).formatted(Formatting.GRAY, Formatting.ITALIC));
        context.drawTooltip(client.textRenderer, text, mouseX, mouseY);
    }

    public static Text getTextForSlot(int slot) {
        return switch (slot) {
            case 0 -> Text.keybind(BloodlustClient.ABILITY_1.getTranslationKey());
            case 1 -> Text.keybind(BloodlustClient.ABILITY_2.getTranslationKey());
            default -> null;
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingWidget == null)
            return super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            sendBindPacket(-1);
            bindingWidget = null;
            return true;
        }

        if (keyCode == BloodlustClient.ABILITY_1.getDefaultKey().getCode()) {
            sendBindPacket(0);
            return true;
        }
        if (keyCode == BloodlustClient.ABILITY_2.getDefaultKey().getCode()) {
            sendBindPacket(1);
            return true;
        }

        bindingWidget = null;
        return false;
    }

    private void sendBindPacket(int i) {
        ClientPlayNetworking.send(new BindAbilityC2S(bindingWidget.ability, i));
        bindingWidget = null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (bindingWidget != null)
            return false;
        for (VampireAbilityWidget ability : abilities) {
            if (ability.isMouseOver((int) mouseX, (int) mouseY, (int) (width / 2 + scrollX), (int) (height / 2 + scrollY)) && ability.onClick(this, button))
                return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean dragged = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        if (!dragged) {
            scrollX += deltaX;
            scrollY += deltaY;
            return false;
        }
        return true;
    }
}
