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
package org.spongepowered.common.mixin.core.tileentity;

import com.mojang.authlib.GameProfile;
import net.minecraft.tileentity.SkullTileEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.data.processor.common.SkullUtils;
import org.spongepowered.common.bridge.tileentity.TileEntitySkullBridge;

@Mixin(SkullTileEntity.class)
public abstract class TileEntitySkullMixin extends TileEntityMixin implements TileEntitySkullBridge {

    @Shadow private com.mojang.authlib.GameProfile playerProfile;
    @Shadow private int skullType;

    @Override
    public void bridge$setPlayerProfile(final com.mojang.authlib.GameProfile mcProfile, final boolean update) {
        this.skullType = 3;
        this.playerProfile = mcProfile;
        if (update) {
            updatePlayerProfile();
        }
    }

    /**
     * @author windy - March 13th, 2016
     *
     * @reason Overwrite this method to overload to
     * {@link #bridge$setPlayerProfile(GameProfile, boolean)}. This allows to
     * set the profile from {@link SkullUtils} without invoking another update.
     *
     * @param mcProfile Minecraft GameProfile
     */
    @Overwrite
    public void setPlayerProfile(final com.mojang.authlib.GameProfile mcProfile) {
        bridge$setPlayerProfile(mcProfile, true);
    }

    /**
     * @author windy - March 13th, 2016
     * @reason Overwrite this method to delegate profile updating to the
     * {@link GameProfileManager} so skull updating can be handled
     * asynchronously.
     */
    @Overwrite
    private void updatePlayerProfile() {
        final org.spongepowered.api.profile.GameProfile profile = (org.spongepowered.api.profile.GameProfile) ((TileEntitySkullAccessor) this).accessor$getMojangProfile();
        if (profile != null && profile.getName().isPresent() && !profile.getName().get().isEmpty()) {
            if (profile.isFilled() && profile.getPropertyMap().containsKey("textures")) {
                ((TileEntityBridge) this).bridge$markDirty();
            } else {
                Sponge.getServer().getGameProfileManager().get(profile.getName().get()).handle((newProfile, thrown) -> {
                    if (newProfile != null) {
                        ((TileEntitySkullBridge) this).bridge$setPlayerProfile((GameProfile) newProfile, false);
                        ((TileEntityBridge) this).bridge$markDirty();
                    } else {
                        SpongeImpl.getLogger().warn("Could not update player GameProfile for Skull: ",
                                thrown.getMessage());
                    }
                    return newProfile;
                });
            }
        } else {
            ((TileEntityBridge) this).bridge$markDirty();
        }
    }

}
