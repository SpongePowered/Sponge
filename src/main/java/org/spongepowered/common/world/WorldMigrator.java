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

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.apache.commons.io.FileUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.file.CopyFileVisitor;
import org.spongepowered.common.SpongeImpl;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to migrate Worlds from Bukkit -> Sponge and fix core world errors
 */
public class WorldMigrator {

    /**
     * Gets the old world container used when this server used to be running Bukkit.
     *
     * @return The world container
     */
    public static Path getOldWorldContainer() {
        Path worldContainer = Paths.get(".");
        final Path bukkitYml = worldContainer.resolve("bukkit.yml");

        if (Files.exists(bukkitYml)) {
            try {
                final ConfigurationNode node = YAMLConfigurationLoader.builder().setPath(bukkitYml).build().load();
                final String containerCandidate = node.getNode("settings", "world-container").getString("");
                if (!containerCandidate.isEmpty()) {
                    try {
                        worldContainer = Paths.get(worldContainer.toString()).resolve(containerCandidate);
                    } catch (InvalidPathException ipe) {
                        SpongeImpl.getLogger().warn("Cannot use path [{}] specified under [world-container] in bukkit"
                                + ".yml. Will use [{}] instead.", containerCandidate, worldContainer, ipe);
                    }
                }
            } catch (IOException ioe) {
                SpongeImpl.getLogger().warn("Cannot load bukkit.yml. Will use [{}] instead.", worldContainer, ioe);
            }
        }

        return worldContainer;
    }

    /**
     * Performs the migration of worlds from {@link WorldMigrator#getOldWorldContainer()} to the provided worldContainer.
     *
     * @param worldContainer The container to move worlds to
     */
    public static void migrateWorldsTo(Path worldContainer) {
        SpongeImpl.getLogger().info("Checking for worlds that need to be migrated...");

        final Path oldWorldContainer = getOldWorldContainer();
        final List<Path> migrated = new ArrayList<>();

        if (Files.exists(oldWorldContainer)) {

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(oldWorldContainer,
                    entry -> !entry.getFileName().equals(worldContainer.getFileName()) && Files.exists(entry.resolve("level.dat")) && !Files
                            .exists(entry.resolve("level_sponge.dat")))) {
                for (Path oldWorldPath : stream) {
                    Path worldPath = worldContainer.resolve(getVanillaNetherOrEndName(oldWorldPath));

                    // Only copy over the old world files if we don't have it already.
                    if (Files.notExists(worldPath)) {
                        SpongeImpl.getLogger().info("Migrating [{}] from [{}].", oldWorldPath.getFileName(), oldWorldContainer);
                        try {
                            worldPath = renameToVanillaNetherOrEnd(worldContainer, oldWorldPath, worldPath);
                            Files.walkFileTree(oldWorldPath, new CopyFileVisitor(worldPath));
                            fixInnerNetherOrEndRegionData(worldPath);
                            removeInnerNameFolder(worldPath);
                            migrated.add(worldPath);
                        } catch (IOException ioe) {
                            SpongeImpl.getLogger().warn("Failed to migrate [{}] from [{}] to [{}]", oldWorldPath.getFileName(), oldWorldContainer,
                                    worldPath, ioe);
                        }

                        SpongeImpl.getLogger()
                                .info("Migrated world [{}] from [{}] to [{}]", oldWorldPath.getFileName(), oldWorldContainer, worldPath);
                    }
                }
            } catch (Exception ignore) {
            }

            if (!migrated.isEmpty()) {
                SpongeImpl.getLogger().info("[{}] worlds have been migrated back to Vanilla's format.", migrated.size());
            } else {
                SpongeImpl.getLogger().info("No worlds were found in need of migration.");
            }
        }

        // Previous to 1/10/2018, Sponge accidentally used the wrong save folder for client saves (put them on the root instead of saves/my_save).
        // This fixes this issue by moving over the folders and deleting them on the root
        if (Sponge.getPlatform().getType().isClient()) {
            try {
                Files.newDirectoryStream(worldContainer.getParent()).forEach((saveFolder) -> {
                    final boolean isWorld = Files.exists(saveFolder.resolve("level.dat"));
                    if (isWorld) {
                        final Path invalidSaveDataFolder = saveFolder.getParent().getParent().resolve(saveFolder.getFileName().toString());
                        try {
                            if (Files.isDirectory(invalidSaveDataFolder)) {
                                Files.walkFileTree(invalidSaveDataFolder, new SimpleFileVisitor<Path>() {

                                    @Override
                                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                        if (invalidSaveDataFolder != dir) {
                                            Path validFile = null;

                                            for (int i = 2; i < dir.getNameCount(); i++) {
                                                if (validFile == null) {
                                                    validFile = saveFolder.resolve(dir.getName(i));
                                                } else {
                                                    validFile = validFile.resolve(dir.getName(i));
                                                }
                                            }

                                            if (validFile != null && Files.notExists(validFile)) {
                                                Files.createDirectories(validFile);
                                            }
                                        }

                                        return super.preVisitDirectory(dir, attrs);
                                    }

                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        // Directories are made above
                                        if (!Files.isDirectory(file)) {
                                            Path validFile = null;

                                            for (int i = 2; i < file.getNameCount(); i++) {
                                                if (validFile == null) {
                                                    validFile = saveFolder.resolve(file.getName(i));
                                                } else {
                                                    validFile = validFile.resolve(file.getName(i));
                                                }
                                            }

                                            // Only move non-existing files in the proper directory
                                            if (validFile != null && Files.notExists(validFile)) {
                                                com.google.common.io.Files.move(file.toFile(), validFile.toFile());
                                            }
                                        }
                                        return super.visitFile(file, attrs);
                                    }
                                });

                                FileUtils.deleteDirectory(invalidSaveDataFolder.toFile());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Determines if a Bukkit nether or end is valid for the server's world container.
     *
     * If the server's world name is "world" and we are being given a "world_nether" and "world_the_end", we need to rename these to
     * "DIM-1/DIM1" (if possible).
     *
     * @param worldContainer The world container
     * @param oldWorldPath The old path to the incoming world
     * @return True to rename, false to not do so
     */
    private static boolean isValidBukkitNetherOrEnd(Path worldContainer, Path oldWorldPath) {
        return oldWorldPath.getFileName().toString().startsWith(worldContainer.getFileName().toString()) && (oldWorldPath.getFileName()
                .toString().endsWith("_nether") || oldWorldPath.getFileName().toString().endsWith("_the_end"));
    }

    /**
     * Gets the name we should rename the incoming world folder to. Either "DIM-1" or "DIM1".
     *
     * @param worldPath The world path to check
     * @return The rename or the same name otherwise
     */
    private static String getVanillaNetherOrEndName(Path worldPath) {
        final String newName = worldPath.getFileName().toString();
        if (newName.endsWith("_nether")) {
            return "DIM-1";
        } else if (newName.endsWith("_the_end")) {
            return "DIM1";
        } else {
            return newName;
        }
    }

    /**
     * Renames the world at the provided path to the proper Vanilla naming for Nether and The_End, if needed.
     *
     * Generally this is DIM-1 and DIM1 for Nether and The_End respectively.
     *
     * @param worldPath The path to rename
     * @return The corrected path or the original path if un-needed
     */
    private static Path renameToVanillaNetherOrEnd(Path worldContainer, Path oldWorldPath, Path worldPath) {
        final String newName = getVanillaNetherOrEndName(oldWorldPath);
        final Path newWorldPath = worldContainer.resolve(newName);

        // We only rename the Nether/The_End folder if the prefix matches the worldContainer directory name
        // Ex. If worldContainer directory name is "world" and folder names "world_nether" or "world_end" exist, we need to rename
        // those to the correct "DIM-1" and "DIM1" naming respectively.
        if (isValidBukkitNetherOrEnd(worldContainer, oldWorldPath)) {
            // If the new world container has no DIM-1/DIM1, we want to move these world files to those names
            if (Files.notExists(worldContainer.resolve(newName))) {
                return newWorldPath;
            }
        }

        // Either the new world container has a DIM-1/DIM1 or this is another plugin added world in Bukkit. Either way, just move it over.
        return worldPath;
    }

    /**
     * Fix Bukkit's desire to have a folder name the same as Vanilla's dimension name INSIDE this folder's name.
     *
     * ex. world_nether/DIM-1 world_the_end/DIM1
     *
     * @param oldWorldPath The path to the old world data
     */
    private static void fixInnerNetherOrEndRegionData(Path oldWorldPath) {
        try {
            // Copy region within DIM-1 to world root
            com.google.common.io.Files.move(oldWorldPath.resolve("DIM-1").resolve("region").toFile(), oldWorldPath.resolve("region").toFile());
        } catch (IOException ignore) {}

        try {
            // Copy region within DIM1 to world root
            com.google.common.io.Files.move(oldWorldPath.resolve("DIM1").resolve("region").toFile(), oldWorldPath.resolve("region").toFile());
        } catch (IOException ignore) {}
    }

    /**
     * The last step of migration involves cleaning up folders within the world folders that match Vanilla dimension names (DIM-1/DIM1).
     *
     * @param worldPath The path to inspect
     */
    private static void removeInnerNameFolder(Path worldPath) {
        // We successfully copied the region folder elsewhere so now delete the DIM-1/DIM1 inside
        try {
            Files.deleteIfExists(worldPath.resolve("DIM-1"));
        } catch (IOException ignore) {}

        try {
            Files.deleteIfExists(worldPath.resolve("DIM1"));
        } catch (IOException ignore) {}
    }
}
