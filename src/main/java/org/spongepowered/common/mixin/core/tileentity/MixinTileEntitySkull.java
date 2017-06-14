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
import org.spongepowered.api.block.tileentity.Skull;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.processor.common.SkullUtils;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntitySkull;

import java.util.List;
import java.util.Optional;

@Mixin(TileEntitySkull.class)
@Implements(@Interface(iface = IMixinTileEntitySkull.class, prefix = "skull$"))
public abstract class MixinTileEntitySkull extends MixinTileEntity implements Skull {

    @Shadow private com.mojang.authlib.GameProfile playerProfile;
    @Shadow private int skullType;

    @Shadow public abstract com.mojang.authlib.GameProfile shadow$getPlayerProfile();

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getSkullData());
        Optional<RepresentedPlayerData> profileData = get(RepresentedPlayerData.class);
        if (profileData.isPresent()) {
            manipulators.add(profileData.get());
        }
    }

    @Intrinsic
    public com.mojang.authlib.GameProfile skull$getPlayerProfile() {
        return shadow$getPlayerProfile();
    }

    public void setPlayerProfile(com.mojang.authlib.GameProfile mcProfile, boolean update) {
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
     * {@link #setPlayerProfile(GameProfile, boolean)}. This allows to
     * set the profile from {@link SkullUtils} without invoking another update.
     *
     * @param mcProfile Minecraft GameProfile
     */
    @Overwrite
    public void setPlayerProfile(com.mojang.authlib.GameProfile mcProfile) {
        setPlayerProfile(mcProfile, true);
    }

    /**
     * @author windy - March 13th, 2016
     * @reason Overwrite this method to delegate profile updating to the
     * {@link GameProfileManager} so skull updating can be handled
     * asynchronously.
     */
    @Overwrite
    private void updatePlayerProfile() {
        SkullUtils.updatePlayerProfile((IMixinTileEntitySkull) this);
    }

}
