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
package org.spongepowered.common.service;

import com.google.inject.Singleton;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.service.ServiceRegistration;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.service.whitelist.WhitelistService;

import java.util.Optional;

@Singleton
public final class SpongeServiceProvider implements ServiceProvider {

    @Override public <T> Optional<T> provide(Class<T> serviceClass) {
        return Optional.empty();
    }

    @Override public <T> Optional<ServiceRegistration<T>> getRegistration(Class<T> serviceClass) {
        return Optional.empty();
    }

    @Override public BanService banService() {
        return null;
    }

    @Override public Optional<EconomyService> economyService() {
        return Optional.empty();
    }

    @Override public PaginationService paginationService() {
        return null;
    }

    @Override public PermissionService permissionService() {
        return null;
    }

    @Override public UserStorageService userStorageService() {
        return null;
    }

    @Override public WhitelistService whitelistService() {
        return null;
    }
}
