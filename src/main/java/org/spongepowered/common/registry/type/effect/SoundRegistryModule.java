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
package org.spongepowered.common.registry.type.effect;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.Locale;

import javax.annotation.Nullable;

@RegisterCatalog(SoundTypes.class)
public final class SoundRegistryModule extends AbstractCatalogRegistryModule<SoundType> implements AdditionalCatalogRegistryModule<SoundType> {


    public static SoundRegistryModule inst() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerDefaults() {
        for (ResourceLocation key : SoundEvent.REGISTRY.getKeys()) {
            this.map.put((CatalogKey) (Object) key, (SoundType) SoundEvent.REGISTRY.getObject(key));
        }
    }

    @Override
    protected String marshalFieldKey(String key) {
        return key.replace('.', '_');
    }

    @Nullable
    private SoundType getInternal(final String id) {
        final SoundType sound = this.map.get(id);
        if (sound != null) {
            return sound;
        }
        return this.map.get(id.replace('_', '.'));
    }

    @Override
    public void registerAdditionalCatalog(SoundType extraCatalog) {
        ResourceLocation catalogId = new ResourceLocation(checkNotNull(extraCatalog).getKey().toString().toLowerCase(Locale.ENGLISH));
        if (!this.map.containsKey((CatalogKey) (Object) catalogId)) {
            this.map.put((CatalogKey) (Object) catalogId, extraCatalog);
        }
    }

    private static final class Holder {

        static final SoundRegistryModule INSTANCE = new SoundRegistryModule();
    }
}
