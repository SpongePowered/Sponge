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
import org.apache.logging.log4j.spi.AbstractLogger;
import org.slf4j.Logger;
import org.slf4j.impl.SLF4JLogger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.data.property.PropertyRegistry;
import org.spongepowered.api.service.ProviderExistsException;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.command.SimpleCommandService;
import org.spongepowered.api.service.config.ConfigService;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.api.service.profile.GameProfileResolver;
import org.spongepowered.api.service.rcon.RconService;
import org.spongepowered.api.service.scheduler.SchedulerService;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.service.user.UserStorage;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.common.command.CommandSponge;
import org.spongepowered.common.command.SpongeCommandDisambiguator;
import org.spongepowered.common.command.SpongeHelpCommand;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.service.config.SpongeConfigService;
import org.spongepowered.common.service.pagination.SpongePaginationService;
import org.spongepowered.common.service.persistence.SpongeSerializationService;
import org.spongepowered.common.service.profile.SpongeProfileResolver;
import org.spongepowered.common.service.rcon.MinecraftRconService;
import org.spongepowered.common.service.scheduler.SpongeScheduler;
import org.spongepowered.common.service.sql.SqlServiceImpl;
import org.spongepowered.common.service.user.SpongeUserStorage;
import org.spongepowered.common.text.action.SpongeCallbackHolder;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.SpongeDimensionType;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.UUID;

/**
 * Used to setup the ecosystem.
 */
@NonnullByDefault
public final class SpongeBootstrap {
    private static final Logger slf4jLogger = new SLF4JLogger((AbstractLogger) Sponge.getLogger(), Sponge.getLogger().getName());

    public static void initializeServices() {
        SimpleCommandService commandService = new SimpleCommandService(Sponge.getGame(), slf4jLogger,
                new SpongeCommandDisambiguator(Sponge.getGame()));
        if (registerService(CommandService.class, commandService)) {
            commandService.register(Sponge.getPlugin(), CommandSponge.getCommand(), "sponge", "sp");
            commandService.register(Sponge.getPlugin(), SpongeHelpCommand.create(), "help");
            commandService.register(Sponge.getPlugin(), SpongeCallbackHolder.getInstance().createCommand(), SpongeCallbackHolder.CALLBACK_COMMAND);
        }

        registerService(SqlService.class, new SqlServiceImpl());
        if (!registerService(SchedulerService.class, SpongeScheduler.getInstance())) {
            throw new ExceptionInInitializerError("Cannot continue with a Non-Sponge Scheduler!");
        }
        registerService(SerializationService.class, new SpongeSerializationService());
        registerService(PropertyRegistry.class, SpongePropertyRegistry.getInstance());
        registerService(PaginationService.class, new SpongePaginationService());
        if (Sponge.getGame().getPlatform().getType() == Platform.Type.SERVER) {
            registerService(RconService.class, new MinecraftRconService((DedicatedServer) MinecraftServer.getServer()));
        }
        registerService(ConfigService.class, new SpongeConfigService(Sponge.getGame().getPluginManager()));
        registerService(UserStorage.class, new SpongeUserStorage());
        registerService(GameProfileResolver.class, new SpongeProfileResolver());
        Sponge.getGame().getServiceManager().potentiallyProvide(PermissionService.class).executeWhenPresent(input -> {
            Sponge.getGame().getServer().getConsole().getContainingCollection();
            return true;
        });
    }

    private static <T> boolean registerService(Class<T> serviceClass, T serviceImpl) {
        try {
            Sponge.getGame().getServiceManager().setProvider(Sponge.getPlugin(), serviceClass, serviceImpl);
            return true;
        } catch (ProviderExistsException e) {
            Sponge.getLogger().warn("Non-Sponge {} already registered: {}", serviceClass.getSimpleName(), e.getLocalizedMessage());
            return false;
        }
    }

    public static void preInitializeRegistry() {
        ((SpongeGameRegistry) Sponge.getGame().getRegistry()).preInit();
    }

    public static void initializeRegistry() {
        ((SpongeGameRegistry) Sponge.getGame().getRegistry()).init();
    }

    public static void postInitializeRegistry() {
        ((SpongeGameRegistry) Sponge.getGame().getRegistry()).postInit();
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
                    boolean enabled = spongeData.getBoolean("enabled");
                    boolean loadOnStartup = spongeData.getBoolean("loadOnStartup");
                    int dimensionId = spongeData.getInteger("dimensionId");
                    if (!(dimensionId == -1) && !(dimensionId == 0) && !(dimensionId == 1)) {
                        if (!enabled) {
                            Sponge.getLogger().info("World {} is currently disabled. Skipping world load...", child.getName());
                            continue;
                        }
                        if (!loadOnStartup) {
                            Sponge.getLogger().info("World {} 'loadOnStartup' is disabled.. Skipping world load...", child.getName());
                            continue;
                        }
                    } else if (dimensionId == -1) {
                        if (!MinecraftServer.getServer().getAllowNether()) {
                            continue;
                        }
                    }
                    if (spongeData.hasKey("uuid_most") && spongeData.hasKey("uuid_least")) {
                        UUID uuid = new UUID(spongeData.getLong("uuid_most"), spongeData.getLong("uuid_least"));
                        Sponge.getSpongeRegistry().registerWorldUniqueId(uuid, child.getName());
                    }
                    if (spongeData.hasKey("dimensionId") && spongeData.getBoolean("enabled")) {
                        int dimension = spongeData.getInteger("dimensionId");
                        for (Map.Entry<Class<? extends Dimension>, DimensionType> mapEntry : Sponge.getSpongeRegistry().dimensionClassMappings
                                .entrySet()) {
                            if (mapEntry.getKey().getCanonicalName().equalsIgnoreCase(spongeData.getString("dimensionType"))) {
                                Sponge.getSpongeRegistry().registerWorldDimensionId(dimension, child.getName());
                                if (!DimensionManager.isDimensionRegistered(dimension)) {
                                    DimensionManager.registerDimension(dimension,
                                            ((SpongeDimensionType) mapEntry.getValue()).getDimensionTypeId());
                                }
                            }
                        }
                    } else {
                        Sponge.getLogger().info("World {} is disabled! Skipping world registration...", child.getName());
                    }
                }
            } catch (Throwable t) {
                Sponge.getLogger().error("Error during world registration.", t);
            }
        }
    }
}
