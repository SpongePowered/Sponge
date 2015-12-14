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
package org.spongepowered.common;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ProviderExistsException;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.rcon.RconService;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.command.CommandSponge;
import org.spongepowered.common.command.SpongeHelpCommand;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.registry.type.world.DimensionRegistryModule;
import org.spongepowered.common.service.pagination.SpongePaginationService;
import org.spongepowered.common.service.rcon.MinecraftRconService;
import org.spongepowered.common.service.sql.SqlServiceImpl;
import org.spongepowered.common.service.user.SpongeUserStorageService;
import org.spongepowered.common.text.action.SpongeCallbackHolder;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.SpongeDimensionType;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

/**
 * Used to setup the ecosystem.
 */
@NonnullByDefault
public final class SpongeBootstrap {

    public static void initializeServices() {
        registerService(SqlService.class, new SqlServiceImpl());
        registerService(PaginationService.class, new SpongePaginationService());
        if (SpongeImpl.getGame().getPlatform().getType() == Platform.Type.SERVER) {
            registerService(RconService.class, new MinecraftRconService((DedicatedServer) MinecraftServer.getServer()));
        }
        registerService(UserStorageService.class, new SpongeUserStorageService());
        SpongeImpl.getGame().getServiceManager().potentiallyProvide(PermissionService.class)
                .executeWhenPresent(input -> SpongeImpl.getGame().getServer().getConsole().getContainingCollection());
    }

    public static void initializeCommands() {
        Sponge.getCommandDispatcher().register(SpongeImpl.getPlugin(), CommandSponge.getCommand(), "sponge", "sp");
        Sponge.getCommandDispatcher().register(SpongeImpl.getPlugin(), SpongeHelpCommand.create(), "help");
        Sponge.getCommandDispatcher().register(SpongeImpl.getPlugin(), SpongeCallbackHolder.getInstance().createCommand(), SpongeCallbackHolder.CALLBACK_COMMAND);
    }

    private static <T> boolean registerService(Class<T> serviceClass, T serviceImpl) {
        try {
            SpongeImpl.getGame().getServiceManager().setProvider(SpongeImpl.getPlugin(), serviceClass, serviceImpl);
            return true;
        } catch (ProviderExistsException e) {
            SpongeImpl.getLogger().warn("Non-Sponge {} already registered: {}", serviceClass.getSimpleName(), e.getLocalizedMessage());
            return false;
        }
    }

    public static void registerWorlds() {
        final File[] directoryListing = DimensionManager.getCurrentSaveRootDirectory().listFiles();
        if (directoryListing == null) {
            return;
        }

        for (File child : directoryListing) {
            File levelData = new File(child, "level_sponge.dat");
            if (!child.isDirectory() || !levelData.exists()) {
                continue;
            }

            try {
                NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(levelData));
                if (nbt.hasKey(NbtDataUtil.SPONGE_DATA)) {
                    NBTTagCompound spongeData = nbt.getCompoundTag(NbtDataUtil.SPONGE_DATA);
                    int dimensionId = spongeData.getInteger("dimensionId");
                    String dimType = spongeData.getString("dimensionType");
                    // Temporary fix for old data, remove in future build
                    if (dimType.equalsIgnoreCase("net.minecraft.world.WorldProviderSurface")) {
                        dimType = "overworld";
                    } else if (dimType.equalsIgnoreCase("net.minecraft.world.WorldProviderHell")) {
                        dimType = "nether";
                    } else if (dimType.equalsIgnoreCase("net.minecraft.world.WorldProviderEnd")) {
                        dimType = "the_end";
                    }
                    spongeData.setString("dimensionType", dimType);
                    String worldFolder = spongeData.getString("LevelName");
                    SpongeConfig<?> activeConfig = SpongeHooks.getActiveConfig(dimType, worldFolder);

                    if (dimensionId == -1) {
                        if (!MinecraftServer.getServer().getAllowNether()) {
                            continue;
                        }
                    }

                    if (!activeConfig.getConfig().getWorld().getKeepSpawnLoaded()) {
                        SpongeImpl.getLogger().info("World 'keepSpawnLoaded' {} is currently disabled. Skipping world load...", child.getName());
                            continue;
                        }

                    if (!activeConfig.getConfig().getWorld().loadOnStartup()) {
                        SpongeImpl.getLogger().info("World {} 'loadOnStartup' is disabled.. Skipping world load...", child.getName());
                            continue;
                        }

                    if (spongeData.hasKey("uuid_most") && spongeData.hasKey("uuid_least")) {
                        UUID uuid = new UUID(spongeData.getLong("uuid_most"), spongeData.getLong("uuid_least"));
                        DimensionRegistryModule.getInstance().registerWorldUniqueId(uuid, child.getName());
                    }
                    if (spongeData.hasKey("dimensionId") && activeConfig.getConfig().getWorld().isWorldEnabled()) {
                        int dimension = spongeData.getInteger("dimensionId");
                        DimensionRegistryModule.getInstance().dimensionClassMappings.entrySet().forEach(mapEntry -> {
                            if (mapEntry.getValue().getId().equalsIgnoreCase(spongeData.getString("dimensionType"))) {
                                DimensionRegistryModule.getInstance().registerWorldDimensionId(dimension, child.getName());
                                if (!DimensionManager.isDimensionRegistered(dimension)) {
                                    DimensionManager.registerDimension(dimension,
                                                                       ((SpongeDimensionType) mapEntry.getValue()).getDimensionTypeId());
                                }
                            }
                        });

                    } else {
                        SpongeImpl.getLogger().info("World {} is disabled! Skipping world registration...", child.getName());
                    }
                }
            } catch (Throwable t) {
                SpongeImpl.getLogger().error("Error during world registration.", t);
            }
        }
    }
}
