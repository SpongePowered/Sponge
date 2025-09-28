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

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.ShieldDamageReduction;
import org.spongepowered.api.data.type.ShieldItemDamageFunction;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.common.bridge.tags.TagBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public final class ShieldItemStackData {

    private ShieldItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.DYE_COLOR)
                        .get(h -> (DyeColor) (Object) h.getOrDefault(DataComponents.BASE_COLOR, net.minecraft.world.item.DyeColor.WHITE))
                        .set((h, v) -> h.set(DataComponents.BASE_COLOR, (net.minecraft.world.item.DyeColor) (Object) v))
                    .create(Keys.BANNER_PATTERN_LAYERS)
                        .get(h -> h.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers()
                                .stream().map(BannerPatternLayer.class::cast).toList())
                        .set((h, v) -> {
                            h.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(v.stream().map(BannerPatternLayers.Layer.class::cast).toList()));
                            // TODO check setting banner base? Constants.TileEntity.Banner.BANNER_BASE / BannerPatternShapes.BASE
                        })
                    .create(Keys.SHIELD_DEPLOY_TICKS)
                        .get(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.get(DataComponents.BLOCKS_ATTACKS);
                            if (blocksAttacks == null) {
                                return null;
                            }
                            return Ticks.of((long) (Constants.TickConversions.TICKS_PER_SECOND * blocksAttacks.blockDelaySeconds()));
                        })
                        .set((h, v) -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                v.ticks() / (float) Constants.TickConversions.TICKS_PER_SECOND,
                                blocksAttacks.disableCooldownScale(),
                                blocksAttacks.damageReductions(),
                                blocksAttacks.itemDamage(),
                                blocksAttacks.bypassedBy(),
                                blocksAttacks.blockSound(),
                                blocksAttacks.disableSound()
                            ));
                        })
                    .create(Keys.DISABLE_SHIELD_TICKS_SCALE)
                        .get(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.get(DataComponents.BLOCKS_ATTACKS);
                            if (blocksAttacks == null) {
                                return null;
                            }
                            return (double) blocksAttacks.disableCooldownScale();
                        })
                        .set((h, v) -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                blocksAttacks.blockDelaySeconds(),
                                v.floatValue(),
                                blocksAttacks.damageReductions(),
                                blocksAttacks.itemDamage(),
                                blocksAttacks.bypassedBy(),
                                blocksAttacks.blockSound(),
                                blocksAttacks.disableSound()
                            ));
                        })
                    .create(Keys.SHIELD_DAMAGE_REDUCTIONS)
                        .get(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.get(DataComponents.BLOCKS_ATTACKS);
                            if (blocksAttacks == null) {
                                return null;
                            }
                            return (List<ShieldDamageReduction<?>>) (Object) List.copyOf(blocksAttacks.damageReductions());
                        })
                        .set((h, v) -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                blocksAttacks.blockDelaySeconds(),
                                blocksAttacks.disableCooldownScale(),
                                (List<BlocksAttacks.DamageReduction>) (Object) List.copyOf(v),
                                blocksAttacks.itemDamage(),
                                blocksAttacks.bypassedBy(),
                                blocksAttacks.blockSound(),
                                blocksAttacks.disableSound()
                            ));
                        })
                    .create(Keys.SHIELD_ITEM_DAMAGE_FUNCTION)
                        .get(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.get(DataComponents.BLOCKS_ATTACKS);
                            if (blocksAttacks == null) {
                                return null;
                            }
                            return (ShieldItemDamageFunction<?>) (Object) blocksAttacks.itemDamage();
                        })
                        .set((h, v) -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                blocksAttacks.blockDelaySeconds(),
                                blocksAttacks.disableCooldownScale(),
                                blocksAttacks.damageReductions(),
                                (BlocksAttacks.ItemDamageFunction) (Object) v,
                                blocksAttacks.bypassedBy(),
                                blocksAttacks.blockSound(),
                                blocksAttacks.disableSound()
                            ));
                        })
                    .create(Keys.BYPASS_DAMAGE_TAG)
                        .get(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.get(DataComponents.BLOCKS_ATTACKS);
                            if (blocksAttacks == null || blocksAttacks.bypassedBy().isEmpty()) {
                                return null;
                            }
                            return (Tag<DamageType>) (Object) blocksAttacks.bypassedBy().get();
                        })
                        .set((h, v) -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                blocksAttacks.blockDelaySeconds(),
                                blocksAttacks.disableCooldownScale(),
                                blocksAttacks.damageReductions(),
                                blocksAttacks.itemDamage(),
                                Optional.of(((TagBridge<net.minecraft.world.damagesource.DamageType>) v).bridge$asVanillaTag()),
                                blocksAttacks.blockSound(),
                                blocksAttacks.disableSound()
                            ));
                        })
                        .delete(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                blocksAttacks.blockDelaySeconds(),
                                blocksAttacks.disableCooldownScale(),
                                blocksAttacks.damageReductions(),
                                blocksAttacks.itemDamage(),
                                Optional.empty(),
                                blocksAttacks.blockSound(),
                                blocksAttacks.disableSound()
                            ));
                        })
                    .create(Keys.SHIELD_BLOCK_SOUND)
                        .get(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.get(DataComponents.BLOCKS_ATTACKS);
                            if (blocksAttacks == null || blocksAttacks.blockSound().isEmpty()) {
                                return null;
                            }
                            return (SoundType) (Object) blocksAttacks.blockSound().get().value();
                        })
                        .set((h, v) -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                blocksAttacks.blockDelaySeconds(),
                                blocksAttacks.disableCooldownScale(),
                                blocksAttacks.damageReductions(),
                                blocksAttacks.itemDamage(),
                                blocksAttacks.bypassedBy(),
                                Optional.of(Holder.direct((SoundEvent) (Object) v)),
                                blocksAttacks.disableSound()
                            ));
                        })
                        .delete(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                blocksAttacks.blockDelaySeconds(),
                                blocksAttacks.disableCooldownScale(),
                                blocksAttacks.damageReductions(),
                                blocksAttacks.itemDamage(),
                                blocksAttacks.bypassedBy(),
                                Optional.empty(),
                                blocksAttacks.disableSound()
                            ));
                        })
                    .create(Keys.SHIELD_DISABLE_SOUND)
                        .get(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.get(DataComponents.BLOCKS_ATTACKS);
                            if (blocksAttacks == null || blocksAttacks.disableSound().isEmpty()) {
                                return null;
                            }
                            return (SoundType) (Object) blocksAttacks.disableSound().get().value();
                        })
                        .set((h, v) -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                blocksAttacks.blockDelaySeconds(),
                                blocksAttacks.disableCooldownScale(),
                                blocksAttacks.damageReductions(),
                                blocksAttacks.itemDamage(),
                                blocksAttacks.bypassedBy(),
                                blocksAttacks.blockSound(),
                                Optional.of(Holder.direct((SoundEvent) (Object) v))
                            ));
                        })
                        .delete(h -> {
                            final @Nullable BlocksAttacks blocksAttacks = h.getOrDefault(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS_DEFAULTS);
                            h.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                                blocksAttacks.blockDelaySeconds(),
                                blocksAttacks.disableCooldownScale(),
                                blocksAttacks.damageReductions(),
                                blocksAttacks.itemDamage(),
                                blocksAttacks.bypassedBy(),
                                blocksAttacks.blockSound(),
                                Optional.empty()
                            ));
                        })
        ;
    }
    // @formatter:on

    private static final BlocksAttacks BLOCKS_ATTACKS_DEFAULTS = new BlocksAttacks(
        0,
        1,
        List.of(),
        BlocksAttacks.ItemDamageFunction.DEFAULT,
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );

}
