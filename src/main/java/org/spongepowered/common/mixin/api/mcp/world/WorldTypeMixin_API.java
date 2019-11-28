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
package org.spongepowered.common.mixin.api.mcp.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonParseException;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.FlatGeneratorInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.data.persistence.JsonDataFormat;
import org.spongepowered.common.util.Constants;

import java.io.IOException;

@NonnullByDefault
@Mixin(WorldType.class)
public abstract class WorldTypeMixin_API implements GeneratorType {

    @Shadow @Final private String name;

    @Override
    public String getId() {
        return SpongeImplHooks.getModIdFromClass(this.getClass()) + ":" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this.name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public DataContainer getGeneratorSettings() {
        // Minecraft stores the generator settings as a string. For the flat
        // world, they use a custom format, for WorldType.CUSTOMIZED they use
        // a serialized JSON string
        if (((WorldType) (Object) this) == WorldType.FLAT) {
            String defaultSettings = FlatGeneratorInfo.getDefaultFlatGenerator().toString();
            return DataContainer.createNew().set(Constants.Sponge.World.WORLD_CUSTOM_SETTINGS, defaultSettings);
        }
        if (((WorldType) (Object) this) == WorldType.CUSTOMIZED) {
            // They easiest way to go from ChunkProviderSettings to DataContainer is via json and NBT
            try {
                return JsonDataFormat.serialize(ChunkGeneratorSettings.Factory.field_177901_a, new ChunkGeneratorSettings.Factory());
            } catch (JsonParseException | IOException e) {
                throw new AssertionError("Failed to serialize default settings of CUSTOMIZED world type", e);
            }
        }

        return DataContainer.createNew();
    }

    @Override
    public WorldGenerator createGenerator(World world) {
        checkNotNull(world);
        return ((WorldServerBridge) world).bridge$createWorldGenerator(getGeneratorSettings());
    }

}
