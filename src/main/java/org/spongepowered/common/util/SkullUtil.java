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
package org.spongepowered.common.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntitySkull;

public class SkullUtil {

    public static void updatePlayerProfile(IMixinTileEntitySkull skull) {
        GameProfile profile = (GameProfile) skull.getPlayerProfile();
        if (profile.getName().isPresent()) {
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
                        thrown.printStackTrace();
                    }
                    return newProfile;
                });
            }
        } else {
            skull.markDirty();
        }
    }

}
