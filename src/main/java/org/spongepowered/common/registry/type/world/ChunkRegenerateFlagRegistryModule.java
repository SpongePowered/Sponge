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
package org.spongepowered.common.registry.type.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.ChunkRegenerateFlags;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.world.SpongeChunkRegenerateFlag;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class ChunkRegenerateFlagRegistryModule implements RegistryModule {

    @RegisterCatalog(ChunkRegenerateFlags.class)
    private final Map<String, SpongeChunkRegenerateFlag> flags = new LinkedHashMap<>(4);
    private final Int2ObjectMap<SpongeChunkRegenerateFlag> maskedFlags = new Int2ObjectLinkedOpenHashMap<>(4);
    private static ChunkRegenerateFlagRegistryModule INSTANCE = new ChunkRegenerateFlagRegistryModule();

    public static ChunkRegenerateFlagRegistryModule getInstance() {
        return INSTANCE;
    }

    private ChunkRegenerateFlagRegistryModule() {
    }

    public static SpongeChunkRegenerateFlag fromNativeInt(int flag) {
        final SpongeChunkRegenerateFlag spongeChunkRegenerateFlag = getInstance().maskedFlags.get(flag);
        if (spongeChunkRegenerateFlag != null) {
            return spongeChunkRegenerateFlag;
        }
        return (SpongeChunkRegenerateFlag) ChunkRegenerateFlags.ALL;
    }

    @Override
    public void registerDefaults() {
        register(new SpongeChunkRegenerateFlag("NONE".toLowerCase(Locale.ENGLISH), Flags.NONE));
        register(new SpongeChunkRegenerateFlag("ALL".toLowerCase(Locale.ENGLISH), Flags.ALL));
        register(new SpongeChunkRegenerateFlag("CREATE".toLowerCase(Locale.ENGLISH), Flags.CREATE));
        register(new SpongeChunkRegenerateFlag("ENTITIES".toLowerCase(Locale.ENGLISH), Flags.ENTITIES));
        RegistryHelper.mapFields(ChunkRegenerateFlags.class, this.flags);
    }

    private void register(SpongeChunkRegenerateFlag flag) {
        this.maskedFlags.put(flag.getRawFlag(), flag);
        this.flags.put(flag.getName(), flag);
    }

    public Collection<SpongeChunkRegenerateFlag> getValues() {
        return Collections.unmodifiableCollection(this.flags.values());
    }

    public static final class Flags {

        public static final int CREATE                      = 0b00000001;
        public static final int ENTITIES                    = 0b00000010;
        public static final int NONE                        = 0000000000;
        public static final int ALL                         = CREATE | ENTITIES;

    }
}
