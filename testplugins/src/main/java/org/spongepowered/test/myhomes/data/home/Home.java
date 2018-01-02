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
package org.spongepowered.test.myhomes.data.home;

import org.spongepowered.test.myhomes.data.home.impl.HomeBuilder;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.World;

public class Home implements DataSerializable {

    public static final DataQuery WORLD_QUERY = DataQuery.of("WorldUUID");
    public static final DataQuery POSITION_QUERY = DataQuery.of("Position");
    public static final DataQuery ROTATION_QUERY = DataQuery.of("Rotation");
    public static final DataQuery NAME_QUERY = DataQuery.of("Name");

    private Transform<World> transform;

    private String name;

    public Home(Transform<World> transform, String name) {
        this.transform = transform;
        this.name = name;
    }

    @Override
    public int getContentVersion() {
        return HomeBuilder.CONTENT_VERSION;
    }

    public Transform<World> getTransform() {
        return this.transform;
    }

    public String getName() {
        return name;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(WORLD_QUERY, this.transform.getExtent().getUniqueId())
                .set(POSITION_QUERY, this.transform.getPosition())
                .set(ROTATION_QUERY, this.transform.getRotation())
                .set(NAME_QUERY, this.name)
                .set(Queries.CONTENT_VERSION, HomeBuilder.CONTENT_VERSION);
    }

}
