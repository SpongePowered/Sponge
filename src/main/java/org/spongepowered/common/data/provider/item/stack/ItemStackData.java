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
import net.minecraft.block.Block;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.NestedTableEntry;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.accessor.item.ToolItemAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Set;

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
                            if (h.isFood()) {
                                final List<Pair<EffectInstance,Float>> itemEffects = h.getItem().getFood().getEffects();
                                final WeightedTable<PotionEffect> effects = new WeightedTable<>();
                                final ChanceTable<PotionEffect> chance = new ChanceTable<>();
                                for (Pair<EffectInstance,Float> effect : itemEffects) {
                                    chance.add((PotionEffect) effect.getKey(), effect.getValue());
                                }
                                effects.add(new NestedTableEntry<>(1, chance));
                                return effects;
                            }
                            return null;
                        })
                    .create(Keys.BURN_TIME)
                        .get(h -> {
                            final Integer burnTime = AbstractFurnaceTileEntity.getBurnTimes().get(h.getItem());
                            if (burnTime != null && burnTime > 0) {
                                return burnTime;
                            }
                            return null;
                        })
                    .create(Keys.CAN_HARVEST)
                        .get(h -> {
                            final Item item = h.getItem();
                            if (item instanceof ToolItemAccessor && !(item instanceof PickaxeItem)) {
                                final Set<Block> blocks = ((ToolItemAccessor) item).accessor$getEffectiveBlocks();
                                return ImmutableSet.copyOf((Set<BlockType>) (Object) blocks);
                            }

                            final Set<BlockType> blockTypes = Registry.BLOCK.stream()
                                    .filter(b -> item.canHarvestBlock(b.getDefaultState()))
                                    .map(BlockType.class::cast)
                                    .collect(ImmutableSet.toImmutableSet());
                            return blockTypes.isEmpty() ? null : blockTypes;
                        })
                    .create(Keys.CONTAINER_ITEM)
                        .get(h -> (ItemType) h.getItem().getContainerItem())
                    .create(Keys.DISPLAY_NAME)
                        .get(h -> {
                            if (h.getItem() == Items.WRITTEN_BOOK) {
                                final CompoundNBT tag = h.getTag();
                                if (tag != null) {
                                    final String title = tag.getString(Constants.Item.Book.ITEM_BOOK_TITLE);
                                    return SpongeAdventure.legacySection(title);
                                }
                            }
                            return SpongeAdventure.asAdventure(h.getDisplayName());
                        })
                        .set((h, v) -> {
                            if (h.getItem() == Items.WRITTEN_BOOK) {
                                final String legacy = SpongeAdventure.legacySection(v);
                                h.setTagInfo(Constants.Item.Book.ITEM_BOOK_TITLE, StringNBT.valueOf(legacy));
                            } else {
                                h.setDisplayName(SpongeAdventure.asVanilla(v));
                            }
                        })
                        .delete(h -> {
                            final CompoundNBT tag = h.getChildTag(Constants.Item.ITEM_DISPLAY);
                            if (tag != null) {
                                tag.remove(Constants.Item.ITEM_DISPLAY_NAME);
                            }
                        })
                    .create(Keys.IS_UNBREAKABLE)
                        .get(h -> {
                            final CompoundNBT tag = h.getTag();
                            if (tag == null || !tag.contains(Constants.Item.ITEM_UNBREAKABLE, Constants.NBT.TAG_BYTE)) {
                                return false;
                            }
                            return tag.getBoolean(Constants.Item.ITEM_UNBREAKABLE);
                        })
                        .set(ItemStackData::setIsUnbrekable)
                        .delete(h -> setIsUnbrekable(h, false))
                    .create(Keys.LORE)
                        .get(h -> {
                            final CompoundNBT tag = h.getTag();
                            if (tag == null || tag.contains(Constants.Item.ITEM_DISPLAY)) {
                                return null;
                            }

                            final ListNBT list = tag.getList(Constants.Item.ITEM_LORE, Constants.NBT.TAG_STRING);
                            return list.isEmpty() ? null : SpongeAdventure.json(list.stream().collect(NbtCollectors.toStringList()));
                        })
                        .set((h, v) -> {
                            if (v.isEmpty()) {
                                deleteLore(h);
                                return;
                            }
                            final ListNBT list = SpongeAdventure.listTagJson(v);
                            h.getOrCreateChildTag(Constants.Item.ITEM_DISPLAY).put(Constants.Item.ITEM_LORE, list);
                        })
                        .delete(ItemStackData::deleteLore)
                    .create(Keys.MAX_DURABILITY)
                        .get(h -> h.getItem().isDamageable() ? h.getItem().getMaxDamage() : null)
                        .supports(h -> h.getItem().isDamageable())
                    .create(Keys.REPLENISHED_FOOD)
                        .get(h -> {
                            if (h.getItem().isFood()) {
                                final Food food = h.getItem().getFood();
                                return food == null ? null : food.getHealing();
                            }
                            return null;
                        })
                        .supports(h -> h.getItem().isFood())
                    .create(Keys.REPLENISHED_SATURATION)
                        .get(h -> {
                            if (h.getItem().isFood()) {
                                final Food food = h.getItem().getFood();
                                if (food != null) {
                                    // Translate's Minecraft's weird internal value to the actual saturation value
                                    return food.getSaturation() * food.getHealing() * 2.0;
                                }
                            }
                            return null;
                        })
                    .supports(h -> h.getItem().isFood());
    }
    // @formatter:on

    private static void setIsUnbrekable(final ItemStack stack, final Boolean value) {
        if (value == null || (!value && !stack.hasTag())) {
            return;
        }
        final CompoundNBT tag = stack.getOrCreateTag();
        if (value) {
            tag.putBoolean(Constants.Item.ITEM_UNBREAKABLE, true);
        } else {
            tag.remove(Constants.Item.ITEM_UNBREAKABLE);
        }
    }

    private static void deleteLore(final ItemStack stack) {
        final CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains(Constants.Item.ITEM_DISPLAY)) {
            tag.getCompound(Constants.Item.ITEM_DISPLAY).remove(Constants.Item.ITEM_LORE);
        }
    }
}
