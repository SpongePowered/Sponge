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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.datapack.DataPack;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class SpongeDataPackManager implements DataPackManager {

    private static final String PATH_SUFFIX = ".json";
    private static final int PATH_SUFFIX_LENGTH = ".json".length();

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

        this.findPack(DataPackTypes.BIOME, ResourceKey.of("biometest", "override"));
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
        this.registerAndSerializePack(DataPackTypes.BLOCK_TAG, reloadablePacks);
        return reloadablePacks;
    }

    private <T extends DataPackEntry<T>> List<T> callRegisterDataPackValueEvent(final SpongeDataPackType<T> type) {
        final RegisterDataPackValueEventImpl<T> event = new RegisterDataPackValueEventImpl<>(Cause.of(EventContext.empty(), SpongeCommon.game()), SpongeCommon.game(), type);
        SpongeCommon.post(event);
        return event.serializables();
    }

    @SuppressWarnings("unchecked")
    private <T extends DataPackEntry<T>> void registerAndSerializePack(final DataPackType<T> type, final Collection<String> reloadablePacks) {
        final List<T> packEntries = this.callRegisterDataPackValueEvent((SpongeDataPackType<T>) type);
        if (packEntries.isEmpty()) {
            return;
        }
        this.serializePack(reloadablePacks, packEntries);
    }

    private <T extends DataPackEntry<T>> void serializePack(final Collection<String> reloadablePacks, final List<T> packEntries) {
        for (final T packEntry : packEntries) {
            final SpongeDataPack<T> implPack = (SpongeDataPack<T>) packEntry.pack();
            final String fullPackName = "file/" + implPack.name();
            try {
                final boolean success = implPack.type().packSerializer().serialize(implPack, this.packDir(implPack), packEntries);
                DataPackSerializer.writePackMetadata(implPack, this.packDir(implPack), false);
                if (success && implPack.type().reloadable()) {
                    reloadablePacks.add(fullPackName);
                } else {
                    reloadablePacks.remove(fullPackName);
                }
            } catch (final IOException e) {
                reloadablePacks.remove(fullPackName);
                SpongeCommon.logger().error(e);
            }
        }

    }


    @Override
    public <T extends DataPackEntry<T>> CompletableFuture<Boolean> save(final T entry) {
        // TODO this is actually blocking
        this.serializePack(new ArrayList<>(), List.of(entry));
        return CompletableFuture.completedFuture(((SpongeDataPackType)entry.pack().type()).reloadable());
    }

    @Override
    public <T extends DataPackEntry<T>> CompletableFuture<Optional<T>> load(final DataPack<T> pack, final ResourceKey key) {
        Objects.requireNonNull(key, "key");
        final SpongeDataPack<T> implPack = (SpongeDataPack<T>) pack;
        final Path file = this.packFile(implPack, key);
        if (Files.exists(file)) {
            try {
                // TODO this is actually blocking
                final T deserialized = implPack.type().packSerializer().deserialize(implPack, file, key);
                return CompletableFuture.completedFuture(Optional.ofNullable(deserialized));
            } catch (IOException ex) {
                return FutureUtil.completedWithException(ex);
            }
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public boolean exists(final DataPack<?> pack, final ResourceKey key) {
        final SpongeDataPack<?> packImpl = (SpongeDataPack<?>) pack;
        final Path file = this.packFile(packImpl, Objects.requireNonNull(key, "key"));
        return Files.exists(file);
    }

    @Override
    public boolean delete(final DataPack<?> pack, final ResourceKey key) throws IOException {
        final SpongeDataPack<?> packImpl = (SpongeDataPack<?>) pack;
        final Path file = this.packFile(packImpl, Objects.requireNonNull(key, "key"));
        return Files.deleteIfExists(file);
    }

    public void copy(final DataPack<?> pack, final ResourceKey from, final ResourceKey to) throws IOException {
        final SpongeDataPack<?> packImpl = (SpongeDataPack<?>) pack;
        final Path fileFrom = this.packFile(packImpl, Objects.requireNonNull(from, "from"));
        final Path fileto = this.packFile(packImpl, Objects.requireNonNull(to, "to"));
        Files.createDirectories(fileto.getParent());
        Files.copy(fileFrom, fileto, StandardCopyOption.REPLACE_EXISTING);
    }

    public void move(final DataPack<?> pack, final ResourceKey from, final ResourceKey to) throws IOException {
        final SpongeDataPack<?> packImpl = (SpongeDataPack<?>) pack;
        final Path fileFrom = this.packFile(packImpl, Objects.requireNonNull(from, "from"));
        final Path fileto = this.packFile(packImpl, Objects.requireNonNull(to, "to"));
        Files.createDirectories(fileto.getParent());
        Files.copy(fileFrom, fileto, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public List<ResourceKey> list(final DataPack<?> pack) {
        final List<ResourceKey> packEntries = new ArrayList<>();
        final Path packDir = this.packDir(pack);
        if (!Files.isDirectory(packDir)) {
            return packEntries;
        }
        try (final Stream<Path> namespaces = Files.walk(packDir.resolve("data"), 1)) {
            namespaces.filter(Files::isDirectory).forEach(namespaceDir -> {
                final Path typeDir = namespaceDir.resolve(((SpongeDataPack<?>) pack).type().dir());
                if (Files.isDirectory(typeDir)) {
                    try (final Stream<Path> pluginTemplates = Files.walk(typeDir, 1)) {
                        pluginTemplates.filter(file -> file.toString().endsWith(".json"))
                                .map(file -> SpongeDataPackManager.keyFor(namespaceDir, file))
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

    @Override
    public <T extends DataPackEntry<T>> Map<DataPack<T>, Collection<ResourceKey>> find(final DataPackType<T> packType) {
        final ResourceManager manager = this.server.getResourceManager();
        Map<String, List<ResourceLocation>> resources = new HashMap<>();
        Map<String, String> descriptions = new HashMap<>();
        final SpongeDataPackType<T> typeImpl = (SpongeDataPackType<T>) packType;
        manager.listPacks().forEach(pack -> {
            final String packName = pack.getName();
            descriptions.computeIfAbsent(packName, k -> this.packDescription(pack));
            for (final String namespace : pack.getNamespaces(PackType.SERVER_DATA)) {
                final Collection<ResourceLocation> locs = pack.getResources(PackType.SERVER_DATA, namespace, typeImpl.dir(), loc -> true);
                if (!locs.isEmpty()) {
                    resources.computeIfAbsent(packName, k -> new ArrayList<>()).addAll(locs);
                }
            }
        });

        final Map<DataPack<T>, Collection<ResourceKey>> map = new HashMap<>();
        for (final Map.Entry<String, List<ResourceLocation>> entry : resources.entrySet()) {
            map.put(packType.pack(entry.getKey(), descriptions.get(entry.getKey())), entry.getValue().stream().map(loc -> {
                final String path = loc.getPath();
                final String value = path.substring(typeImpl.dir().length(), path.length() - PATH_SUFFIX_LENGTH);
                return ResourceKey.of(loc.getNamespace(), value);
            }).toList());
        }
        return map;
    }

    @Override
    public <T extends DataPackEntry<T>> Optional<DataPack<T>> findPack(final DataPackType<T> packType, final ResourceKey key) {
        final ResourceKey compareKey = ResourceKey.of(key.namespace(), ((SpongeDataPackType<T>) packType).dir() + "/" + key.value() + PATH_SUFFIX);
        for (final Resource resource : this.server.getResourceManager().listResources(((SpongeDataPackType) packType).dir(), loc -> loc.equals(compareKey)).values()) {
            return Optional.of(packType.pack(resource.sourcePackId(), "N/A"));
        }
        return Optional.empty();
    }

    private String packDescription(final PackResources pack) {
        try {
            return pack.getMetadataSection(PackMetadataSection.SERIALIZER).getDescription().getString();
        } catch (IOException e) {
            return "N/A";
        }
    }

    private static ResourceKey keyFor(Path namespaceDir, Path file) {
        final String namespace = namespaceDir.getFileName().toString();
        final String value = FilenameUtils.removeExtension(file.getFileName().toString());
        return ResourceKey.of(namespace, value);
    }

    private <T extends DataPackEntry<T>> Path packFile(final SpongeDataPack<T> pack, final ResourceKey key) {
        // TODO TagTemplate changes based on which registry the tag is for
        // return pack.packSerializer().packEntryFile(pack, ?, this.packDir(pack));
        return this.packDir(pack)
                .resolve("data")
                .resolve(key.namespace())
                .resolve(pack.type().dir())
                .resolve(key.value() + ".json");
    }

    private Path packDir(final DataPack<?> pack) {
        return this.packsDir.resolve(pack.name());
    }

}
