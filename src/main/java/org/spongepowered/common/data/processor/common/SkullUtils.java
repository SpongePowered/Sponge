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

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedPlayerData;
import org.spongepowered.common.data.type.SpongeSkullType;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntitySkull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

public class SkullUtils {

    /**
     * There's not really a meaningful default value for this, since it's a CatalogType. However, the Vanilla give command defaults the skeleton type (index 0), so it's used as the default here.
     */
    public static final SkullType DEFAULT_TYPE = SkullTypes.SKELETON;

    public static boolean supportsObject(Object object) {
        return object instanceof TileEntitySkull || isValidItemStack(object);
    }

    public static SkullType getSkullType(int skullType) {
        return SpongeImpl.getRegistry().getAllOf(SkullType.class).stream()
                .filter(type -> type instanceof SpongeSkullType && ((SpongeSkullType) type).getByteId() == skullType)
                .findAny().orElse(DEFAULT_TYPE);
    }

    public static boolean isValidItemStack(Object container) {
        return container instanceof ItemStack && ((ItemStack) container).getItem().equals(Items.SKULL);
    }

    public static SkullType getSkullType(TileEntitySkull tileEntitySkull) {
        return SkullUtils.getSkullType(tileEntitySkull.getSkullType());
    }

    public static void setSkullType(TileEntitySkull tileEntitySkull, int skullType) {
        tileEntitySkull.setType(skullType);
        tileEntitySkull.markDirty();
        tileEntitySkull.getWorld().notifyBlockUpdate(tileEntitySkull.getPos(), tileEntitySkull.getWorld().getBlockState(tileEntitySkull.getPos()), tileEntitySkull.getWorld()
                .getBlockState(tileEntitySkull.getPos()), 3);
    }

    public static SkullType getSkullType(ItemStack itemStack) {
        return SkullUtils.getSkullType(itemStack.getMetadata());
    }

    public static Optional<GameProfile> getProfile(TileEntitySkull entity) {
        return Optional.ofNullable((GameProfile) entity.getPlayerProfile());
    }

    public static boolean setProfile(TileEntitySkull tileEntitySkull, @Nullable GameProfile profile) {
        if (getSkullType(tileEntitySkull).equals(SkullTypes.PLAYER)) {
            final GameProfile newProfile = SpongeRepresentedPlayerData.NULL_PROFILE.equals(profile) ? null : resolveProfileIfNecessary(profile);
            tileEntitySkull.setPlayerProfile((com.mojang.authlib.GameProfile) newProfile);
            tileEntitySkull.markDirty();
            tileEntitySkull.getWorld().notifyBlockUpdate(tileEntitySkull.getPos(), tileEntitySkull.getWorld().getBlockState(tileEntitySkull.getPos()), tileEntitySkull.getWorld()
                    .getBlockState(tileEntitySkull.getPos()), 3);
            return true;
        }
        return false;
    }

    public static Optional<GameProfile> getProfile(ItemStack skull) {
        if (isValidItemStack(skull) && getSkullType(skull).equals(SkullTypes.PLAYER)) {
            final NBTTagCompound nbt = skull.getSubCompound(NbtDataUtil.ITEM_SKULL_OWNER);
            final com.mojang.authlib.GameProfile mcProfile = nbt == null ? null : NBTUtil.readGameProfileFromNBT(nbt);
            return Optional.ofNullable((GameProfile) mcProfile);
        }
        return Optional.empty();
    }

    public static boolean setProfile(ItemStack skull, @Nullable GameProfile profile) {
        if (isValidItemStack(skull) && getSkullType(skull).equals(SkullTypes.PLAYER)) {
            if (profile == null || profile.equals(SpongeRepresentedPlayerData.NULL_PROFILE)) {
                if (skull.getTagCompound() != null) {
                    skull.getTagCompound().removeTag(NbtDataUtil.ITEM_SKULL_OWNER);
                }
            } else {
                final NBTTagCompound nbt = new NBTTagCompound();
                NBTUtil.writeGameProfile(nbt, (com.mojang.authlib.GameProfile) resolveProfileIfNecessary(profile));
                skull.setTagInfo(NbtDataUtil.ITEM_SKULL_OWNER, nbt);
            }
            return true;
        }
        return false;
    }

    private static @Nullable GameProfile resolveProfileIfNecessary(@Nullable GameProfile profile) {
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

    public static void updatePlayerProfile(IMixinTileEntitySkull skull) {
        GameProfile profile = (GameProfile) skull.getPlayerProfile();
        if (profile != null && profile.getName().isPresent() && !profile.getName().get().isEmpty()) {
            if (profile.isFilled() && profile.getPropertyMap().containsKey("textures")) {
                skull.markDirty();
            } else {
                Sponge.getServer().getGameProfileManager().get(profile.getName().get()).handle((newProfile, thrown) -> {
                    if (newProfile != null) {
                        skull.setPlayerProfile((com.mojang.authlib.GameProfile) newProfile, false);
                        skull.markDirty();
                    } else {
                        SpongeImpl.getLogger().warn("Could not update player GameProfile for Skull: ",
                                thrown.getMessage());
                    }
                    return newProfile;
                });
            }
        } else {
            skull.markDirty();
        }
    }
}
