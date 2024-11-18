package com.auroali.sanguinisluxuria.client.screen;

import com.auroali.sanguinisluxuria.BLResources;
import com.auroali.sanguinisluxuria.VampireHelper;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.network.UnlockAbilityC2S;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntComparators;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VampireAbilityWidget implements Comparable<VampireAbilityWidget> {
    public static final int WIDTH = 19;
    public static final int HEIGHT = 19;

    VampireAbility ability;
    VampireAbilityWidget parent;
    ItemStack icon;
    List<VampireAbilityWidget> children;
    private boolean hidden;
    private int x;
    private int y;
    int childrenMinX = Integer.MIN_VALUE;
    int childrenMaxX = Integer.MIN_VALUE;

    public VampireAbilityWidget(VampireAbility ability, VampireAbilityWidget parent) {
        this.ability = ability;
        this.parent = parent;
        this.icon = ability.getIcon();
        this.children = new ArrayList<>();
        this.hidden = ability.isHidden(MinecraftClient.getInstance().player);
    }

    public void resolveParent(List<VampireAbilityWidget> abilities) {
        if (hidden)
            return;
        for (VampireAbilityWidget widget : abilities) {
            if (widget.ability == ability.getParent()) {
                this.parent = widget;
                this.parent.getChildren().add(this);
                if (!this.hidden) {
                    this.hidden = parent.hidden;
                    progateHiddenStateToChildren();
                }
                return;
            }
        }
    }

    public void progateHiddenStateToChildren() {
        List<VampireAbilityWidget> children = this.children.stream().filter(c -> !c.hidden).toList();
        while (!children.isEmpty()) {
            children.forEach(c -> c.hidden = hidden);
            children = children
              .stream()
              .flatMap(c -> c.getChildren().stream())
              .filter(c -> !c.hidden)
              .toList();
        }
    }

    public void render(DrawContext context, VampireAbilityContainer container, int offsetX, int offsetY, int mouseX, int mouseY) {
        if (hidden)
            return;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        drawLines(context, container, offsetX, offsetY);
        context.drawTexture(
          BLResources.ICONS,
          offsetX + x,
          offsetY + y,
          0,
          container.hasAbility(ability) ? 46 + HEIGHT : 46,
          WIDTH,
          HEIGHT,
          256,
          256
        );
        context.drawItem(
          icon,
          getX() + offsetX + 1,
          getY() + offsetY + 1
        );
        RenderSystem.disableDepthTest();
    }

    public void drawLines(DrawContext context, VampireAbilityContainer container, int offsetX, int offsetY) {
        if (children.isEmpty())
            return;

        if (childrenMinX == Integer.MIN_VALUE && childrenMaxX == Integer.MIN_VALUE)
            calculateLineMinMaxX();

//        int colour = -1;
//        int blackColour = 0xFF000000;
//

        int yOff = offsetY + (children.get(0).getY() - y + 16) / 2;

        context.fill(offsetX + childrenMinX + WIDTH / 2 - 1, y + yOff - 1, offsetX + childrenMaxX + WIDTH / 2 + 2, y + yOff + 2, 0xFF000000);
        context.fill(offsetX + x + WIDTH / 2 - 1, offsetY + y + 16, offsetX + x + WIDTH / 2 + 2, y + yOff + 2, 0xFF000000);
        children.stream().sorted().forEach(w -> {
            int colour = -1;
            if (VampireHelper.hasIncompatibleAbility(MinecraftClient.getInstance().player, w.ability) || !container.hasAbility(ability)) {
                colour = 0xFF6C0000;
            } else if (container.hasAbility(w.ability)) {
                colour = 0xFFFF6E11;
            }
            int childYOff = (w.getY() - y) / 4;
            context.fill(offsetX + w.getX() + WIDTH / 2 - 1, offsetY + w.getY() - childYOff + 2, offsetX + w.getX() + WIDTH / 2 + 2, offsetY + w.getY(), 0xFF000000);
            context.drawVerticalLine(offsetX + w.getX() + WIDTH / 2, offsetY + w.getY() - childYOff, offsetY + w.getY(), colour);
            context.drawVerticalLine(offsetX + x + WIDTH / 2, offsetY + y, y + yOff, colour);
            context.drawHorizontalLine(offsetX + w.getX() + WIDTH / 2, offsetX + x + WIDTH / 2, y + yOff, colour);
        });
    }

    public void calculateLineMinMaxX() {
        if (children.isEmpty())
            return;

        childrenMinX = getX();
        childrenMaxX = getX();
        for (VampireAbilityWidget w : children) {
            if (childrenMinX > w.getX())
                childrenMinX = w.getX();
            if (childrenMaxX < w.getX())
                childrenMaxX = w.getX();
        }
    }

    public boolean isMouseOver(int mouseX, int mouseY, int offsetX, int offsetY) {
        if (hidden)
            return false;
        int x = getX() + offsetX;
        int y = getY() + offsetY;

        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;

    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<VampireAbilityWidget> getChildren() {
        return children;
    }

    public boolean isOverlappingX(VampireAbilityWidget other) {
        if (hidden)
            return false;
        return getX() >= other.getX() && getX() <= other.getX() + WIDTH;
    }

    public boolean isOverlappingY(VampireAbilityWidget other) {
        if (hidden)
            return false;
        return getY() >= other.getY() && getY() <= other.getY() + HEIGHT;
    }

    public boolean onClick(VampireAbilitiesScreen screen, int button) {
        if (hidden)
            return false;
        ClientPlayerEntity entity = MinecraftClient.getInstance().player;
        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(entity);
        if (button == 0 && tryUnlock(vampire, entity))
            return true;
        if (button == 1 && bindTo(vampire, entity, screen))
            return true;
        return false;
    }

    private boolean bindTo(VampireComponent vampire, ClientPlayerEntity entity, VampireAbilitiesScreen screen) {
        if (!vampire.getAbilties().hasAbility(ability) || !ability.isKeybindable())
            return false;

        screen.bindingWidget = this;
        return true;
    }

    private boolean tryUnlock(VampireComponent vampire, ClientPlayerEntity entity) {
        if (vampire.getSkillPoints() < ability.getRequiredSkillPoints() || vampire.getAbilties().hasAbility(ability) || !vampire.getAbilties().hasAbility(ability.getParent())) {
            entity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
            return false;
        }

        if (VampireHelper.hasIncompatibleAbility(vampire.getAbilties(), ability))
            return false;

        entity.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
        ClientPlayNetworking.send(new UnlockAbilityC2S(ability));
        return true;
    }

    @Override
    public int compareTo(@NotNull VampireAbilityWidget ability) {
        if (parent != null) {
            int xDiff = x - parent.x;
            int xDiff2 = ability.x - parent.x;

            return IntComparators.OPPOSITE_COMPARATOR.compare(Math.abs(xDiff), Math.abs(xDiff2));
        }
        return Integer.compare(x, ability.x);
    }
}
