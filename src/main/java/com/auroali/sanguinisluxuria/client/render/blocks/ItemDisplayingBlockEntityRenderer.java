package com.auroali.sanguinisluxuria.client.render.blocks;

import com.auroali.sanguinisluxuria.common.blockentities.ItemDisplayingBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class ItemDisplayingBlockEntityRenderer<T extends BlockEntity & ItemDisplayingBlockEntity> implements BlockEntityRenderer<T> {
    final ItemRenderer itemRenderer;
    final Random random;

    public ItemDisplayingBlockEntityRenderer(ItemRenderer itemRenderer) {
        this.itemRenderer = itemRenderer;
        this.random = Random.create();
    }

    private int getRenderedAmount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }

        return i;
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getDisplayItem().isEmpty())
            return;
        ItemStack displayItem = entity.getDisplayItem();
        int displayTicks = entity.getDisplayTicks();
        Vec3d displayOffset = entity.getDisplayOffset();

        matrices.push();
        random.setSeed(Item.getRawId(displayItem.getItem()) + displayItem.getDamage());
        matrices.translate(displayOffset.getX(), displayOffset.getY(), displayOffset.getZ());
        float bobbing = MathHelper.sin((displayTicks + tickDelta) / 10.0F) * 0.1F + 0.1F;
        //matrices.translate(0, bobbing, 0);
        float rotation = (displayTicks + tickDelta) / 20.0f;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rotation));

        BakedModel model = itemRenderer.getModel(displayItem, entity.getWorld(), null, entity.getPos().hashCode());
        boolean hasDepth = model.hasDepth();

        float yScale = model.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y();
        matrices.translate(0.0, (bobbing + 0.25F * yScale), 0.0);
        float xGroundScale = model.getTransformation().ground.scale.x();
        float yGroundScale = model.getTransformation().ground.scale.y();
        float zGroundScale = model.getTransformation().ground.scale.z();
        int amount = getRenderedAmount(displayItem);
        if (!hasDepth) {
            float r = -0.0F * (float) (amount - 1) * 0.5F * xGroundScale;
            float s = -0.0F * (float) (amount - 1) * 0.5F * yGroundScale;
            float t = -0.09375F * (float) (amount - 1) * 0.5F * zGroundScale;
            matrices.translate(r, s, t);
        }

        for (int i = 0; i < amount; ++i) {
            matrices.push();
            if (i > 0) {
                if (hasDepth) {
                    float s = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float t = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float v = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    matrices.translate(s, t, v);
                } else {
                    float s = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float t = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    matrices.translate(s, t, 0.0);
                }
            }

            this.itemRenderer
              .renderItem(displayItem, ModelTransformationMode.GROUND, false, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, model);
            matrices.pop();
            if (!hasDepth) {
                matrices.translate((0.0F * xGroundScale), (0.0F * yGroundScale), (0.09375F * zGroundScale));
            }
        }
        matrices.pop();
    }
}
