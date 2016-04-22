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
package org.spongepowered.common.gui.window;

import net.minecraft.util.BlockPos;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.gui.window.LocationBackedWindow;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public abstract class AbstractSpongeBlockContainerWindow extends AbstractSpongeContainerWindow implements LocationBackedWindow {

    protected Location<World> location;

    @Override
    protected IInteractionObject provideInteractionObject() {
        if (this.location != null) {
            return provideInteractionObject((net.minecraft.world.World) this.location.getExtent(), VecHelper.toBlockPos(this.location));
        } else {
            return provideInteractionObject(null, null);
        }
    }

    protected abstract IInteractionObject provideInteractionObject(net.minecraft.world.World world, BlockPos pos);

    @Override
    protected boolean shouldCreateVirtualContainer() {
        return this.location == null;
    }

    @Override
    public Optional<Location<World>> getLocation() {
        return Optional.ofNullable(this.location);
    }

    @Override
    public void setLocation(Location<World> location) {
        checkNotOpen();
        this.location = location;
    }

}
