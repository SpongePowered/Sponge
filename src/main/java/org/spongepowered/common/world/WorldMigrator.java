package org.spongepowered.common.world;

import net.minecraft.server.MinecraftServer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.spongepowered.api.Server;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Used to migrate Worlds from Bukkit -> Sponge
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
                        SpongeImpl.getLogger().warn("Cannot use path [" + containerCandidate + "] specified under [world-container] in bukkit"
                                + ".yml. Will use [" + worldContainer.toString() + "] instead.", ipe);
                    }
                }
            } catch (IOException ioe) {
                SpongeImpl.getLogger().warn("Cannot load bukkit.yml. Will use [" + worldContainer.toString() + "] instead.", ioe);
            }
        }

        return worldContainer;
    }

    public static void migrateWorldsTo(Path worldContainer) {
        SpongeImpl.getLogger().info("Checking for worlds that need to be migrated...");

        final Path oldWorldContainer = getOldWorldContainer();
        final List<Path> migrated = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(oldWorldContainer,
                entry -> !entry.getFileName().equals(worldContainer.getFileName()) && Files.exists(entry.resolve("level.dat")))) {
            for (Path oldWorldPath : stream) {
                Path worldPath = worldContainer.resolve(oldWorldPath.getFileName());

                // Only copy over the old world files if we don't have it already.
                if (Files.notExists(worldPath)) {
                    SpongeImpl.getLogger().info("Migrating [" + oldWorldPath.getFileName() + "] from [" + oldWorldContainer + "].");
                    try {
                        worldPath = renameToVanillaNetherOrEnd(worldContainer, oldWorldPath, worldPath);
                        com.google.common.io.Files.move(oldWorldPath.toFile(), worldPath.toFile());
                        // world_nether/world_the_end
                        if (isValidBukkitNetherOrEnd(worldContainer, oldWorldPath)) {
                            removeInnerNameFolder(worldPath);
                        }
                        migrated.add(worldPath);
                    } catch (IOException ioe) {
                        SpongeImpl.getLogger().warn("Failed to migrate [" + oldWorldPath.getFileName() + "] from [" + oldWorldContainer + "] to ["
                                + worldContainer + "].", ioe);
                    }
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        for (Path newWorldPath : migrated) {
            final Optional<World> optWorld = ((Server) MinecraftServer.getServer()).getWorld(newWorldPath.getFileName().toString());
            // We need to ask the server to load Bukkit plugin worlds
            if (!optWorld.isPresent()) {
                DimensionManager.registerDimension(DimensionManager.getNextFreeDimId(), 0);
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
     * Gets the name we should rename the incoming world folder to. Either "DIM-1" or "DIM1"
     * @param worldContainer The old world container where our world is coming from
     * @param oldWorldPath The old world path
     * @return The rename or the same name otherwise
     */
    private static String getVanillaNetherOrEndName(Path worldContainer, Path oldWorldPath) {
        String newName = oldWorldPath.getFileName().toString();
        final String[] split = oldWorldPath.getFileName().toString().split(worldContainer.getFileName().toString());
        if (split.length > 1) {
            if (split[1].equals("_nether")) {
                newName = "DIM-1";
            } else if (split[1].equals("_the_end")) {
                newName = "DIM1";
            }
        }
        return newName;
    }

    /**
     * Renames the world at the provided path to the proper Vanilla naming for Nether and The_End, if needed.
     *
     * Generally this is DIM-1 and DIM1 for Nether and The_End respectively.
     * @param worldPath The path to rename
     * @return The corrected path or the original path if un-needed
     */
    private static Path renameToVanillaNetherOrEnd(Path worldContainer, Path oldWorldPath, Path worldPath) {
        final String newName = getVanillaNetherOrEndName(worldContainer, oldWorldPath);
        final Path newWorldPath = worldContainer.resolve(newName);

        // Fix Bukkit's desire to have a folder name the same as Vanilla's dimension name INSIDE this folder's name
        // ex. world_nether/DIM-1 world_the_end/DIM1
        try {
            // Copy region within DIM-1/DIM1 to world_nether/world_the_end root
            com.google.common.io.Files.move(oldWorldPath.resolve(newName).resolve("region").toFile(), oldWorldPath.resolve("region").toFile());
        } catch (IOException ignore) {}

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
     * The last step of migration involves cleaning up folders within the world folders that are the same name as the root
     * folder's name (which happens due to how Bukkit stores DIM-1/DIM1 within world_nether/world_the_end
     * @param worldPath The path to inspect
     */
    private static void removeInnerNameFolder(Path worldPath) {
        // We successfully copied the region folder elsewhere so now delete the DIM-1/DIM1 inside
        // world_nether/world_the_end
        try {
            Files.delete(worldPath.resolve(worldPath.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
