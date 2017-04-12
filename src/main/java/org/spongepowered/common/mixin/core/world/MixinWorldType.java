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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CaseFormat;
import com.google.common.base.Objects;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.FlatGeneratorInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.persistence.JsonDataFormat;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.world.GeneratorTypeRegistryModule;

import java.io.IOException;

@NonnullByDefault
@Mixin(WorldType.class)
public abstract class MixinWorldType implements GeneratorType {

    @Shadow @Final private String worldType;
    @Shadow @Final private int worldTypeId;

    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At("RETURN"))
    private void onConstructSpongeRegister(int id, String name, CallbackInfo callbackInfo) {
        // Ensures that new world types are automatically registered with the registry module
        GeneratorTypeRegistryModule.getInstance().registerAdditionalCatalog(this);
    }


    @Override
    public String getId() {
        return SpongeImplHooks.getModIdFromClass(this.getClass()) + ":" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this.worldType);
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
            return new MemoryDataContainer().set(DataQueries.WORLD_CUSTOM_SETTINGS, defaultSettings);
        }
        if ((Object) this == WorldType.CUSTOMIZED) {
            // They easiest way to go from ChunkProviderSettings to DataContainer is via json and NBT
            try {
                return JsonDataFormat.serialize(ChunkProviderSettings.Factory.JSON_ADAPTER, new ChunkProviderSettings.Factory());
            } catch (JsonParseException | IOException e) {
                throw new AssertionError("Failed to serialize default settings of CUSTOMIZED world type", e);
            }
        }

        return new MemoryDataContainer();
    }

    @Override
    public WorldGenerator createGenerator(World world) {
        checkNotNull(world);
        return ((IMixinWorldServer) world).createWorldGenerator(getGeneratorSettings());
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
        return this.getName().equals(other.getName()) && this.worldTypeId == other.getWorldTypeID();
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
