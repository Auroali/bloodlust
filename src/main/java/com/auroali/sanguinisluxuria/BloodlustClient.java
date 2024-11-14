package com.auroali.sanguinisluxuria;

import com.auroali.sanguinisluxuria.client.BLHud;
import com.auroali.sanguinisluxuria.client.particles.DrippingBloodParticle;
import com.auroali.sanguinisluxuria.client.render.blocks.PedestalBlockRenderer;
import com.auroali.sanguinisluxuria.client.render.entities.VampireMerchantRenderer;
import com.auroali.sanguinisluxuria.client.render.entities.VampireVillagerRenderer;
import com.auroali.sanguinisluxuria.client.screen.VampireAbilitiesScreen;
import com.auroali.sanguinisluxuria.common.abilities.SyncableVampireAbility;
import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.auroali.sanguinisluxuria.common.network.ActivateAbilityC2S;
import com.auroali.sanguinisluxuria.common.network.AltarRecipeStartS2C;
import com.auroali.sanguinisluxuria.common.network.DrainBloodC2S;
import com.auroali.sanguinisluxuria.common.network.HungryDecayedLogVFXS2C;
import com.auroali.sanguinisluxuria.common.registry.*;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class BloodlustClient implements ClientModInitializer {
    public static KeyBinding SUCK_BLOOD = new KeyBinding(
      "key.sanguinisluxuria.bite",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_R,
      "category.sanguinisluxuria.sanguinisluxuria"
    );
    public static KeyBinding OPEN_ABILITIES = new KeyBinding(
      "key.sanguinisluxuria.open_abilities",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_Y,
      "category.sanguinisluxuria.sanguinisluxuria"
    );
    public static KeyBinding ABILITY_1 = new KeyBinding(
      "key.sanguinisluxuria.ability_1",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_Z,
      "category.sanguinisluxuria.sanguinisluxuria"
    );
    public static KeyBinding ABILITY_2 = new KeyBinding(
      "key.sanguinisluxuria.ability_2",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_X,
      "category.sanguinisluxuria.sanguinisluxuria"
    );

    public static boolean isAltarActive = false;

    public boolean drainingBlood;

    @Override
    public void onInitializeClient() {
        registerBindings();

        BLModelLayers.register();

        TrinketRendererRegistry.registerRenderer(BLItems.MASK_1, BLItems.MASK_1);
        TrinketRendererRegistry.registerRenderer(BLItems.MASK_2, BLItems.MASK_2);
        TrinketRendererRegistry.registerRenderer(BLItems.MASK_3, BLItems.MASK_3);

        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.addModels(BLResources.MASK_ONE_ID.withPrefixedPath("item/").withSuffixedPath("_inventory"));
            pluginContext.addModels(BLResources.MASK_TWO_ID.withPrefixedPath("item/").withSuffixedPath("_inventory"));
            pluginContext.addModels(BLResources.MASK_THREE_ID.withPrefixedPath("item/").withSuffixedPath("_inventory"));
        });

        BloodStorageItem.registerModelPredicate(BLItems.BLOOD_BAG);
        BloodStorageItem.registerModelPredicate(BLItems.BLOOD_BOTTLE);

        BlockRenderLayerMap.INSTANCE.putBlock(BLBlocks.BLOOD_SPLATTER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BLBlocks.PEDESTAL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BLBlocks.ALTAR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BLBlocks.DECAYED_TWIGS, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BLBlocks.GRAFTED_SAPLING, RenderLayer.getCutout());

        EntityRendererRegistry.register(BLEntities.VAMPIRE_VILLAGER, VampireVillagerRenderer::new);
        EntityRendererRegistry.register(BLEntities.VAMPIRE_MERCHANT, VampireMerchantRenderer::new);

        BlockEntityRendererFactories.register(BLBlockEntities.PEDESTAL, ctx -> new PedestalBlockRenderer(ctx.getItemRenderer()));

        HudRenderCallback.EVENT.register(BLHud::render);

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), BLFluids.BLOOD);

        FluidRenderHandlerRegistry.INSTANCE.register(BLFluids.BLOOD, new SimpleFluidRenderHandler(
          BLResources.BLOOD_STILL_TEXTURE,
          BLResources.BLOOD_FLOWING_TEXTURE
        ));

        ParticleFactoryRegistry.getInstance().register(BLParticles.DRIPPING_BLOOD, sprite -> (type, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            SpriteBillboardParticle particle = DrippingBloodParticle.createDrippingBlood(type, world, x, y, z, velocityX, velocityY, velocityZ);
            particle.setSprite(sprite);
            return particle;
        });
        ParticleFactoryRegistry.getInstance().register(BLParticles.FALLING_BLOOD, sprite -> (type, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            SpriteBillboardParticle particle = DrippingBloodParticle.createFallingBlood(type, world, x, y, z, velocityX, velocityY, velocityZ);
            particle.setSprite(sprite);
            return particle;
        });
        ParticleFactoryRegistry.getInstance().register(BLParticles.LANDING_BLOOD, sprite -> (type, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            SpriteBillboardParticle particle = DrippingBloodParticle.createLandingBlood(type, world, x, y, z, velocityX, velocityY, velocityZ);
            particle.setSprite(sprite);
            return particle;
        });

        ClientPlayNetworking.registerGlobalReceiver(BLResources.ABILITY_SYNC_CHANNEL, (client, handler, buf, responseSender) -> {
            int id = buf.readVarInt();
            VampireAbility ability = buf.readRegistryValue(BLRegistries.VAMPIRE_ABILITIES);
            if (client.world != null && client.world.getEntityById(id) instanceof LivingEntity entity && ability instanceof SyncableVampireAbility<?> s)
                s.handlePacket(entity, buf, client::execute);
        });

        ClientPlayNetworking.registerGlobalReceiver(AltarRecipeStartS2C.ID, (packet, player, responseSender) -> {
            World world = player.getWorld();
            final int density = 4;
            for (BlockPos pedestalPos : packet.pedestals()) {
                for (int i = 0; i < pedestalPos.getManhattanDistance(packet.pos()) * density; i++) {
                    Vec3d pos = pedestalPos.toCenterPos();
                    Vec3d offset = packet.pos().toCenterPos().subtract(pedestalPos.toCenterPos())
                      .normalize()
                      .multiply((double) i / density);
                    pos = pos.add(offset);
                    world.addParticle(
                      DustParticleEffect.DEFAULT,
                      pos.getX() + world.getRandom().nextGaussian() * 0.07,
                      pos.getY() + world.getRandom().nextGaussian() * 0.07,
                      pos.getZ() + world.getRandom().nextGaussian() * 0.07,
                      0,
                      0,
                      0
                                     );
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(HungryDecayedLogVFXS2C.ID, (packet, player, responseSender) -> {
            Entity entity = player.getWorld().getEntityById(packet.entityId());
            if (entity == null || entity.getBoundingBox() == null)
                return;

            Box boundingBox = entity.getBoundingBox();
            Random random = player.getRandom();
            for (int i = 0; i < 15; i++) {
                double x = boundingBox.minX + boundingBox.getXLength() * random.nextDouble();
                double y = boundingBox.minY + boundingBox.getYLength() * random.nextDouble();
                double z = boundingBox.minZ + boundingBox.getZLength() * random.nextDouble();
                player.getWorld().addParticle(DustParticleEffect.DEFAULT, x, y, z, 0, 0, 0);
            }
        });
    }

    public void registerBindings() {
        SUCK_BLOOD = KeyBindingHelper.registerKeyBinding(SUCK_BLOOD);
        OPEN_ABILITIES = KeyBindingHelper.registerKeyBinding(OPEN_ABILITIES);
        ABILITY_1 = KeyBindingHelper.registerKeyBinding(ABILITY_1);
        ABILITY_2 = KeyBindingHelper.registerKeyBinding(ABILITY_2);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_ABILITIES.wasPressed() && VampireHelper.isVampire(client.player)) {
                client.setScreen(new VampireAbilitiesScreen());
            }
            while (ABILITY_1.wasPressed()) {
                ClientPlayNetworking.send(new ActivateAbilityC2S(0));
            }
            while (ABILITY_2.wasPressed()) {
                ClientPlayNetworking.send(new ActivateAbilityC2S(1));
            }
            if (SUCK_BLOOD.isPressed()) {
                if (isLookingAtValidTarget() || !VampireHelper.getItemInHand(client.player, Hand.MAIN_HAND, BloodStorageItem.FILLABLE_ITEM_PREDICATE).isEmpty()) {
                    ClientPlayNetworking.send(new DrainBloodC2S(true));
                    drainingBlood = true;
                }
            } else if (drainingBlood) {
                drainingBlood = false;
                ClientPlayNetworking.send(new DrainBloodC2S(false));
            }
        });

    }

    public static boolean isLookingAtValidTarget() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!VampireHelper.isVampire(client.player))
            return false;

        HitResult result = client.crosshairTarget;
        LivingEntity target = result != null && result.getType() == HitResult.Type.ENTITY && ((EntityHitResult) result).getEntity() instanceof LivingEntity living ? living : null;

        return target != null && target.getType().isIn(BLTags.Entities.HAS_BLOOD);
    }
}
