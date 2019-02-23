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
package org.spongepowered.common.world;

import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.world.WorldArchetype;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorldLoader {

    /**
     * Gets a {@link Path} pointing to the directory where world saves can be located.
     *
     * <p>On SpongeVanilla or SpongeForge (in server mode), this is typically the same as the Overworld's save folder.</p>
     *
     * <p>On SpongeForge (in client mode), this is typically the name of the save the client gives when creating a new SSP save.</p>
     * @return The save folder
     */
    Path getSaveDirectory();

    /**
     * Creates a new {@link WorldInfo}.
     *
     * <p>This will cause an appropriate directory for the offline world's data as well as a config directory to be created.
     * Ex. .\world\world_a and .\config\sponge\worlds\overworld\world_a.conf</p>
     *
     * <p>If the world data already exists, it will be returned instead and that data may represent an
     * offline or online world.</p>
     *
     * @param directoryName The folder name
     * @param archetype The world archetype
     * @return The world info or {@link Optional#empty()} if creation failed
     */
    Optional<WorldInfo> createWorldInfo(String directoryName, WorldArchetype archetype);

    /**
     * Registers a {@link WorldInfo}.
     *
     * @param directoryName The folder name
     * @param info The world info
     */
    void registerWorldInfo(String directoryName, WorldInfo info);

    /**
     * Gets a {@link WorldInfo} by {@link DimensionType}.
     *
     * <p>This may be an offline or online world.</p>
     *
     * @param type The dimension type
     * @return The world info or {@link Optional#empty()} if not found
     */
    Optional<WorldInfo> getWorldInfo(DimensionType type);

    /**
     * Gets a {@link WorldInfo} by {@link UUID}.
     *
     * @param uniqueId The unique id
     * @return The world info or {@link Optional#empty()} if not found
     */
    Optional<WorldInfo> getWorldInfo(UUID uniqueId);

    /**
     * Gets a {@link WorldInfo} by directory name.
     *
     * @param directoryName The directory name
     * @return The world info or {@link Optional#empty()} if not found
     */
    Optional<WorldInfo> getWorldInfo(String directoryName);

    /**
     * Unregisters a {@link WorldInfo} by it's directory name.
     *
     * @param directoryName The directory name
     */
    void unregisterWorldInfo(String directoryName);

    /**
     * Unregisters a {@link WorldInfo} from being tracked by this manager.
     *
     * @param info The world info
     */
    void unregisterWorldInfo(WorldInfo info);

    /**
     * Returns a {@link Collection} of all known unloaded {@link WorldInfo}s.
     *
     * <p>
     *     Keep in mind that this is a best effort attempt and is only known when plugins/mods unload worlds. If a user copies a world
     *     into this folder manually, we will have no knowledge of it until it is loaded and then unloaded.
     * </p>
     *
     * @return The offline worlds
     */
    Collection<WorldInfo> getUnloadedWorldInfos();

    /**
     * Returns a {@link Collection} of all known, loaded and unloaded, {@link WorldInfo}s.
     *
     * <p>
     *     Keep in mind that this is a best effort attempt and is unloaded ones are only known when plugins/mods unload worlds. If a user copies a
     *     world into this folder manually, we will have no knowledge of it until it is loaded.
     * </p>
     *
     * @return The offline worlds
     */
    Collection<WorldInfo> getWorldInfos();

    /**
     * Loads a {@link WorldServer} by it's {@link UUID}.
     *
     * <p>If the world is already loaded, it will be returned.</p>
     *
     * @param uniqueId The unique id
     * @return The world or {@link Optional#empty()}} if load failed
     */
    Optional<WorldServer> loadWorld(UUID uniqueId);

    /**
     * Loads a {@link WorldInfo} based on a {@link WorldInfo}.
     *
     * <p>If the world is already loaded, it will be returned.</p>
     *
     * <p>If no persistent world data exists for the info provided, it will be created.</p>
     *
     * @param info The world info
     * @return The world or {@link Optional#empty()}} if load failed
     */
    Optional<WorldServer> loadWorld(WorldInfo info);

    /**
     * Loads a {@link WorldServer} by it's {@link UUID}.
     *
     * <p>If the world is already loaded, it will be returned.</p>
     *
     * @param directoryName The directory name
     * @return The world or {@link Optional#empty()}} if load failed
     */
    Optional<WorldServer> loadWorld(String directoryName);

    /**
     * Unloads a {@link WorldServer} by it's {@link DimensionType}.
     *
     * <p>If the dimension type represents an offline world, a false result will be returned.</p>
     *
     * <p>If specifying false to check config, the world will unload regardless if set to never unload.</p>
     *
     * @param dimensionType The dimension type
     * @param checkConfig A toggle to specify checking configuration data
     * @return True if the world unloaded, false if it could not
     */
    default boolean unloadWorld(DimensionType dimensionType, boolean checkConfig) {
        final WorldServer world = this.getWorld(dimensionType).orElse(null);
        if (world == null) {
            return false;
        }

        return this.unloadWorld(world, checkConfig);
    }

    /**
     * Unloads a {@link WorldServer} by it's folder name.
     *
     * <p>If the directory name represents an offline world, a false result will be returned.</p>
     *
     * <p>If specifying false to check config, the world will unload regardless if set to never unload.</p>
     *
     * @param directoryName The directory name
     * @param checkConfig A toggle to specify checking configuration data
     * @return True if the world unloaded, false if it could not
     */
    default boolean unloadWorld(String directoryName, boolean checkConfig) {
        final WorldServer world = this.getWorld(directoryName).orElse(null);
        if (world == null) {
            return false;
        }

        return this.unloadWorld(world, checkConfig);
    }

    /**
     * Unloads a {@link WorldServer} by it's {@link UUID}.
     *
     * <p>If the unique id represents an offline world, a false result will be returned.</p>
     *
     * <p>If specifying false to check config, the world will unload regardless if set to never unload.</p>
     *
     * @param uniqueId The unique id
     * @param checkConfig A toggle to specify checking configuration data
     * @return True if the world unloaded, false if it could not
     */
    default boolean unloadWorld(UUID uniqueId, boolean checkConfig) {
        final WorldServer world = this.getWorld(uniqueId).orElse(null);
        if (world == null) {
            return false;
        }

        return this.unloadWorld(world, checkConfig);
    }

    /**
     * Unloads a {@link WorldServer}.
     *
     * <p>If the world is unknown to the server, a false result will be returned.</p>
     *
     * <p>If specifying false to check config, the world will unload regardless if set to never unload.</p>
     *
     * @param world The world
     * @param checkConfig A toggle to specify checking configuration data
     * @return True if the world unloaded, false if it could not
     */
    boolean unloadWorld(WorldServer world, boolean checkConfig);

    /**
     * Gets a {@link WorldServer} by it's registered {@link DimensionType}.
     *
     * @param type The dimension type
     * @return The world or {@link Optional#empty()} if not found
     */
    Optional<WorldServer> getWorld(DimensionType type);

    /**
     * Gets a {@link WorldServer} by it's registered folder name.
     *
     * @param directoryName The directory name
     * @return The world or {@link Optional#empty()} if not found
     */
    Optional<WorldServer> getWorld(String directoryName);

    /**
     * Gets a {@link WorldServer} by it's {@link UUID}.
     *
     * @param uuid The unique id
     * @return The world or {@link Optional#empty()} if not found
     */
    Optional<WorldServer> getWorld(UUID uuid);

    /**
     * Gets all {@link WorldServer}s.
     *
     * @return The worlds
     */
    List<WorldServer> getWorlds();
}
