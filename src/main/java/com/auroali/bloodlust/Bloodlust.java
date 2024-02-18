package com.auroali.bloodlust;

import com.auroali.bloodlust.common.commands.BloodlustCommand;
import com.auroali.bloodlust.common.components.BLEntityComponents;
import com.auroali.bloodlust.common.components.VampireComponent;
import com.auroali.bloodlust.common.registry.BLItems;
import com.auroali.bloodlust.common.registry.BLTags;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bloodlust implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "bloodlust";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(BloodlustCommand.register()));

		ServerPlayNetworking.registerGlobalReceiver(BLResources.KEYBIND_CHANNEL, (server, player, handler, buf, responseSender) -> {
			boolean draining = buf.readBoolean();
			server.execute(() -> {
				VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(player);
				if(!vampire.isVampire())
					return;

				if(draining)
					vampire.tryStartSuckingBlood();
				else
					vampire.stopSuckingBlood();
			});
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(player);
			ItemStack stack = player.getStackInHand(hand);
			if(vampire.isVampire() && stack.isFood() && ! stack.isIn(BLTags.Items.VAMPIRE_FOOD))
				return TypedActionResult.fail(stack);

			return TypedActionResult.pass(stack);
		});

		BLItems.register();
	}
}