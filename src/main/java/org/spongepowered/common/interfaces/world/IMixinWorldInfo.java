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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.registry.type.world.dimension.GlobalDimensionType;

import java.util.UUID;

import javax.annotation.Nullable;

public interface IMixinWorldInfo {

    /**
     * Gets the {@link DimensionType}.
     *
     * <p>If this {@link WorldInfo} has never been loaded by the server, it will have no
     * dimension type.</p>
     *
     * @return The dimension type
     */
    @Nullable
    DimensionType getDimensionType();

    /**
     * Sets the {@link DimensionType} of this {@link WorldInfo}.
     *
     * @param type The dimension type
     */
    void setDimensionType(@Nullable DimensionType type);

    /**
     * Sets the {@link GlobalDimensionType}.
     *
     * <p>Unlike the {@link IMixinWorldInfo#getDimensionType()}</p>, we know the global one when constructing
     * a new {@link WorldInfo} or when loading from disk for the first time (as it is part of our contract)</p>
     * @param dimensionType The global dimension type
     */
    void setGlobalDimensionType(GlobalDimensionType dimensionType);
    /**
     * Gets the {@link UUID} of this {@link WorldInfo}.
     *
     * <p>If this info has never been loaded by the server, it will not have an assigned unique id.</p>
     *
     * @return The unique id
     */
    @Nullable
    UUID getUniqueId();

    /**
     * Sets the {@link UUID} of this {@link WorldInfo}.
     *
     * @param uniqueId The unique id
     */
    void setUniqueId(@Nullable UUID uniqueId);

    /**
     * Sets the directory name that backs this {@link WorldInfo}.
     *
     * @param directoryName The directory name
     */
    void setDirectoryName(String directoryName);

    /**
     * Gets if this {@link WorldInfo} is fake.
     *
     * <p>Fakeness is generally determined as a world info that is used by the base game in
     * connecting to a server, "MpServer", we which don't want to touch as well as world infos
     * that we create due to mods creating {@link WorldServer} objects with no properties.</p>
     *
     * @return True if false, false if not
     */
    boolean isFake();

    /**
     * Returns if this {@link WorldInfo} was created by a mod.
     *
     * <p>Only used by SpongeForge.</p>
     *
     * @return True if created by a mod, false if not
     */
    default boolean isCreatedByMod() {
        return false;
    }

    /**
     * Sets if this {@link WorldInfo} was created by a mod.
     *
     * @param createdByMod If created by a mod or not
     */
    void setCreatedByMod(boolean createdByMod);

    /**
     * Sets the {@link PortalAgentType} used to handle teleporting to this world.
     *
     * @param type The portal agent type
     */
    void setPortalAgentType(PortalAgentType type);

    /**
     * Gets if a custom {@link EnumDifficulty} has been set on this {@link WorldInfo}.
     *
     * <p>This is used to determine if the difficulty specified in the "server.properties" should override
     * the difficulty set in the world data. Unfortunately, Vanilla always overrides this value with what the server
     * specifies.</p>
     *
     * @return True if a custom difficulty has been set, false if not
     */
    boolean hasCustomDifficulty();

    /**
     * Sets the {@link EnumDifficulty} of this {@link WorldInfo} without triggering a custom difficulty to be set.
     *
     * <p>This is used to set the difficulty from the server without Sponge thinking this difficulty should always be set.</p>
     *
     * @param difficulty The difficulty
     */
    void setNonCustomDifficulty(EnumDifficulty difficulty);

    /**
     * Gets the index in the player {@link UUID} table.
     *
     * <p>This is a component of Sponge's tracking system.</p>
     *
     * @param uniqueId The unique id
     * @return The index or null if we have no knowledge of it
     */
    @Nullable
    Integer getOrCreateIndexForPlayerUniqueId(UUID uniqueId);

    /**
     * Gets the {@link UUID} of a player by the index in the table
     *
     * <p>This is a component of Sponge's tracking system.</p>
     *
     * @param index The index
     * @return The unique id or null if we have no knowledge of it
     */
    @Nullable
    UUID getPlayerUniqueIdForIndex(int index);

    /**
     * Gets the {@link SpongeConfig<WorldConfig>} for this {@link WorldInfo} or creates it if it doesn't exist.
     *
     * <p>This normally exists in ./config/sponge/worlds/<dimension_type>/<level_name></p>
     *
     * @return The config
     */
    SpongeConfig<WorldConfig> getOrCreateConfig();

    /**
     * Gets the {@link SpongeConfig<WorldConfig>} for this {@link WorldInfo}.
     *
     * @return The config
     */
    @Nullable
    SpongeConfig<WorldConfig> getConfig();

    /**
     * Sets the {@link SpongeConfig<WorldConfig>} for this {@link WorldInfo}.
     *
     * @param config The config
     */
    void setConfig(@Nullable SpongeConfig<WorldConfig> config);

    /**
     * Reads from the provided {@link NBTTagCompound} that contains the decoded data from the
     * Sponge level data.
     *
     * <p>Properties from the compound are set as additional properties injected onto {@link WorldInfo}.</p>
     *
     * @param compound The compound
     */
    void readSpongeCompound(NBTTagCompound compound);

    /**
     * Writes to the provided {@link NBTTagCompound} properties on this {@link WorldInfo} that Sponge
     * injected from our level data.
     *
     * @param compound The compound
     * @return The passed compound, with our properties written
     */
    NBTTagCompound writeSpongeCompound(NBTTagCompound compound);
}
