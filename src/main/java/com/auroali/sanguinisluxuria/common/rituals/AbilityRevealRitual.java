package com.auroali.sanguinisluxuria.common.rituals;

import com.auroali.sanguinisluxuria.common.abilities.VampireAbility;
import com.auroali.sanguinisluxuria.common.components.BLEntityComponents;
import com.auroali.sanguinisluxuria.common.components.VampireComponent;
import com.auroali.sanguinisluxuria.common.registry.BLRitualTypes;
import com.mojang.serialization.Codec;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AbilityRevealRitual implements Ritual, ItemCreatingRitual {
    public static final AbilityRevealRitual INSTANCE = new AbilityRevealRitual();
    public static final Codec<AbilityRevealRitual> CODEC = Codec.unit(() -> INSTANCE);
    public static final ItemStack OUTPUT = new ItemStack(Items.WRITTEN_BOOK);

    protected AbilityRevealRitual() {
    }

    @Override
    public ItemStack getOutput() {
        return OUTPUT;
    }

    @Override
    public void onCompleted(World world, LivingEntity initiator, BlockPos pos, Inventory inventory) {
        VampireComponent vampire = BLEntityComponents.VAMPIRE_COMPONENT.get(initiator);
        ItemStack outputStack = new ItemStack(Items.WRITTEN_BOOK);
        NbtCompound nbt = outputStack.getOrCreateNbt();
        nbt.putString(WrittenBookItem.AUTHOR_KEY, "Ritual of Revealing");
        nbt.putString(WrittenBookItem.TITLE_KEY, "Mutations");

        // generate the pages for the book
        List<Text> pages = new ArrayList<>();
        int lines = 0;
        for (VampireAbility ability : vampire.getAbilties()) {
            Text abilityText = Text.translatable(ability.getTranslationKey()).formatted(Formatting.DARK_RED, Formatting.BOLD, Formatting.ITALIC);
            if (pages.isEmpty()) {
                pages.add(abilityText);
                lines = 1;
                continue;
            }
            // get the current page
            Text page = pages.get(pages.size() - 1);
            // add the new text to a copy of the page
            Text newPage = Texts.join(List.of(page, abilityText), Text.of("\n"));
            // if the new page can't fit in the book, make a new page
            if (newPage.getString().length() > 1024 || lines > 13) {
                pages.add(abilityText);
                lines = 0;
                continue;
            }
            lines += 1 + abilityText.getString().length() / 114;
            pages.set(pages.size() - 1, newPage);
        }

        // add the pages to the book
        NbtList pagesNbt = new NbtList();
        pages.stream().map(Text.Serializer::toJson).map(NbtString::of).forEach(pagesNbt::add);
        outputStack.setSubNbt(WrittenBookItem.PAGES_KEY, pagesNbt);

        // spawn the entity
        Vec3d centerPos = pos.toCenterPos();
        ItemEntity entity = new ItemEntity(
          world,
          centerPos.getX(),
          centerPos.getY() + 1,
          centerPos.getZ(),
          outputStack);
        world.spawnEntity(entity);
    }

    @Override
    public RitualType<?> getType() {
        return BLRitualTypes.ABILITY_REVEAL_RITUAL_TYPE;
    }
}
