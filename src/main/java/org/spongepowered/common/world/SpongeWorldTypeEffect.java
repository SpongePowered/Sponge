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
package org.spongepowered.common.world;

import net.minecraft.world.DimensionType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.WorldTypeEffect;
import org.spongepowered.common.AbstractResourceKeyed;
import org.spongepowered.common.registry.provider.DimensionEffectProvider;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

public final class SpongeWorldTypeEffect extends AbstractResourceKeyed implements WorldTypeEffect {

    public SpongeWorldTypeEffect(final ResourceKey key) {
        super(key);

        DimensionEffectProvider.INSTANCE.put(key, this);
    }

    public static final class BuilderImpl extends AbstractResourceKeyedBuilder<WorldTypeEffect, WorldTypeEffect.Builder> implements WorldTypeEffect.Builder {

        @Override
        protected WorldTypeEffect build0() {
            return new SpongeWorldTypeEffect(this.key);
        }
    }

    public static final class FactoryImpl implements WorldTypeEffect.Factory {

        private static final SpongeWorldTypeEffect OVERWORLD = new SpongeWorldTypeEffect((ResourceKey) (Object) DimensionType.OVERWORLD_EFFECTS);

        private static final SpongeWorldTypeEffect NETHER = new SpongeWorldTypeEffect((ResourceKey) (Object) DimensionType.NETHER_EFFECTS);

        private static final SpongeWorldTypeEffect END = new SpongeWorldTypeEffect((ResourceKey) (Object) DimensionType.END_EFFECTS);

        @Override
        public WorldTypeEffect overworld() {
            return FactoryImpl.OVERWORLD;
        }

        @Override
        public WorldTypeEffect nether() {
            return FactoryImpl.NETHER;
        }

        @Override
        public WorldTypeEffect end() {
            return FactoryImpl.END;
        }
    }
}
