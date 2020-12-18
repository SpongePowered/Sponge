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
package org.spongepowered.common.service.game;

import io.leangen.geantyref.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.common.applaunch.config.common.ServicesCategory;
import org.spongepowered.common.event.lifecycle.ProvideServiceEventImpl;
import org.spongepowered.common.service.SpongeServiceProvider;
import org.spongepowered.common.service.game.pagination.SpongePaginationService;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collections;
import java.util.List;

@Singleton
public final class SpongeGameScopedServiceProvider extends SpongeServiceProvider implements ServiceProvider.GameScoped {

    @Inject
    public SpongeGameScopedServiceProvider(final Game game, final Injector injector) {
        super(game, injector);
    }

    @Override
    protected List<Service<?>> servicesToSelect() {
        return Collections.singletonList(new Service<>(
                PaginationService.class,
                servicePluginSubCategory -> servicePluginSubCategory.paginationService,
                SpongePaginationService.class));
    }

    @Override
    protected final <T> ProvideServiceEventImpl<T> createEvent(final PluginContainer container, final Service<T> service) {
        return new ProvideServiceEventImpl<T>(Cause.of(EventContext.empty(), this.getGame()),
                this.getGame(),
                TypeToken.get(service.getServiceClass()));
    }

    @Override
    @NonNull
    public final PaginationService paginationService() {
        return this.provideUnchecked(PaginationService.class);
    }

}
