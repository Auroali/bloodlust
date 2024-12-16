package com.auroali.sanguinisluxuria;

import com.auroali.sanguinisluxuria.common.abilities.VampireAbilityContainer;
import com.auroali.sanguinisluxuria.common.blockentities.AltarBlockEntity;
import com.auroali.sanguinisluxuria.common.blockentities.PedestalBlockEntity;
import com.auroali.sanguinisluxuria.common.commands.BloodlustCommand;
import com.auroali.sanguinisluxuria.common.commands.arguments.VampireAbilityArgument;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.BloodComponent;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.items.BloodStorageItem;
import com.auroali.sanguinisluxuria.common.network.ActivateAbilityC2S;
import com.auroali.sanguinisluxuria.common.network.DrainBloodC2S;
import com.auroali.sanguinisluxuria.common.registry.*;
import com.auroali.sanguinisluxuria.config.BLConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.CauldronFluidContent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bloodlust implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MODID = "sanguinisluxuria";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        BLRegistries.init();
        BLConfig.INSTANCE.load();

        BLEntityAttributes.register();
        BLCauldronBehaviours.register();
        BLFluids.register();
        BLBlocks.register();
        BLBlockEntities.register();
        BLRecipeSerializers.register();
        BLRecipeTypes.register();
        BLItems.register();
        BLItemGroups.register();
        BLSounds.register();
        BLStatusEffects.register();
        BLVampireAbilities.register();
        BLAdvancementCriterion.register();
        BLEnchantments.register();
        BLEntities.register();
        BLWorldgen.register();
        BLParticles.register();
        BLRitualTypes.register();

        ArgumentTypeRegistry.registerArgumentType(
          BLResources.VAMPIRE_ABILITY_ARGUMENT_ID,
          VampireAbilityArgument.class,
          ConstantArgumentSerializer.of(VampireAbilityArgument::argument)
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(BloodlustCommand.register()));

        registerNetworkHandlers();

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (VampireHelper.isVampire(newPlayer)) {
                VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(newPlayer);
                vampire.setDowned(false);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register(Bloodlust::dropBlood);

        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, pos, vanilla) -> {
            if (VampireHelper.isVampire(player)) {
                return vanilla ? ActionResult.FAIL : ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CLERIC, 3, BLTradeOffers::registerClericTrades);

        ItemStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (blockEntity instanceof PedestalBlockEntity e)
                return InventoryStorage.of(e.getInventory(), null);
            if (blockEntity instanceof AltarBlockEntity e)
                return InventoryStorage.of(e, null);
            return null;
        }, BLBlockEntities.PEDESTAL);

        CauldronFluidContent.registerCauldron(BLBlocks.BLOOD_CAULDRON, BLFluids.BLOOD, FluidConstants.BOTTLE, LeveledCauldronBlock.LEVEL);

        FluidStorage.combinedItemApiProvider(BLItems.BLOOD_BOTTLE).register(ctx -> new BloodStorageItem.FluidStorage(ctx, BLItems.BLOOD_BOTTLE));
        FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register(ctx -> new BloodStorageItem.FluidStorage(ctx, BLItems.BLOOD_BOTTLE));
        FluidStorage.combinedItemApiProvider(BLItems.BLOOD_BAG).register(ctx -> new BloodStorageItem.FluidStorage(ctx, BLItems.BLOOD_BAG));

        FluidVariantAttributes.register(BLFluids.BLOOD, BLFluids.BLOOD_ATTRIBUTE_HANDLER);
    }

    private static void dropBlood(LivingEntity entity, DamageSource source) {
        if (!VampireHelper.isVampire(entity)
          && entity.getType().isIn(BLTags.Entities.HAS_BLOOD)
          && (entity.getType().isIn(BLTags.Entities.CAN_DROP_BLOOD) || entity.hasStatusEffect(BLStatusEffects.BLEEDING))
        ) {
            BloodComponent blood = BLEntityComponents.BLOOD_COMPONENT.get(entity);
            if (blood.getBlood() < blood.getMaxBlood())
                return;

            BlockState state = entity.getWorld().getBlockState(entity.getBlockPos());
            BlockState belowState = entity.getWorld().getBlockState(entity.getBlockPos().down());
            if (tryFillCauldron(entity.getWorld(), entity.getBlockPos(), state) || tryFillCauldron(entity.getWorld(), entity.getBlockPos().down(), belowState))
                return;

            BlockState newState = BLBlocks.BLOOD_SPLATTER.getDefaultState();
            if (!state.canReplace(new AutomaticItemPlacementContext(entity.getWorld(), entity.getBlockPos(), Direction.DOWN, ItemStack.EMPTY, Direction.UP)) || !newState.canPlaceAt(entity.getWorld(), entity.getBlockPos()))
                return;

            entity.getWorld().setBlockState(entity.getBlockPos(), newState);
        }
    }

    private static boolean tryFillCauldron(World world, BlockPos pos, BlockState state) {
        if (state.isOf(BLBlocks.BLOOD_CAULDRON) && state.get(LeveledCauldronBlock.LEVEL) < LeveledCauldronBlock.MAX_LEVEL) {
            world.setBlockState(pos, state.with(LeveledCauldronBlock.LEVEL, state.get(LeveledCauldronBlock.LEVEL) + 1));
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        if (state.isOf(Blocks.CAULDRON)) {
            world.setBlockState(pos, BLBlocks.BLOOD_CAULDRON.getDefaultState());
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }


    public static void registerNetworkHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(ActivateAbilityC2S.ID, (packet, player, responseSender) -> {
            if (!VampireHelper.isVampire(player))
                return;
            VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(player);
            VampireAbilityContainer container = vampire.getAbilties();
            if (container.hasAbility(packet.ability()))
                packet.ability().activate(player, vampire);
        });
        ServerPlayNetworking.registerGlobalReceiver(DrainBloodC2S.ID, (packet, player, responseSender) -> {
            if (!VampireHelper.isVampire(player))
                return;
            VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(player);
            if (packet.draining())
                vampire.tryStartSuckingBlood();
            else
                vampire.stopSuckingBlood();
        });
    }
}