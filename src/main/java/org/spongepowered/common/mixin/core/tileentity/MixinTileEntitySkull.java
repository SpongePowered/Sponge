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
import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntitySkull;
import org.spongepowered.common.util.SkullUtil;

@Mixin(TileEntitySkull.class)
@Implements(@Interface(iface = IMixinTileEntitySkull.class, prefix = "skull$"))
public abstract class MixinTileEntitySkull {

    @Shadow private com.mojang.authlib.GameProfile playerProfile;
    @Shadow private int skullType;

    @Shadow public abstract com.mojang.authlib.GameProfile shadow$getPlayerProfile();

    @Intrinsic
    public com.mojang.authlib.GameProfile skull$getPlayerProfile() {
        return shadow$getPlayerProfile();
    }

    public void setPlayerProfile(com.mojang.authlib.GameProfile mcProfile, boolean update) {
        System.out.println(">>> setPlayerProfile()");
        this.skullType = 3;
        this.playerProfile = mcProfile;
        if (update) {
            updatePlayerProfile();
        }
    }

    /**
     * Overwrite this method to overload to
     * {@link #setPlayerProfile(GameProfile, boolean)}. This allows to
     * set the profile from {@link SkullUtil} without invoking another update.
     *
     * - windy 3/13/16
     *
     * @param mcProfile Minecraft GameProfile
     */
    @Overwrite
    public void setPlayerProfile(com.mojang.authlib.GameProfile mcProfile) {
        System.out.println(">>> setPlayerProfile()");
        setPlayerProfile(mcProfile, true);
    }

    /**
     * Overwrite this method to delegate profile updating to the
     * {@link GameProfileManager} so skull updating can be handled
     * asynchronously.
     *
     * - windy 3/13/16
     */
    @Overwrite
    private void updatePlayerProfile() {
        System.out.println(">>> updatePlayerProfile()");
        SkullUtil.updatePlayerProfile((IMixinTileEntitySkull) this);
    }

}
