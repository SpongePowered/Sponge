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
package org.spongepowered.test.world;

import com.google.inject.Inject;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.world.biome.BiomeFinders;
import org.spongepowered.api.world.WorldTypeEffects;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.WorldTypeTemplates;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("dimensiontest")
public final class DimensionTest {

    private final PluginContainer plugin;

    @Inject
    public DimensionTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterDataPackValue(final RegisterDataPackValueEvent event) {
        event
            .register(WorldTypeTemplate
                .builder()
                    .key(ResourceKey.of(this.plugin, "test_one"))
                    .from(WorldTypeTemplates.THE_NETHER)
                    .effect(WorldTypeEffects.END)
                    .createDragonFight(true)
                    .build()
            )
            .register(WorldTypeTemplate
                    .builder()
                    .from(WorldTypeTemplates.OVERWORLD)
                    .key(ResourceKey.of(this.plugin, "test_two"))
                    .effect(WorldTypeEffects.END)
                    .coordinateMultiplier(2)
                    .biomeFinder(BiomeFinders.FUZZY) // Overworld is column fuzzed by default...this should be interesting
                    .createDragonFight(true)
                    .piglinSafe(true)
                    .build()
            )
        ;
    }
}
