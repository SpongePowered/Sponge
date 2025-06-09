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
package org.spongepowered.common.world.server;

import org.spongepowered.api.util.Builder;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.api.world.server.WorldArchetype;
import org.spongepowered.api.world.server.WorldArchetypeType;

import java.util.Objects;
import java.util.Optional;

public record SpongeWorldArchetype(WorldArchetypeType type, Optional<WorldGenerationConfig> generationConfig) implements WorldArchetype {

    public static final class BuilderImpl implements WorldArchetype.Builder {

        private WorldArchetypeType type;
        private WorldGenerationConfig generationConfig;

        public BuilderImpl() {
            this.reset();
        }

        @Override
        public Builder type(final WorldArchetypeType type) {
            this.type = type;
            return this;
        }

        @Override
        public Builder generationConfig(final WorldGenerationConfig generationConfig) {
            this.generationConfig = generationConfig;
            return this;
        }

        @Override
        public Builder from(final WorldArchetype value) {
            this.type = value.type();
            value.generationConfig().ifPresent(c -> this.generationConfig = c);
            return this;
        }

        @Override
        public Builder reset() {
            this.type = null;
            this.generationConfig = null;
            return this;
        }

        @Override
        public WorldArchetype build() {
            return new SpongeWorldArchetype(Objects.requireNonNull(this.type, "type"), Optional.ofNullable(this.generationConfig));
        }
    }
}
