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

import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.Optional;
import org.spongepowered.common.util.VecHelper;

final class CompassItemData {
    private CompassItemData() {
    }

    static void register(final DataProviderRegistrator registrator) {
        registrator
            .asMutable(ItemStack.class)
                .create(Keys.LODESTONE)
                    .get(stack -> {
                        if (CompassItem.isLodestoneCompass(stack)) {
                            final CompoundNBT tag = stack.getOrCreateTag();
                            final Optional<RegistryKey<World>> dimension = CompassItem.getLodestoneDimension(tag);
                            if (dimension.isPresent()) {
                                return ServerLocation.of(
                                    (ServerWorld) SpongeCommon.getServer().getLevel(dimension.get()),
                                    VecHelper.toVector3d(NBTUtil.readBlockPos(tag.getCompound("LodestonePos")))
                                );
                            }
                        }
                        return null;
                    })
                    .set((stack, location) -> {
                        final CompoundNBT tag = stack.getOrCreateTag();
                        tag.put("LodestonePos", NBTUtil.writeBlockPos(VecHelper.toBlockPos(location)));
                        World.RESOURCE_KEY_CODEC.encodeStart(NBTDynamicOps.INSTANCE, ((net.minecraft.world.server.ServerWorld) location.getWorld()).dimension())
                            .resultOrPartial(SpongeCommon.getLogger()::error).ifPresent(dimension -> tag.put("LodestoneDimension", dimension));
                        tag.putBoolean("LodestoneTracked", true);
                    })
                    .delete(stack -> {
                        final CompoundNBT tag = stack.getTag();
                        if (tag != null) {
                            tag.remove("LodestoneDimension");
                            tag.remove("LodestonePos");
                            tag.remove("LodestoneTracked");
                        }
                    });
    }
}
