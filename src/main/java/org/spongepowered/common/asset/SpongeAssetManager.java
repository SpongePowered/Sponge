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
package org.spongepowered.common.asset;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.net.URL;
import java.util.Optional;

@Singleton
public final class SpongeAssetManager implements AssetManager {

    private static final String DEFAULT_ASSET_DIR = "assets/";
    private static final ClassLoader CLASS_LOADER = Sponge.class.getClassLoader();

    @Override
    public Optional<Asset> asset(final PluginContainer container, final String name) {
        checkNotNull(container);
        checkNotNull(name);
        checkArgument(!name.isEmpty(), "name cannot be empty");

        URL url = SpongeAssetManager.CLASS_LOADER.getResource(SpongeAssetManager.DEFAULT_ASSET_DIR + container.metadata().id() + '/' + name);
        if (url == null) {
            return Optional.empty();
        }
        return Optional.of(new SpongeAsset(container, url));
    }

    @Override
    public Optional<Asset> asset(String name) {
        return this.asset(Launch.instance().platformPlugin(), name);
    }
}
