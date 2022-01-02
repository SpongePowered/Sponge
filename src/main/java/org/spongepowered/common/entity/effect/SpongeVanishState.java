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
package org.spongepowered.common.entity.effect;

import com.google.common.base.Objects;
import org.spongepowered.api.effect.VanishState;

public final class SpongeVanishState implements VanishState {
    private static final VanishState VISIBLE = new SpongeVanishState(false, true, false, true, true, true);
    private static final VanishState DEFAULT_VANISHED = new SpongeVanishState(true, false, false, false, false, false);

    private final boolean vanished;
    private final boolean ignoresCollisions;
    private final boolean untargetable;
    private final boolean affectsSpawning;
    private final boolean createsSounds;
    private final boolean createsParticles;

    SpongeVanishState(
        final boolean vanished,
        final boolean collisions,
        final boolean untargetable,
        final boolean affectsSpawning,
        final boolean createsSounds,
        final boolean createsParticles
    ) {
        this.vanished = vanished;
        if (!vanished) {
            this.ignoresCollisions = false;
            this.untargetable = false;
            this.affectsSpawning = true;
            this.createsSounds = true;
            this.createsParticles = true;
            return;
        }
        this.ignoresCollisions = collisions;
        this.untargetable = untargetable;
        this.affectsSpawning = affectsSpawning;
        this.createsSounds = createsSounds;
        this.createsParticles = createsParticles;
    }

    @Override
    public boolean invisible() {
        return this.vanished;
    }

    @Override
    public VanishState vanish() {
        if (this.vanished) {
            return this;
        }
        return SpongeVanishState.DEFAULT_VANISHED;
    }

    @Override
    public VanishState unvanish() {
        return SpongeVanishState.VISIBLE;
    }

    @Override
    public boolean ignoresCollisions() {
        return this.ignoresCollisions;
    }

    @Override
    public VanishState ignoreCollisions(final boolean ignoresCollisions) {
        if (!this.vanished) {
            return this;
        }
        if (this.ignoresCollisions == ignoresCollisions) {
            return this;
        }
        return new SpongeVanishState(
            true,
            ignoresCollisions,
            this.untargetable,
            affectsSpawning,
            createsSounds,
            createsParticles
        );
    }

    @Override
    public boolean untargetable() {
        return this.untargetable;
    }

    @Override
    public VanishState untargetable(final boolean untargetable) {
        if (!this.vanished) {
            return this;
        }
        if (this.untargetable == untargetable) {
            return this;
        }
        return new SpongeVanishState(
            true,
            this.ignoresCollisions,
            untargetable,
            affectsSpawning,
            createsSounds,
            createsParticles
        );
    }

    @Override
    public boolean affectsMonsterSpawning() {
        return false;
    }

    @Override
    public VanishState affectMonsterSpawning(final boolean affectsMonsterSpawning) {
        if (!this.vanished) {
            return this;
        }
        if (this.affectsSpawning == affectsMonsterSpawning) {
            return this;
        }
        return new SpongeVanishState(
            true,
            this.ignoresCollisions,
            this.untargetable,
            affectsMonsterSpawning,
            this.createsSounds,
            this.createsParticles
        );
    }

    @Override
    public boolean createsSounds() {
        return this.createsSounds;
    }

    @Override
    public VanishState createSounds(final boolean createSounds) {
        if (!this.vanished) {
            return this;
        }
        if (this.createsSounds == createSounds) {
            return this;
        }
        return new SpongeVanishState(
            true,
            this.ignoresCollisions,
            this.untargetable,
            this.affectsSpawning,
            createSounds,
            this.createsParticles
        );
    }

    @Override
    public boolean createsParticles() {
        return this.createsParticles;
    }

    @Override
    public VanishState createParticles(final boolean createParticles) {
        if (!this.vanished) {
            return this;
        }
        if (this.createsParticles == createParticles) {
            return this;
        }
        return new SpongeVanishState(
            true,
            this.ignoresCollisions,
            this.untargetable,
            this.affectsSpawning,
            this.createsSounds,
            createParticles
        );
    }

    @Override
    public String toString() {
        return "VanishedState{ignoresCollisions=" + this.ignoresCollisions + ", untargetable=" + this.untargetable + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(true, this.ignoresCollisions, this.untargetable);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VanishState)) {
            return false;
        }
        final VanishState other = (VanishState) obj;
        return
            this.invisible() == other.invisible()
                && this.ignoresCollisions == other.ignoresCollisions()
                && this.untargetable == other.untargetable();
    }

    public static final class SpongeVanishStateFactory implements Factory {
        @Override
        public VanishState vanished() {
            return SpongeVanishState.DEFAULT_VANISHED;
        }

        @Override
        public VanishState unvanished() {
            return SpongeVanishState.VISIBLE;
        }
    }
}
