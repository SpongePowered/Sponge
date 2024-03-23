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

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.item.ItemRarity;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.NestedTableEntry;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                            if (h.has(DataComponents.FOOD)) {
                                final var itemEffects = h.get(DataComponents.FOOD).effects();
                                final WeightedTable<PotionEffect> effects = new WeightedTable<>();
                                final ChanceTable<PotionEffect> chance = new ChanceTable<>();
                                for (final var effect : itemEffects) {
                                    chance.add((PotionEffect) effect.effect(), effect.probability());
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
                            final Registry<Block> blockRegistry = SpongeCommon.vanillaRegistry(Registries.BLOCK);
                            final Tool tool = h.get(DataComponents.TOOL);
                            if (tool != null) {
                                return tool.rules().stream().map(Tool.Rule::blocks)
                                        .flatMap(HolderSet::stream)
                                        .map(Holder::value)
                                        .map(BlockType.class::cast)
                                        .collect(Collectors.toSet());
                            }

                            final Set<BlockType> blockTypes = blockRegistry.stream()
                                    .filter(b -> h.isCorrectToolForDrops(b.defaultBlockState()))
                                    .map(BlockType.class::cast)
                                    .collect(Collectors.toUnmodifiableSet());
                            return blockTypes.isEmpty() ? null : blockTypes;
                        })
                    .create(Keys.CONTAINER_ITEM)
                        .get(h -> (ItemType) h.getItem().getCraftingRemainingItem())
                    .create(Keys.DISPLAY_NAME)
                        .get(h -> SpongeAdventure.asAdventure(h.getDisplayName()))
                    .create(Keys.CUSTOM_MODEL_DATA)
                        .get(h -> h.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.DEFAULT).value())
                        .set((h, v) -> h.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(v)))
                        .delete(h -> h.remove(DataComponents.CUSTOM_MODEL_DATA))
                    .create(Keys.CUSTOM_NAME)
                        .get(h -> {
                            if (h.has(DataComponents.CUSTOM_NAME)) {
                                return SpongeAdventure.asAdventure(h.getHoverName());
                            }
                            if (h.getItem() == Items.WRITTEN_BOOK) {
                                // When no custom name is set on a written book fallback to its title
                                // The custom name has a higher priority than the title so no setter is needed.
                                var bookContent = h.get(DataComponents.WRITTEN_BOOK_CONTENT);
                                if (bookContent != null) {
                                    final String rawTitle = bookContent.title().raw();
                                    if (!StringUtil.isBlank(rawTitle)) {
                                        return LegacyComponentSerializer.legacySection().deserialize(rawTitle);
                                    }
                                }
                            }
                            return null;
                        })
                        .set((h, v) -> h.set(DataComponents.CUSTOM_NAME, SpongeAdventure.asVanilla(v)))
                        .delete(h -> h.remove(DataComponents.CUSTOM_NAME))
                    .create(Keys.IS_UNBREAKABLE)
                        .get(h -> h.has(DataComponents.UNBREAKABLE))
                        .set(ItemStackData::setIsUnbrekable)
                        .delete(h -> ItemStackData.setIsUnbrekable(h, false))
                    .create(Keys.LORE)
                        .get(h -> {
                            final List<Component> lines = h.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines();
                            if (lines.isEmpty()) {
                                return null;
                            }
                            return lines.stream().map(SpongeAdventure::asAdventure).toList();
                        })
                        .set((h, v) -> {
                            if (v.isEmpty()) {
                                h.remove(DataComponents.LORE);
                                return;
                            }
                            h.set(DataComponents.LORE, new ItemLore(v.stream().map(SpongeAdventure::asVanilla).toList()));
                        })
                        .delete(h -> h.remove(DataComponents.LORE))
                    .create(Keys.MAX_DURABILITY)
                        .get(h -> h.getMaxDamage() != 0 ? h.getMaxDamage() : null)
                        .supports(h -> h.getMaxDamage() != 0)
                    .create(Keys.ITEM_DURABILITY)
                        .get(stack -> stack.getMaxDamage() - stack.getDamageValue())
                        .set((stack, durability) -> stack.setDamageValue(stack.getMaxDamage() - durability))
                        .supports(h -> h.getMaxDamage() != 0)
                    .create(Keys.ITEM_RARITY)
                        .get(stack -> (ItemRarity) (Object) stack.getRarity())
                    .create(Keys.REPLENISHED_FOOD)
                        .get(h -> {
                            final var food = h.get(DataComponents.FOOD);
                            if (food != null) {
                                return food.nutrition();
                            }
                            return null;
                        })
                    .create(Keys.REPLENISHED_SATURATION)
                        .get(h -> {
                            final var food = h.get(DataComponents.FOOD);
                            if (food != null) {
                                // Translate's Minecraft's weird internal value to the actual saturation value
                                return food.saturationModifier() * food.nutrition() * 2.0;
                            }
                            return null;
                        })
                    ;
    }
    // @formatter:on

    private static void setIsUnbrekable(final ItemStack stack, final Boolean value) {
        if (value) {
            stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        } else {
            stack.remove(DataComponents.UNBREAKABLE);
        }
    }

}
