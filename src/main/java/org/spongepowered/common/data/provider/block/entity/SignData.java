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
package org.spongepowered.common.data.provider.block.entity;

import net.kyori.adventure.text.Component;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.accessor.world.level.block.entity.SignBlockEntityAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.DirectionUtil;
import org.spongepowered.common.util.RotationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SignData {

    private SignData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(SignBlockEntity.class)
                    .create(Keys.SIGN_LINES)
                        .get(SignData::getSignLines)
                        .set(SignData::setSignLines)
                        .delete(h -> SignData.setSignLines(h, Collections.emptyList()))
                    .create(Keys.DIRECTION)
                        .get(h -> {
                            if (h.getBlockState().getBlock() instanceof StandingSignBlock) {
                                return RotationUtils.getFor(h.getBlockState().getValue(StandingSignBlock.ROTATION));
                            } else if (h.getBlockState().getBlock() instanceof WallSignBlock) {
                                return DirectionUtil.getFor(h.getBlockState().getValue(WallSignBlock.FACING));
                            }
                            return null;
                        })
                        .setAnd((h, v) -> {
                            if (h.getBlockState().getBlock() instanceof StandingSignBlock) {
                                h.getLevel().setBlockAndUpdate(h.getBlockPos(), RotationUtils.set(h.getBlockState(), v, StandingSignBlock.ROTATION));
                                return true;
                            } else if (h.getBlockState().getBlock() instanceof WallSignBlock) {
                                h.getLevel().setBlockAndUpdate(h.getBlockPos(), DirectionUtil.set(h.getBlockState(), v, WallSignBlock.FACING));
                                return true;
                            }

                            return false;
                        })
                        .supports(h -> h.getLevel() != null)
                    .create(Keys.GLOWING_TEXT)
                        .get(SignBlockEntity::hasGlowingText)
                        .set(SignBlockEntity::setHasGlowingText)
                .asMutable(ServerLocation.class)
                    .create(Keys.SIGN_LINES)
                        .get(SignData::getSignLines)
                        .set(SignData::setSignLines)
                        .delete(h -> SignData.setSignLines(h, Collections.emptyList()))
                        .supports(loc -> loc.blockEntity().map(b -> b instanceof SignBlockEntity).orElse(false));
    }
    // @formatter:on

    private static SignBlockEntity toSignTileEntity(final ServerLocation holder) {
        return (SignBlockEntity) holder.blockEntity().get();
    }

    private static void setSignLines(final ServerLocation holder, final List<Component> value) {
        SignData.setSignLines(SignData.toSignTileEntity(holder), value);
    }

    private static void setSignLines(final SignBlockEntity holder, final List<Component> value) {
        final SignBlockEntityAccessor accessor = (SignBlockEntityAccessor) holder;
        for (int i = 0; i < accessor.accessor$messages().length; i++) {
            accessor.accessor$messages()[i] = SpongeAdventure.asVanilla(i > value.size() - 1 ? Component.empty() : value.get(i));
        }
        holder.setChanged();
        holder.getLevel().sendBlockUpdated(holder.getBlockPos(), holder.getBlockState(), holder.getBlockState(), 3);
    }

    private static List<Component> getSignLines(ServerLocation h) {
        return SignData.getSignLines(SignData.toSignTileEntity(h));
    }

    private static List<Component> getSignLines(SignBlockEntity h) {
        final SignBlockEntityAccessor accessor = (SignBlockEntityAccessor) h;
        final List<Component> lines = new ArrayList<>(accessor.accessor$messages().length);
        for (int i = 0; i < accessor.accessor$messages().length; i++) {
            lines.add(SpongeAdventure.asAdventure(accessor.accessor$messages()[i]));
        }
        return lines;
    }
}
