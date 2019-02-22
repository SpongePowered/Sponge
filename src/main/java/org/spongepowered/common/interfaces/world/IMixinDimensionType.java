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
package org.spongepowered.common.interfaces.world;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.common.registry.type.world.dimension.GlobalDimensionType;

public interface IMixinDimensionType {

    /**
     * Gets the {@link GlobalDimensionType}.
     *
     * <p>This is used to associate {@link WorldServer} {@link DimensionType}s to their global one. Ex. World A, B, and C can all be "Overworld"
     * style worlds, each have their own Mojang DimensionType, but one Sponge DimensionType.</p>
     * @return The global dimension type
     */
    GlobalDimensionType getGlobalDimensionType();

    /**
     * Sets the {@link GlobalDimensionType}.
     *
     * @see {@link IMixinDimensionType#getGlobalDimensionType()}
     *
     * @param dimensionType The global dimension type
     */
    void setGlobalDimensionType(GlobalDimensionType dimensionType);

    /**
     * Gets a {@link DimensionType} that is compatible with a client.
     *
     * <p>In SpongeVanilla, this will return {@link DimensionType#OVERWORLD} for {@link Dimension} that are not Vanilla.</p>
     *
     * <p>In SpongeForge. this will return the result that SpongeVanilla would if and only if the client is Vanilla. Otherwise
     * the dimension registration will be sent down.</p>
     * @return The compatible dimension type
     */
    DimensionType asClientDimensionType();

    /**
     * Sends dimension registration to the {@link EntityPlayerMP}.
     *
     * <p>This is entirely dependent upon the player's client honoring this. Mostly added for Forge clients.</p>
     * @param player The player
     */
    void sendDimensionRegistrationTo(EntityPlayerMP player);
}
