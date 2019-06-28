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
package org.spongepowered.common.event.damage;

import com.google.common.base.MoreObjects;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class MinecraftBlockDamageSource extends DamageSource {

    private final BlockSnapshot blockSnapshot;
    private final Location<World> location;

    public MinecraftBlockDamageSource(final String damageTypeIn, final Location<World> location) {
        super(damageTypeIn);
        this.blockSnapshot = location.createSnapshot();
        this.location = location;
    }

    public void impl$setFireDamage() {
        this.setFireDamage();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("BlockDamageSource")
            .add("Name", this.damageType)
            .add("BlockSnapshot", this.blockSnapshot)
            .add("Location", this.location)
            .toString();
    }
}
