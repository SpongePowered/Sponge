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
package org.spongepowered.common.data.processor.common;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SkullTileEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedPlayerData;
import org.spongepowered.common.data.type.SpongeSkullType;
import org.spongepowered.common.util.Constants;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

public class SkullUtils {

    public static boolean supportsObject(final Object object) {
        return object instanceof SkullTileEntity || isValidItemStack(object);
    }

    public static SkullType getSkullType(final int skullType) {
        for (final SkullType type : SpongeImpl.getRegistry().getAllOf(SkullType.class)){
            if (type instanceof SpongeSkullType && ((SpongeSkullType) type).getByteId() == skullType) {
                return type;
            }
        }
        return Constants.TileEntity.Skull.DEFAULT_TYPE;
    }

    public static boolean isValidItemStack(final Object container) {
        return container instanceof ItemStack && ((ItemStack) container).func_77973_b().equals(Items.field_151144_bL);
    }

    public static boolean setProfile(final SkullTileEntity tileEntitySkull, @Nullable final GameProfile profile) {
        if (SkullUtils.getSkullType(tileEntitySkull.func_145904_a()).equals(SkullTypes.PLAYER)) {
            final GameProfile newProfile = SpongeRepresentedPlayerData.NULL_PROFILE.equals(profile) ? null : resolveProfileIfNecessary(profile);
            tileEntitySkull.func_152106_a((com.mojang.authlib.GameProfile) newProfile);
            tileEntitySkull.func_70296_d();
            tileEntitySkull.func_145831_w().func_184138_a(tileEntitySkull.func_174877_v(), tileEntitySkull.func_145831_w().func_180495_p(tileEntitySkull.func_174877_v()), tileEntitySkull.func_145831_w()
                    .func_180495_p(tileEntitySkull.func_174877_v()), 3);
            return true;
        }
        return false;
    }

    public static boolean setProfile(final ItemStack skull, @Nullable final GameProfile profile) {
        if (isValidItemStack(skull) && SkullUtils.getSkullType(skull.func_77960_j()).equals(SkullTypes.PLAYER)) {
            if (profile == null || profile.equals(SpongeRepresentedPlayerData.NULL_PROFILE)) {
                if (skull.func_77978_p() != null) {
                    skull.func_77978_p().func_82580_o(Constants.Item.Skull.ITEM_SKULL_OWNER);
                }
            } else {
                final CompoundNBT nbt = new CompoundNBT();
                NBTUtil.func_180708_a(nbt, (com.mojang.authlib.GameProfile) resolveProfileIfNecessary(profile));
                skull.func_77983_a(Constants.Item.Skull.ITEM_SKULL_OWNER, nbt);
            }
            return true;
        }
        return false;
    }

    private static @Nullable GameProfile resolveProfileIfNecessary(@Nullable final GameProfile profile) {
        if (profile == null) {
            return null;
        }
        if (profile.getPropertyMap().containsKey("textures")) {
            return profile;
        }
        // Skulls need a name in order to properly display -> resolve if no name is contained in the given profile
        final CompletableFuture<GameProfile> future = Sponge.getGame().getServer().getGameProfileManager().fill(profile);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            SpongeImpl.getLogger().debug("Exception while trying to fill skull GameProfile for '" + profile + "'", e);
            return profile;
        }
    }

}
