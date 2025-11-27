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
package org.spongepowered.common.entity;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

public class EntityArchetypeTest {

    @Disabled
    @Test
    public void testEntityArchetype() {
        final ServerWorld world = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get();
        final Sheep entity = world.createEntity(EntityTypes.SHEEP, Vector3d.ZERO);

        Sponge.server().causeStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN); // Need that for restoring

        final EntityArchetype archetype = entity.createArchetype();
        // Restore same entity from archetype
        archetype.apply(entity.serverLocation());
        // Test restoring from serialized archetype
        final EntityArchetype rebuiltArchetype = EntityArchetype.builder().build(archetype.toContainer()).get();
        rebuiltArchetype.apply(entity.serverLocation());

        final EntitySnapshot rebuiltSnapshot = rebuiltArchetype.toSnapshot(entity.serverLocation());
        rebuiltSnapshot.restore();
    }
}
