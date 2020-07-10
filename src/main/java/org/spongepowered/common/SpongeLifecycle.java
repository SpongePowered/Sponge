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
package org.spongepowered.common;

import org.spongepowered.api.Engine;
import org.spongepowered.common.registry.SpongeBuilderRegistry;
import org.spongepowered.common.registry.SpongeFactoryRegistry;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;
import org.spongepowered.common.service.SpongeServiceProvider;

public abstract class SpongeLifecycle {

    protected final Engine engine;

    public SpongeLifecycle(final Engine engine) {
        this.engine = engine;
    }

    public void establishFactories() {
        ((SpongeFactoryRegistry) this.engine.getGame().getRegistry().getFactoryRegistry()).registerDefaultFactories();
    }

    public void establishBuilders() {
        ((SpongeBuilderRegistry) this.engine.getGame().getRegistry().getBuilderRegistry()).registerDefaultBuilders();
    }

    public void initTimings() {
        SpongeTimingsFactory.INSTANCE.init();
    }

    public void establishServices() {
        ((SpongeServiceProvider) this.engine.getGame().getServiceProvider()).init();
    }

    public void establishServerFeatures() {
        //Sponge.getSystemSubject().getContainingCollection();
        // Yes this looks odd but prevents having to do sided lifecycle solely to always point at the Server
        ((SpongeServer) this.engine.getGame().getServer()).getUsernameCache().load();
    }
}
