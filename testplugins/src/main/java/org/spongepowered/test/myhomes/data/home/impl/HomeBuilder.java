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
package org.spongepowered.test.myhomes.data.home.impl;

import org.spongepowered.test.myhomes.data.home.Home;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;
import java.util.UUID;

public class HomeBuilder extends AbstractDataBuilder<Home> {

    public static final int CONTENT_VERSION = 2;

    public HomeBuilder() {
        super(Home.class, CONTENT_VERSION);
    }

    @Override
    protected Optional<Home> buildContent(DataView content) throws InvalidDataException {
        if (!content.contains(Home.WORLD_QUERY, Home.POSITION_QUERY, Home.ROTATION_QUERY, Home.NAME_QUERY)) {
            return Optional.empty();
        }

        World world = Sponge.getServer().getWorld(content.getObject(Home.WORLD_QUERY, UUID.class).get()).orElseThrow(InvalidDataException::new);
        Vector3d position = content.getObject(Home.POSITION_QUERY, Vector3d.class).get();
        Vector3d rotation = content.getObject(Home.ROTATION_QUERY, Vector3d.class).get();
        String name = content.getString(Home.NAME_QUERY).get();

        Transform<World> transform = new Transform<>(world, position, rotation);
        return Optional.of(new Home(transform, name));
    }

    public static class NameUpdater implements DataContentUpdater {
        @Override
        public int getInputVersion() {
            return 1;
        }

        @Override
        public int getOutputVersion() {
            return 2;
        }

        @Override
        public DataView update(DataView content) {
            if (!content.contains(Home.WORLD_QUERY, Home.POSITION_QUERY)) {
                throw new InvalidDataException("Invalid data for a home!");
            }
            UUID uuid = content.getObject(Home.WORLD_QUERY, UUID.class).get();
            Vector3d pos = content.getObject(Home.POSITION_QUERY, Vector3d.class).get();
            String name = Sponge.getServer().getWorldProperties(uuid)
                    .map(WorldProperties::getWorldName)
                    .orElse(uuid.toString().substring(0, 9));

            name = String.format("%s-%d,%d,%d", name, pos.getFloorX(), pos.getFloorY(), pos.getFloorZ());

            content.set(Home.NAME_QUERY, name);

            return content;
        }
    }
}
