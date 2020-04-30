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
package org.spongepowered.common.event.tracking.phase.entity;

import org.spongepowered.common.event.tracking.IPhaseState;

public final class EntityPhase {

    public static final class State {
        public static final IPhaseState<EntityDeathContext> DEATH = new EntityDeathState();
        public static final IPhaseState<BasicEntityContext> DEATH_UPDATE = new DeathUpdateState();
        public static final IPhaseState<DimensionChangeContext> CHANGING_DIMENSION = new ChangingToDimensionState();
        public static final IPhaseState<InvokingTeleporterContext> INVOKING_TELEPORTER = new InvokingTeleporterState();
        public static final IPhaseState<BasicEntityContext> LEAVING_DIMENSION = new LeavingDimensionState();
        public static final IPhaseState<BasicEntityContext> PLAYER_WAKE_UP = new PlayerWakeUpState();
        public static final IPhaseState<BasicEntityContext> ENTITY_DROP_ITEMS = new EntityDropPhaseState();
        public static final IPhaseState<BasicEntityContext> COLLISION = new EntityCollisionState();

        private State() {
        }
    }


    private EntityPhase() {
    }

}
