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
package org.spongepowered.common.data.persistence;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.data.persistence.schematic.SchematicUpdater1_to_2;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.mixin.core.server.MinecraftServerAccessor;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PairStream;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer;
import org.spongepowered.common.util.gen.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.world.schematic.BimapPalette;
import org.spongepowered.common.world.schematic.BlockPaletteWrapper;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.SpongeSchematicBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class SchematicTranslator implements DataTranslator<Schematic> {

    private static final SchematicTranslator INSTANCE = new SchematicTranslator();
    private static final TypeToken<Schematic> TYPE_TOKEN = TypeToken.of(Schematic.class);

    private static final ConcurrentSkipListSet<String> MISSING_MOD_IDS = new ConcurrentSkipListSet<>();

    private static final DataContentUpdater V1_TO_2 = new SchematicUpdater1_to_2();

    @Nullable private static DataFixer VANILLA_FIXER;

    public static SchematicTranslator get() {
        return INSTANCE;
    }

    private SchematicTranslator() {

    }

    @Override
    public String getId() {
        return "sponge:schematic";
    }

    @Override
    public String getName() {
        return "Sponge Schematic Translator";
    }

    @Override
    public TypeToken<Schematic> getToken() {
        return TYPE_TOKEN;
    }

    @Override
    public Schematic translate(DataView unprocessed) throws InvalidDataException {
        if (VANILLA_FIXER == null) {
            VANILLA_FIXER = ((MinecraftServerAccessor) SpongeImpl.getServer()).accessor$getDataFixer();
        }
        final int version = unprocessed.getInt(Constants.Sponge.Schematic.VERSION).get();
        // TODO version conversions

        if (version > Constants.Sponge.Schematic.CURRENT_VERSION) {
            throw new InvalidDataException(String.format("Unknown schematic version %d (current version is %d)", version, Constants.Sponge.Schematic.CURRENT_VERSION));
        } else if (version == 1) {
            unprocessed = V1_TO_2.update(unprocessed);
        }
        { // Strictly for loading tile entities when the format wasn't finalized yet.
            final List<DataView> dataViews = unprocessed.getViewList(Constants.Sponge.Schematic.Versions.V1_TILE_ENTITY_DATA).orElse(null);
            if (dataViews != null) {
                unprocessed.remove(Constants.Sponge.Schematic.Versions.V1_TILE_ENTITY_DATA);
                unprocessed.set(Constants.Sponge.Schematic.BLOCKENTITY_DATA, dataViews);
            }
        }
        final int dataVersion = unprocessed.getInt(Constants.Sponge.Schematic.DATA_VERSION).get();
        // DataFixer will be able to upgrade entity and tile entity data if and only if we're running a valid server and
        // the data version is outdated.
        final boolean needsFixers = dataVersion < Constants.MINECRAFT_DATA_VERSION && VANILLA_FIXER != null;
        final DataView updatedView = unprocessed;

        final DataView metadata = updatedView.getView(Constants.Sponge.Schematic.METADATA).orElse(null);
        if (metadata != null) {
            final Optional<DataView> dot_data = metadata.getView(DataQuery.of("."));
            if (dot_data.isPresent()) {
                final DataView data = dot_data.get();
                for (final DataQuery key : data.getKeys(false)) {
                    if (!metadata.contains(key)) {
                        metadata.set(key, data.get(key).get());
                    }
                }
            }
        }
        if (metadata != null) {
            final String schematicName = metadata.getString(Constants.Sponge.Schematic.NAME).orElse("unknown");
            metadata.getStringList(Constants.Sponge.Schematic.REQUIRED_MODS).ifPresent(mods -> {
                for (final String modId : mods) {
                    if (!Sponge.getPluginManager().getPlugin(modId).isPresent()) {
                        if (MISSING_MOD_IDS.add(modId)) {
                            SpongeImpl.getLogger().warn("When attempting to load the Schematic: " + schematicName + " there is a missing modid: " + modId + " some blocks/tiles/entities may not load correctly.");
                        }
                    }
                }
            });
        }

        // TODO error handling for these optionals
        final int width = updatedView.getShort(Constants.Sponge.Schematic.WIDTH).get();
        final int height = updatedView.getShort(Constants.Sponge.Schematic.HEIGHT).get();
        final int length = updatedView.getShort(Constants.Sponge.Schematic.LENGTH).get();
        if (width > Constants.Sponge.Schematic.MAX_SIZE || height > Constants.Sponge.Schematic.MAX_SIZE || length > Constants.Sponge.Schematic.MAX_SIZE) {
            throw new InvalidDataException(String.format("Schematic is larger than maximum allowable size (found: (%d, %d, %d) max: (%d, %<d, %<d)",
                    width, height, length, Constants.Sponge.Schematic.MAX_SIZE));
        }

        final int[] offset = (int[]) updatedView.get(Constants.Sponge.Schematic.OFFSET).orElse(new int[3]);
        if (offset.length != 3) {
            throw new InvalidDataException("Schematic offset was not of length 3");
        }
        final Palette<BlockState> palette;
        final Optional<DataView> paletteData = updatedView.getView(Constants.Sponge.Schematic.PALETTE);
        final int palette_max = updatedView.getInt(Constants.Sponge.Schematic.PALETTE_MAX).orElse(0xFFFF);
        if (paletteData.isPresent()) {
            // If we had a default palette_max we don't want to allocate all
            // that space for nothing so we use a sensible default instead
            final BimapPalette<BlockState> bimap = new BimapPalette<>(PaletteTypes.LOCAL_BLOCKS, palette_max != 0xFFFF ? palette_max : 64);
            // TODO - 1.13 remove the wrapper.
            palette = new BlockPaletteWrapper(bimap, org.spongepowered.api.world.schematic.BlockPaletteTypes.LOCAL);
            final DataView paletteMap = paletteData.get();
            final Set<DataQuery> paletteKeys = paletteMap.getKeys(false);
            for (final DataQuery key : paletteKeys) {
                final BlockState state = Sponge.getRegistry().getType(BlockState.class, key.getParts().get(0)).orElseGet(BlockTypes.BEDROCK::getDefaultState);
                bimap.assign(state, paletteMap.getInt(key).get());
            }
        } else {
            palette = GlobalPalette.getBlockPalette();
        }

        final Palette<BiomeType> biomePalette;
        final Optional<DataView> biomePaletteData = updatedView.getView(Constants.Sponge.Schematic.BIOME_PALETTE);
        final int biome_max = updatedView.getInt(Constants.Sponge.Schematic.BIOME_PALETTE_MAX).orElse(0xFFFF);
        if (biomePaletteData.isPresent()) {
            final BimapPalette<BiomeType> bimap = new BimapPalette<>(PaletteTypes.LOCAL_BIOMES, biome_max != 0xFFF ? palette_max : 64);
            biomePalette = bimap;
            final DataView biomeMap = biomePaletteData.get();
            final Set<DataQuery> biomeKeys = biomeMap.getKeys(false);
            for (final DataQuery biomeKey : biomeKeys) {
                final BiomeType biome = Sponge.getRegistry().getType(BiomeType.class, biomeKey.getParts().get(0)).get();
                bimap.assign(biome, biomeMap.getInt(biomeKey).get());
            }
        } else {
            biomePalette = GlobalPalette.getBiomePalette();
        }

        final SpongeSchematicBuilder builder = new SpongeSchematicBuilder();
        builder.blockPalette(palette);

        final MutableBlockVolume buffer =
                new ArrayMutableBlockBuffer(palette, new Vector3i(-offset[0], -offset[1], -offset[2]), new Vector3i(width, height, length));

        final byte[] blockdata = (byte[]) updatedView.get(Constants.Sponge.Schematic.BLOCK_DATA).orElseThrow(() -> new InvalidDataException("Missing BlockData for Schematic"));
        int index = 0;
        int i = 0;
        int value = 0;
        int varint_length = 0;
        while (i < blockdata.length) {
            value = 0;
            varint_length = 0;

            while (true) {
                value |= (blockdata[i] & 127) << (varint_length++ * 7);
                if (varint_length > 5) {
                    throw new RuntimeException("VarInt too big (probably corrupted data)");
                }
                if ((blockdata[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }
            // index = (y * length + z) * width + x
            final int y = index / (width * length);
            final int z = (index % (width * length)) / width;
            final int x = (index % (width * length)) % width;
            final BlockState state = palette.get(value).get();
            buffer.setBlock(x - offset[0], y - offset[1], z - offset[2], state);

            index++;
        }
        builder.blocks(buffer);

        updatedView.get(Constants.Sponge.Schematic.BIOME_DATA).ifPresent(biomesObj -> {
            final MutableBiomeVolume biomeBuffer = new ByteArrayMutableBiomeBuffer(biomePalette, new Vector3i(-offset[0], -offset[1], -offset[2]), new Vector3i(width, height, length));
            final byte[] biomes = (byte[]) biomesObj;
            int biomeIndex = 0;
            int biomeJ= 0;
            int bVal = 0;
            int varIntLength = 0;
            while (biomeJ < biomes.length) {
                bVal = 0;
                varIntLength = 0;

                while (true) {
                    bVal |= (biomes[biomeJ] & 127) << (varIntLength++ * 7);
                    if (varIntLength > 5) {
                        throw new RuntimeException("VarInt too big (probably corrupted data)");
                    }
                    if (((biomes[biomeJ] & 128) != 128)) {
                        biomeJ++;
                        break;
                    }
                    biomeJ++;
                }
                final int z = (biomeIndex % (width * length)) / width;
                final int x = (biomeIndex % (width * length)) % width;
                final BiomeType type = biomePalette.get(bVal).get();
                biomeBuffer.setBiome(x - offset[0], 0, z - offset[2], type);

                biomeIndex++;
            }
            builder.biomes(biomeBuffer);
        });

        final Map<Vector3i, TileEntityArchetype> tiles = Maps.newHashMap();

        updatedView.getViewList(Constants.Sponge.Schematic.BLOCKENTITY_DATA)
            .ifPresent(tileData ->
                tileData.forEach(tile -> {
                        final int[] pos = (int[]) tile.get(Constants.Sponge.Schematic.BLOCKENTITY_POS).get();
                        tile.getString(Constants.Sponge.Schematic.BLOCKENTITY_ID)
                            .map(TileEntityTypeRegistryModule.getInstance()::getById)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .ifPresent(type -> {
                                final DataView upgraded;
                                if (needsFixers) {
                                    CompoundNBT tileNbt = NbtTranslator.getInstance().translate(tile);
                                    tileNbt = VANILLA_FIXER.func_188251_a(FixTypes.BLOCK_ENTITY, tileNbt, version);
                                    upgraded = NbtTranslator.getInstance().translate(tileNbt);
                                } else {
                                    upgraded = tile;
                                }

                                final TileEntityArchetype archetype = new SpongeTileEntityArchetypeBuilder()
                                    .state(buffer.getBlock(pos[0] - offset[0], pos[1] - offset[1], pos[2] - offset[2]))
                                    .tileData(upgraded)
                                    .tile(type)
                                    .build();
                                final Vector3i position = new Vector3i(pos[0] - offset[0], pos[1] - offset[1], pos[2] - offset[2]);
                                tiles.put(position, archetype);
                            });
                    }
                )
            );
        builder.tiles(tiles);
        final ArrayList<EntityArchetype> entityArchetypes = new ArrayList<>();
        updatedView.getViewList(Constants.Sponge.Schematic.ENTITIES).map(List::stream)
            .ifPresent(stream -> {
                final Stream<DataView>
                    viewStream =
                    stream.filter(entity -> entity.contains(Constants.Sponge.Schematic.ENTITIES_POS, Constants.Sponge.Schematic.ENTITIES_ID));
                PairStream
                    .from(viewStream,
                        (entity) -> entity.getString(Constants.Sponge.Schematic.ENTITIES_ID)
                            .map(EntityTypeRegistryModule.getInstance()::getById))
                    .filter(((view, entityType) -> entityType.map(Optional::get).isPresent()))
                    .filter((view, entityType) -> !Player.class.isAssignableFrom(entityType.map(Optional::get).get().getEntityClass()))
                    .map((view, type) -> {
                        final DataView upgraded;
                        if (needsFixers) {
                            CompoundNBT entityNbt = NbtTranslator.getInstance().translate(view);
                            entityNbt = VANILLA_FIXER.func_188251_a(FixTypes.ENTITY, entityNbt, version);
                            upgraded = NbtTranslator.getInstance().translate(entityNbt);
                        } else {
                            upgraded = view;
                        }
                        return new SpongeEntityArchetypeBuilder()
                            .type(type.get().get())
                            .entityData(upgraded)
                            .build();
                    })
                    .forEach(entityArchetypes::add);
            });
        if (!entityArchetypes.isEmpty()) {
            builder.entities(entityArchetypes);
        }

        if (metadata != null) {
            final DataContainer meta = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
            for (final DataQuery key : metadata.getKeys(false)) {
                meta.set(key, metadata.get(key).get());
            }
            builder.metadata(meta);
        }
        return builder.build();
    }

    @Override
    public DataContainer translate(final Schematic schematic) throws InvalidDataException {
        final DataContainer data = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        addTo(schematic, data);
        return data;
    }

    @Override
    public DataView addTo(final Schematic schematic, final DataView data) {
        final int xMin = schematic.getBlockMin().getX();
        final int yMin = schematic.getBlockMin().getY();
        final int zMin = schematic.getBlockMin().getZ();
        final int width = schematic.getBlockSize().getX();
        final int height = schematic.getBlockSize().getY();
        final int length = schematic.getBlockSize().getZ();
        if (width > Constants.Sponge.Schematic.MAX_SIZE || height > Constants.Sponge.Schematic.MAX_SIZE || length > Constants.Sponge.Schematic.MAX_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "Schematic is larger than maximum allowable size (found: (%d, %d, %d) max: (%d, %<d, %<d)", width, height, length, Constants.Sponge.Schematic.MAX_SIZE));
        }
        data.set(Constants.Sponge.Schematic.WIDTH, width);
        data.set(Constants.Sponge.Schematic.HEIGHT, height);
        data.set(Constants.Sponge.Schematic.LENGTH, length);

        data.set(Constants.Sponge.Schematic.VERSION, Constants.Sponge.Schematic.CURRENT_VERSION);
        data.set(Constants.Sponge.Schematic.DATA_VERSION, Constants.MINECRAFT_DATA_VERSION);
        for (final DataQuery metaKey : schematic.getMetadata().getKeys(false)) {
            data.set(Constants.Sponge.Schematic.METADATA.then(metaKey), schematic.getMetadata().get(metaKey).get());
        }
        final Set<String> requiredMods = new HashSet<>();

        final int[] offset = new int[] {-xMin, -yMin, -zMin};
        data.set(Constants.Sponge.Schematic.OFFSET, offset);

        final Palette<BlockState> palette = schematic.getPalette();
        try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length)) {
            for (int y = 0; y < height; y++) {
                final int y0 = yMin + y;
                for (int z = 0; z < length; z++) {
                    final int z0 = zMin + z;
                    for (int x = 0; x < width; x++) {
                        final int x0 = xMin + x;
                        final BlockState state = schematic.getBlock(x0, y0, z0);
                        writeIdToBuffer(buffer, palette.getOrAssign(state));
                    }
                }
            }

            data.set(Constants.Sponge.Schematic.BLOCK_DATA, buffer.toByteArray());
        } catch (IOException e) {
            // should never reach here
        }

        final Palette<BiomeType> biomePalette = schematic.getBiomePalette();
        schematic.getBiomes().ifPresent(biomes -> {
            try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * length)) {
                for (int z = 0; z < length; z++) {
                    final int z0 = zMin + z;
                    for (int x = 0; x < width; x++) {
                        final int x0 = xMin + x;
                        final BiomeType state = biomes.getBiome(x0, 0, z0);
                        writeIdToBuffer(buffer, biomePalette.getOrAssign(state));
                    }

                }

                data.set(Constants.Sponge.Schematic.BLOCK_DATA, buffer.toByteArray());
            } catch (IOException e) {
                // Should never reach here.
            }

        });

        if (palette.getType() == PaletteTypes.LOCAL_BLOCKS) {
            final DataQuery paletteQuery = Constants.Sponge.Schematic.PALETTE;
            for (final BlockState state : palette.getEntries()) {
                // getOrAssign to skip the optional, it will never assign
                data.set(paletteQuery.then(state.getId()), palette.getOrAssign(state));
                final String modId = state.getType().getId().split(":")[0];
                if (!"minecraft".equals(modId) && modId != null && !modId.isEmpty()) {
                    requiredMods.add(modId);
                }
            }
            data.set(Constants.Sponge.Schematic.PALETTE_MAX, palette.getHighestId());
        }
        if (biomePalette.getType() == PaletteTypes.LOCAL_BIOMES) {
            final DataQuery paletteQuery = Constants.Sponge.Schematic.BIOME_PALETTE;
            for (final BiomeType biomeType : biomePalette.getEntries()) {
                data.set(paletteQuery.then(biomeType.getId()), biomePalette.getOrAssign(biomeType));
                final String modId = biomeType.getId().split(":")[0];
                if (!"minecraft".equals(modId) && modId != null && !modId.isEmpty()) {
                    requiredMods.add(modId);
                }
            }
            data.set(Constants.Sponge.Schematic.BIOME_PALETTE_MAX, biomePalette.getHighestId());
        }

        final List<DataView> tileEntities = Lists.newArrayList();
        for (final Map.Entry<Vector3i, TileEntityArchetype> entry : schematic.getTileEntityArchetypes().entrySet()) {
            final Vector3i pos = entry.getKey();
            final DataContainer tiledata = entry.getValue().getTileData();
            final int[] apos = new int[] {pos.getX() - xMin, pos.getY() - yMin, pos.getZ() - zMin};
            tiledata.set(Constants.Sponge.Schematic.BLOCKENTITY_POS, apos);
            final SpongeTileEntityType tileEntityType = (SpongeTileEntityType) entry.getValue().getTileEntityType();

            final String modId = tileEntityType.getId().split(":")[0];
            if ("minecraft".equalsIgnoreCase(modId) && !"minecraft".equalsIgnoreCase(tileEntityType.getModId())) {
                if (!"sponge".equalsIgnoreCase(tileEntityType.getModId())) {
                    requiredMods.add(modId);
                }
            }
            if (!"minecraft".equalsIgnoreCase(modId) && modId != null && !modId.isEmpty()) {
                requiredMods.add(modId);
            }
            tileEntities.add(tiledata);
        }
        data.set(Constants.Sponge.Schematic.BLOCKENTITY_DATA, tileEntities);

        final List<DataView> entities = Lists.newArrayList();
        for (final EntityArchetype entityArchetype : schematic.getEntityArchetypes()) {
            final DataContainer entityData = entityArchetype.getEntityData();
            entities.add(entityData);
            final SpongeEntityType type = (SpongeEntityType) entityArchetype.getType();
            final String modId = type.getId().split(":")[0];
            if ("minecraft".equalsIgnoreCase(modId) && !"minecraft".equalsIgnoreCase(type.getModId())) {
                if (!"sponge".equalsIgnoreCase(type.getModId())) {
                    requiredMods.add(modId);
                }
            }
            if (!"minecraft".equals(modId) && modId != null && !modId.isEmpty()) {
                requiredMods.add(modId);
            }
        }
        data.set(Constants.Sponge.Schematic.ENTITIES, entities);

        if (!requiredMods.isEmpty()) {
            data.set(Constants.Sponge.Schematic.METADATA.then(Constants.Sponge.Schematic.REQUIRED_MODS), requiredMods);
        }

        return data;
    }

     private void writeIdToBuffer(final ByteArrayOutputStream buffer, final int orAssign) {
        int id = orAssign;

        while ((id & -128) != 0) {
            buffer.write(id & 127 | 128);
            id >>>= 7;
        }
        buffer.write(id);
    }
}
