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
package org.spongepowered.common.registry.type.text;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.text.selector.ArgumentHolder;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.text.selector.SpongeArgumentHolder;
import org.spongepowered.common.text.selector.SpongeSelectorFactory;

import java.util.HashMap;
import java.util.Map;

@RegistrationDependency({SelectorTypeRegistryModule.class})
public final class ArgumentRegistryModule implements RegistryModule {

    @RegisterCatalog(ArgumentTypes.class)
    private final Map<String, ArgumentHolder<?>> argumentTypeMap = new HashMap<>();

    @Override
    public void registerDefaults() {
        final SpongeSelectorFactory factory = SpongeImpl.getRegistry().getSelectorFactory();
        // POSITION
        ArgumentType<Integer> x = factory.createArgumentType("x", Integer.class);
        ArgumentType<Integer> y = factory.createArgumentType("y", Integer.class);
        ArgumentType<Integer> z = factory.createArgumentType("z", Integer.class);
        ArgumentHolder.Vector3<Vector3i, Integer> position = new SpongeArgumentHolder.SpongeVector3<>(x, y, z, Vector3i.class);
        this.argumentTypeMap.put("position", position);

        // RADIUS
        ArgumentType<Integer> rmin = factory.createArgumentType("rm", Integer.class);
        ArgumentType<Integer> rmax = factory.createArgumentType("r", Integer.class);
        ArgumentHolder.Limit<ArgumentType<Integer>> radius = new SpongeArgumentHolder.SpongeLimit<>(rmin, rmax);
        this.argumentTypeMap.put("radius", radius);

        // GAME_MODE
        this.argumentTypeMap.put("game_mode", factory.createInvertibleArgumentType("m", GameMode.class));

        // COUNT
        this.argumentTypeMap.put("count", factory.createArgumentType("c", Integer.class));

        // LEVEL
        ArgumentType<Integer> lmin = factory.createArgumentType("lm", Integer.class);
        ArgumentType<Integer> lmax = factory.createArgumentType("l", Integer.class);
        ArgumentHolder.Limit<ArgumentType<Integer>> level = new SpongeArgumentHolder.SpongeLimit<>(lmin, lmax);
        this.argumentTypeMap.put("level", level);

        // TEAM
        this.argumentTypeMap.put("team", factory.createInvertibleArgumentType("team", String.class));

        // NAME
        this.argumentTypeMap.put("name", factory.createInvertibleArgumentType("name", String.class));

        // DIMENSION
        ArgumentType<Integer> dx = factory.createArgumentType("dx", Integer.class);
        ArgumentType<Integer> dy = factory.createArgumentType("dy", Integer.class);
        ArgumentType<Integer> dz = factory.createArgumentType("dz", Integer.class);
        ArgumentHolder.Vector3<Vector3i, Integer> dimension =
            new SpongeArgumentHolder.SpongeVector3<>(dx, dy, dz, Vector3i.class);
        this.argumentTypeMap.put("dimension", dimension);

        // ROTATION
        ArgumentType<Double> rotxmin = factory.createArgumentType("rxm", Double.class);
        ArgumentType<Double> rotymin = factory.createArgumentType("rym", Double.class);
        ArgumentType<Double> rotzmin = factory.createArgumentType("rzm", Double.class);
        ArgumentHolder.Vector3<Vector3d, Double> rotmin =
            new SpongeArgumentHolder.SpongeVector3<>(rotxmin, rotymin, rotzmin, Vector3d.class);
        ArgumentType<Double> rotxmax = factory.createArgumentType("rx", Double.class);
        ArgumentType<Double> rotymax = factory.createArgumentType("ry", Double.class);
        ArgumentType<Double> rotzmax = factory.createArgumentType("rz", Double.class);
        ArgumentHolder.Vector3<Vector3d, Double> rotmax =
            new SpongeArgumentHolder.SpongeVector3<>(rotxmax, rotymax, rotzmax, Vector3d.class);
        ArgumentHolder.Limit<ArgumentHolder.Vector3<Vector3d, Double>> rot =
            new SpongeArgumentHolder.SpongeLimit<>(rotmin, rotmax);
        this.argumentTypeMap.put("rotation", rot);

        // ENTITY_TYPE
        this.argumentTypeMap.put("entity_type", factory.createInvertibleArgumentType("type", EntityType.class));
    }
}
