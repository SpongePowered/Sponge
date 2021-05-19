/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.provider.item.stack;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.item.ItemRarity;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.NestedTableEntry;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.accessor.world.item.DiggerItemAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.NBTCollectors;

import java.util.List;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public final class ItemStackData {

    private ItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.APPLICABLE_POTION_EFFECTS)
                        .get(h -> {
                            if (h.isEdible()) {
                                final List<Pair<MobEffectInstance,Float>> itemEffects = h.getItem().getFoodProperties().getEffects();
                                final WeightedTable<PotionEffect> effects = new WeightedTable<>();
                                final ChanceTable<PotionEffect> chance = new ChanceTable<>();
                                for (final Pair<MobEffectInstance,Float> effect : itemEffects) {
                                    chance.add((PotionEffect) effect.getFirst(), effect.getSecond());
                                }
                                effects.add(new NestedTableEntry<>(1, chance));
                                return effects;
                            }
                            return null;
                        })
                    .create(Keys.BURN_TIME)
                        .get(h -> {
                            final Integer burnTime = AbstractFurnaceBlockEntity.getFuel().get(h.getItem());
                            if (burnTime != null && burnTime > 0) {
                                return burnTime;
                            }
                            return null;
                        })
                    .create(Keys.CAN_HARVEST)
                        .get(h -> {
                            final Item item = h.getItem();
                            if (item instanceof DiggerItemAccessor && !(item instanceof PickaxeItem)) {
                                final Set<Block> blocks = ((DiggerItemAccessor) item).accessor$blocks();
                                return ImmutableSet.copyOf((Set<BlockType>) (Object) blocks);
                            }

                            final Set<BlockType> blockTypes = Registry.BLOCK.stream()
                                    .filter(b -> item.isCorrectToolForDrops(b.defaultBlockState()))
                                    .map(BlockType.class::cast)
                                    .collect(ImmutableSet.toImmutableSet());
                            return blockTypes.isEmpty() ? null : blockTypes;
                        })
                    .create(Keys.CONTAINER_ITEM)
                        .get(h -> (ItemType) h.getItem().getCraftingRemainingItem())
                    .create(Keys.DISPLAY_NAME)
                        .get(h -> SpongeAdventure.asAdventure(h.getDisplayName()))
                    .create(Keys.CUSTOM_MODEL_DATA)
                        .get(h -> {
                            final CompoundTag tag = h.getTag();
                            if (tag == null || !tag.contains(Constants.Item.CUSTOM_MODEL_DATA, Constants.NBT.TAG_INT)) {
                                return null;
                            }
                            return tag.getInt(Constants.Item.CUSTOM_MODEL_DATA);
                        })
                        .set((h, v) -> {
                            final CompoundTag tag = h.getOrCreateTag();
                            tag.putInt(Constants.Item.CUSTOM_MODEL_DATA, v);
                        })
                        .delete(h -> {
                            final CompoundTag tag = h.getTag();
                            if (tag != null) {
                                tag.remove(Constants.Item.CUSTOM_MODEL_DATA);
                            }
                        })
                    .create(Keys.CUSTOM_NAME)
                        .get(h -> {
                            if (h.hasCustomHoverName()) {
                                return SpongeAdventure.asAdventure(h.getHoverName());
                            }
                            if (h.getItem() == Items.WRITTEN_BOOK) {
                                // When no custom name is set on a written book fallback to its title
                                // The custom name has a higher priority than the title so no setter is needed.
                                final CompoundTag tag = h.getTag();
                                if (tag != null) {
                                    final String title = tag.getString(Constants.Item.Book.ITEM_BOOK_TITLE);
                                    return LegacyComponentSerializer.legacySection().deserialize(title);
                                }
                            }
                            return null;
                        })
                        .set((h, v) -> h.setHoverName(SpongeAdventure.asVanilla(v)))
                        .delete(ItemStack::resetHoverName)
                    .create(Keys.IS_UNBREAKABLE)
                        .get(h -> {
                            final CompoundTag tag = h.getTag();
                            if (tag == null || !tag.contains(Constants.Item.ITEM_UNBREAKABLE, Constants.NBT.TAG_BYTE)) {
                                return false;
                            }
                            return tag.getBoolean(Constants.Item.ITEM_UNBREAKABLE);
                        })
                        .set(ItemStackData::setIsUnbrekable)
                        .delete(h -> ItemStackData.setIsUnbrekable(h, false))
                    .create(Keys.LORE)
                        .get(h -> {
                            final CompoundTag tag = h.getTag();
                            if (tag == null || tag.contains(Constants.Item.ITEM_DISPLAY)) {
                                return null;
                            }

                            final ListTag list = tag.getList(Constants.Item.ITEM_LORE, Constants.NBT.TAG_STRING);
                            return list.isEmpty() ? null : SpongeAdventure.json(list.stream().collect(NBTCollectors.toStringList()));
                        })
                        .set((h, v) -> {
                            if (v.isEmpty()) {
                                ItemStackData.deleteLore(h);
                                return;
                            }
                            final ListTag list = SpongeAdventure.listTagJson(v);
                            h.getOrCreateTagElement(Constants.Item.ITEM_DISPLAY).put(Constants.Item.ITEM_LORE, list);
                        })
                        .delete(ItemStackData::deleteLore)
                    .create(Keys.MAX_DURABILITY)
                        .get(h -> h.getItem().canBeDepleted() ? h.getItem().getMaxDamage() : null)
                        .supports(h -> h.getItem().canBeDepleted())
                    .create(Keys.ITEM_DURABILITY)
                        .get(stack -> stack.getMaxDamage() - stack.getDamageValue())
                        .set((stack, durability) -> stack.setDamageValue(stack.getMaxDamage() - durability))
                        .supports(h -> h.getItem().canBeDepleted())
                    .create(Keys.ITEM_RARITY)
                        .get(stack -> (ItemRarity) (Object) stack.getRarity())
                    .create(Keys.REPLENISHED_FOOD)
                        .get(h -> {
                            if (h.getItem().isEdible()) {
                                final FoodProperties food = h.getItem().getFoodProperties();
                                return food == null ? null : food.getNutrition();
                            }
                            return null;
                        })
                        .supports(h -> h.getItem().isEdible())
                    .create(Keys.REPLENISHED_SATURATION)
                        .get(h -> {
                            if (h.getItem().isEdible()) {
                                final FoodProperties food = h.getItem().getFoodProperties();
                                if (food != null) {
                                    // Translate's Minecraft's weird internal value to the actual saturation value
                                    return food.getSaturationModifier() * food.getNutrition() * 2.0;
                                }
                            }
                            return null;
                        })
                    .supports(h -> h.getItem().isEdible());
    }
    // @formatter:on

    private static void setIsUnbrekable(final ItemStack stack, final Boolean value) {
        if (value == null || (!value && !stack.hasTag())) {
            return;
        }
        final CompoundTag tag = stack.getOrCreateTag();
        if (value) {
            tag.putBoolean(Constants.Item.ITEM_UNBREAKABLE, true);
        } else {
            tag.remove(Constants.Item.ITEM_UNBREAKABLE);
        }
    }

    private static void deleteLore(final ItemStack stack) {
        final CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(Constants.Item.ITEM_DISPLAY)) {
            tag.getCompound(Constants.Item.ITEM_DISPLAY).remove(Constants.Item.ITEM_LORE);
        }
    }
}
