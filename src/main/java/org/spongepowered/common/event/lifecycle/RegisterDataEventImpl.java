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
package org.spongepowered.common.event.lifecycle;

import org.spongepowered.api.Game;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.SpongeDataRegistration;

public final class RegisterDataEventImpl extends AbstractLifecycleEvent implements RegisterDataEvent {

    private final SpongeDataManager spongeDataManager;

    public RegisterDataEventImpl(final Cause cause, final Game game, final SpongeDataManager spongeDataManager) {
        super(cause, game);
        this.spongeDataManager = spongeDataManager;
    }

    @Override
    public RegisterDataEvent register(final DataRegistration registration) {
        if (!(registration instanceof SpongeDataRegistration)) {
            throw new IllegalArgumentException("Invalid DataRegistration Class! Use the DataRegistration.Builder.");
        }
        this.spongeDataManager.registerCustomDataRegistration((SpongeDataRegistration) registration);
        return this;
    }
}
