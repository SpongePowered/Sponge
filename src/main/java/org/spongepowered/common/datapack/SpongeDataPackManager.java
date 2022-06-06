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
package org.spongepowered.common.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.datapack.DataPackEntry;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.world.server.DataPackManager;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.lifecycle.RegisterDataPackValueEventImpl;
import org.spongepowered.common.item.recipe.ingredient.IngredientResultUtil;
import org.spongepowered.common.item.recipe.ingredient.SpongeIngredient;
import org.spongepowered.common.util.FutureUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class SpongeDataPackManager implements DataPackManager {

    private final MinecraftServer server;
    private final Path packsDir;
    private boolean ignoreNext;

    public SpongeDataPackManager(final MinecraftServer server, final Path packsDir) {
        this.server = server;
        this.packsDir = packsDir;
    }

    // TODO IntegratedServer call?
    public void init() {
        this.ignoreNext = false;
        final List<String> reloadablePacks = this.registerPacks();
        if (!reloadablePacks.isEmpty()) {
            SpongeCommon.logger().info("Reloading for plugin data packs... " + reloadablePacks.size());
            this.ignoreNext = true;
            this.server.reloadResources(this.discoverNewPacks());
        }
    }

    // see ReloadCommand#discoverNewPacks
    private List<String> discoverNewPacks() {
        final PackRepository packRepo = this.server.getPackRepository();
        final List<String> toReload = new ArrayList<>(packRepo.getSelectedIds());
        packRepo.reload();
        final List<String> disabled = this.server.getWorldData().getDataPackConfig().getDisabled();
        for (final String available : packRepo.getAvailableIds()) {
            if (!disabled.contains(available) && !toReload.contains(available)) {
                toReload.add(available);
            }
        }
        return toReload;
    }

    @Override
    public CompletableFuture<Void> reload() {
        this.ignoreNext = false;
        return this.server.reloadResources(this.discoverNewPacks());
    }

    public List<String> registerPacks() {
        // Ignore reload immediately after first call
        if (this.ignoreNext) {
            this.ignoreNext = false;
            return List.of();
        }

        SpongeIngredient.clearCache();
        IngredientResultUtil.clearCache();

        final List<String> reloadablePacks = new ArrayList<>();
        this.registerAndSerializePack(DataPackTypes.ADVANCEMENT, reloadablePacks);
        this.registerAndSerializePack(DataPackTypes.RECIPE, reloadablePacks);
        this.registerAndSerializePack(DataPackTypes.TAG, reloadablePacks);
        return reloadablePacks;
    }

    private <T extends DataPackEntry<T>> List<T> callRegisterDataPackValueEvent(final DataPackType<T> type) {
        final RegisterDataPackValueEventImpl<T> event = new RegisterDataPackValueEventImpl<>(Cause.of(EventContext.empty(), SpongeCommon.game()), SpongeCommon.game(), type);
        SpongeCommon.post(event);
        return event.serializables();
    }

    @SuppressWarnings("unchecked")
    private <T extends DataPackEntry<T>> void registerAndSerializePack(final DataPackType<T> type, final Collection<String> reloadablePacks) {
        final SpongeDataPackType<T> implType = (SpongeDataPackType<T>) type;
        final List<T> packEntries = this.callRegisterDataPackValueEvent(type);
        if (packEntries.isEmpty()) {
            return;
        }
        this.serializePack(implType, reloadablePacks, packEntries);
    }

    private <T extends DataPackEntry<T>> void serializePack(final SpongeDataPackType<T> implType, final Collection<String> reloadablePacks, final List<T> packEntries) {
        final String fullPackName = "file/" + this.packName(implType);
        try {
            final boolean success = implType.packSerializer().serialize(implType, this.packDir(implType), packEntries);
            if (success && implType.reloadable()) {
                reloadablePacks.add(fullPackName);
            } else {
                reloadablePacks.remove(fullPackName);
            }
        } catch (final IOException e) {
            reloadablePacks.remove(fullPackName);
            SpongeCommon.logger().error(e);
        }
    }

    @Override
    public <T extends DataPackEntry<T>> CompletableFuture<Boolean> save(final T entry) {
        final SpongeDataPackType<T> implType = (SpongeDataPackType<T>) entry.type();
        // TODO this is actually blocking
        this.serializePack(implType, new ArrayList<>(), List.of(entry));
        return CompletableFuture.completedFuture(implType.reloadable());
    }

    @Override
    public <T extends DataPackEntry<T>> CompletableFuture<Optional<T>> load(final DataPackType<T> type, final ResourceKey key) {
        Objects.requireNonNull(key, "key");
        final SpongeDataPackType<T> implType = (SpongeDataPackType<T>) type;
        final Path file = this.packFile(implType, key);
        if (Files.exists(file)) {
            try (final InputStream stream = Files.newInputStream(file); final InputStreamReader reader = new InputStreamReader(stream)) {
                final JsonElement element = JsonParser.parseReader(reader);
                if (implType.decoder() != null) {
                    // TODO this is actually blocking
                    final T decoded = implType.decoder().decode(key, element, this.server.registryAccess());
                    return CompletableFuture.completedFuture(Optional.of(decoded));
                }
            } catch (IOException ex) {
                return FutureUtil.completedWithException(ex);
            }
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public boolean exists(final DataPackType<?> type, final ResourceKey key) {
        final Path file = this.packFile(type, Objects.requireNonNull(key, "key"));
        return Files.exists(file);
    }

    @Override
    public boolean delete(final DataPackType<?> type, final ResourceKey key) throws IOException {
        final Path file = this.packFile(type, Objects.requireNonNull(key, "key"));
        return Files.deleteIfExists(file);
    }

    public void copy(final DataPackType<?> type, final ResourceKey from, final ResourceKey to) throws IOException {
        final Path fileFrom = this.packFile(type, Objects.requireNonNull(from, "from"));
        final Path fileto = this.packFile(type, Objects.requireNonNull(to, "to"));
        Files.createDirectories(fileto.getParent());
        Files.copy(fileFrom, fileto, StandardCopyOption.REPLACE_EXISTING);
    }

    public void move(final DataPackType<?> type, final ResourceKey from, final ResourceKey to) throws IOException {
        final Path fileFrom = this.packFile(type, Objects.requireNonNull(from, "from"));
        final Path fileto = this.packFile(type, Objects.requireNonNull(to, "to"));
        Files.createDirectories(fileto.getParent());
        Files.copy(fileFrom, fileto, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public List<ResourceKey> list(final DataPackType<?> type) {
        final List<ResourceKey> packEntries = new ArrayList<>();
        final Path packDir = this.packDir((SpongeDataPackType<?>) type);
        if (!Files.isDirectory(packDir)) {
            return packEntries;
        }
        try (final Stream<Path> namespaces = Files.walk(packDir, 1)) {
            namespaces.filter(Files::isDirectory).forEach(pluginDirectory -> {
                final Path dimensionPath = pluginDirectory.resolve("dimension");
                if (Files.isDirectory(dimensionPath)) {
                    try (final Stream<Path> pluginTemplates = Files.walk(dimensionPath, 1)) {
                        pluginTemplates.filter(file -> file.toString().endsWith(".json"))
                                       .map(file -> SpongeDataPackManager.keyFor(pluginDirectory, file))
                                       .forEach(packEntries::add);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return packEntries;
    }

    private static ResourceKey keyFor(Path namespaceDir, Path file) {
        final String namespace = namespaceDir.getFileName().toString();
        final String value = FilenameUtils.removeExtension(file.getFileName().toString());
        return ResourceKey.of(namespace, value);
    }

    private Path packFile(final DataPackType<?> packType, final ResourceKey key) {
        // TODO incorrect for TagTemplate
        return this.packDir((SpongeDataPackType<?>) packType)
                .resolve("data")
                .resolve(key.namespace())
                .resolve(((SpongeDataPackType<?>) packType).dir())
                .resolve(key.value() + ".json");
    }

    private Path packDir(final SpongeDataPackType<?> packType) {
        return this.packsDir.resolve(this.packName(packType));
    }

    private String packName(final SpongeDataPackType<?> type) {
        return "plugin_" + type.dir().replace("/", "_");
    }
}
