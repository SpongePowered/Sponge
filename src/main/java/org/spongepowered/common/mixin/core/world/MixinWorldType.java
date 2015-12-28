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
package org.spongepowered.common.mixin.core.world;

import com.google.common.base.Objects;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.FlatGeneratorInfo;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.translator.ConfigurateTranslator;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.interfaces.world.IMixinWorldType;
import org.spongepowered.common.util.persistence.NbtTranslator;
import org.spongepowered.common.world.gen.SpongeGenerationPopulator;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Optional;

@NonnullByDefault
@Mixin(WorldType.class)
public abstract class MixinWorldType implements GeneratorType, IMixinWorldType {

    @Shadow private String worldType;
    @Shadow private int worldTypeId;

    @Override
    public String getId() {
        return this.worldType;
    }

    @Override
    public String getName() {
        return this.worldType;
    }

    @Override
    public DataContainer getGeneratorSettings() {
        // Minecraft stores the generator settings as a string. For the flat
        // world, they use a custom format, for WorldType.CUSTOMIZED they use
        // a serialized JSON string
        if ((Object) this == WorldType.FLAT) {
            String defaultSettings = FlatGeneratorInfo.getDefaultFlatGenerator().toString();
            return new MemoryDataContainer().set(CUSTOM_SETTINGS, defaultSettings);
        }
        if ((Object) this == WorldType.CUSTOMIZED) {
            // They easiest way to go from ChunkProviderSettings to
            // DataContainer
            // is via json and NBT
            try {
                String jsonString = ChunkProviderSettings.Factory.jsonToFactory("").toString();
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(jsonString);
                return NbtTranslator.getInstance().translateFrom(nbt);
            } catch (NBTException e) {
                AssertionError error = new AssertionError("Failed to parse default settings of CUSTOMIZED world type");
                error.initCause(e);
                throw error;
            }
        }
        return new MemoryDataContainer();
    }

    @Override
    public WorldGenerator createGenerator(World world) {
        return this.createGeneratorFromString(world, "");
    }

    @Override
    public SpongeWorldGenerator createGenerator(World world, DataContainer settings) {
        // Minecraft uses a string for world generator settings
        // This string can be a JSON string, or be a string of a custom format

        // Try to convert to custom format
        Optional<String> optCustomSettings = settings.getString(CUSTOM_SETTINGS);
        if (optCustomSettings.isPresent()) {
            return this.createGeneratorFromString(world, optCustomSettings.get());
        }

        final StringWriter writer = new StringWriter();
        try {
            HoconConfigurationLoader.builder().setSink(() -> new BufferedWriter(writer)).build().save(ConfigurateTranslator.instance().
                    translateData(settings));
        } catch (Exception e) {
            SpongeImpl.getLogger().warn("Failed to convert settings contained in [" + settings + "] for type [" + this + "] for world [" + world +
                    "].", e);
        }

        return this.createGeneratorFromString(world, writer.toString());
    }

    @Override
    public SpongeWorldGenerator createGeneratorFromString(World world, String settings) {
        final net.minecraft.world.World mcWorld = (net.minecraft.world.World) world;
        final IChunkProvider chunkProvider = ((IMixinWorldProvider) mcWorld.provider).createChunkGenerator(settings);
        final WorldChunkManager chunkManager = mcWorld.provider.worldChunkMgr;
        return new SpongeWorldGenerator((net.minecraft.world.World) world,
                (BiomeGenerator) chunkManager,
                SpongeGenerationPopulator.of((WorldServer) world, chunkProvider));
    }

    @Override
    public int getMinimumSpawnHeight(net.minecraft.world.World world) {
        int spawnHeight = 64;

        if (world.getWorldType() == WorldType.FLAT) {
            spawnHeight = 4;
        } else if (world.getWorldType() == GeneratorTypes.THE_END) {
            spawnHeight = 50;
        }
        return spawnHeight;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getName().hashCode();
        result = prime * result + this.worldTypeId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorldType)) {
            return false;
        }

        final WorldType other = (WorldType) obj;
        return this.getName().equals(other.getWorldTypeName()) && this.worldTypeId == other.getWorldTypeID();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .add("settings", getGeneratorSettings())
                .toString();
    }
}
