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
package org.spongepowered.common.registry.type.data;

import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.type.InstrumentTypes;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeInstrumentType;
import org.spongepowered.common.registry.type.AbstractPrefixCheckCatalogRegistryModule;

import java.util.HashMap;
import java.util.Map;

@RegisterCatalog(InstrumentTypes.class)
public class InstrumentTypeRegistryModule extends AbstractPrefixCheckCatalogRegistryModule<InstrumentType>
        implements AlternateCatalogRegistryModule<InstrumentType> {

    public static InstrumentTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private InstrumentTypeRegistryModule() {
        super("minecraft");
    }

    @Override
    public Map<String, InstrumentType> provideCatalogMap() {
        final Map<String, InstrumentType> mappings = new HashMap<>();
        for (Map.Entry<String, InstrumentType> entry : this.catalogTypeMap.entrySet()) {
            mappings.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return mappings;
    }

    @Override
    public void registerDefaults() {
        try {
            Class.forName("net.minecraft.block.BlockNote"); // Initialize the class so Instruments is loaded
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        register(new SpongeInstrumentType("minecraft:harp", "Harp", 0));
        register(new SpongeInstrumentType("minecraft:bass_drum", "Bass Drum", 1));
        register(new SpongeInstrumentType("minecraft:snare", "Snare", 2));
        register(new SpongeInstrumentType("minecraft:high_hat", "High Hat", 3));
        register(new SpongeInstrumentType("minecraft:bass_attack", "Bass Attack", 4));
        register(new SpongeInstrumentType("minecraft:flute", "Flute", 5));
        register(new SpongeInstrumentType("minecraft:bell", "Bell", 6));
        register(new SpongeInstrumentType("minecraft:guitar", "Guitar", 7));
        register(new SpongeInstrumentType("minecraft:chime", "Chime", 8));
        register(new SpongeInstrumentType("minecraft:xylophone", "Xylophone", 9));
    }

    private static final class Holder {
        final static InstrumentTypeRegistryModule INSTANCE = new InstrumentTypeRegistryModule();
    }
}
