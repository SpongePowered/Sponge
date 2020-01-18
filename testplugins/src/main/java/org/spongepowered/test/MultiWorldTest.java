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
package org.spongepowered.test;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.WorldArchetypes;
import org.spongepowered.api.world.gen.GeneratorTypes;
import org.spongepowered.api.world.server.WorldRegistration;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.util.Optional;

@Plugin(id = "multi-world-test", name = "Multi World Test", version = "0.0.0")
public final class MultiWorldTest implements LoadableModule {

    @Override
    public void disable(MessageReceiver src) {
        Sponge.getServer().getWorldManager().getWorld("temp").ifPresent(world -> Sponge.getServer().getWorldManager().unloadWorld(world));
    }

    @Override
    public void enable(MessageReceiver src) {
        try {
            CatalogKey key = CatalogKey.of("multi-world-test", "overnether");
            final WorldArchetype archetype = Sponge.getRegistry().getCatalogRegistry().get(WorldArchetype.class, key).orElse(
                WorldArchetype.builder().
                    from(WorldArchetypes.THE_NETHER.get())
                    .serializationBehavior(SerializationBehaviors.NONE.get())
                    .generatorType(GeneratorTypes.OVERWORLD.get())
                    .key(key)
                    .build()
            );
            WorldRegistration registration = WorldRegistration.builder().directoryName("temp").build();

            Optional<WorldProperties> properties = Sponge.getServer().getWorldManager().createProperties(registration, archetype);

            Sponge.getServer().getWorldManager().loadWorld(properties.get());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
