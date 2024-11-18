package com.auroali.sanguinisluxuria.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;

public class VampireAbilitiesPositioner {
    public static void position(List<VampireAbilityWidget> widgets) {
        final int rowSpacing = 32;
        final int columnSpacing = 32;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        List<VampireAbilityWidget> abilities = widgets.stream().filter(w -> w.parent == null && !w.ability.isHidden(player)).toList();
        int currentRow = 0;
        while (!abilities.isEmpty()) {
            int currentX = 0;
            for (VampireAbilityWidget widget : abilities) {
                if (widget.parent != null)
                    widget.setX(widget.parent.getX());
                else {
                    widget.setX(currentX * columnSpacing);
                    currentX++;
                }

                widget.setY(currentRow * rowSpacing);
            }
            if (currentRow == 0) {
                int size = abilities.size();
                abilities.forEach(a ->
                  a.setX(a.getX() - columnSpacing / 2 * (size))
                );
            }
            resolveCollisions(abilities, columnSpacing);
            currentRow++;
            abilities = abilities
              .stream()
              .flatMap(w -> w.getChildren().stream())
              .filter((w -> !w.ability.isHidden(player)))
              .toList();
        }
    }

    private static void resolveCollisions(List<VampireAbilityWidget> widgets, @SuppressWarnings("SameParameterValue") int columnSpacing) {
        boolean hasCollisions = true;
        while (hasCollisions) {
            hasCollisions = false;
            for (VampireAbilityWidget widget : widgets) {
                for (VampireAbilityWidget other : widgets) {
                    if (other != widget && widget.isOverlappingX(other)) {
                        int dir = 1;
                        if (other.parent.getX() > widget.parent.getX())
                            dir = -1;
                        widget.setX(widget.getX() + dir * (columnSpacing / 2));
                        other.setX(other.getX() - dir * (columnSpacing / 2));
                        hasCollisions = true;
                    }
                }
            }
        }

        hasCollisions = true;
        while (hasCollisions) {
            hasCollisions = false;
            for (VampireAbilityWidget widget : widgets) {
                if (widget.parent == null)
                    continue;
                boolean next = false;
                while (!next) {
                    int original = widget.getX();
                    if (widget.getX() < widget.parent.getX() - columnSpacing / 2) {
                        widget.setX(widget.getX() + columnSpacing / 2);
                        hasCollisions = true;
                    }
                    if (widget.getX() > widget.parent.getX() + columnSpacing / 2) {
                        widget.setX(widget.getX() - columnSpacing / 2);
                        hasCollisions = true;
                    }
                    if (widget.getX() == original)
                        break;
                    for (VampireAbilityWidget other : widgets) {
                        if (other != widget && (widget.isOverlappingX(other) || other.isOverlappingX(widget))) {
                            widget.setX(original);
                            hasCollisions = false;
                            next = true;
                            break;
                        }
                    }
                }
            }
        }
    }
}
