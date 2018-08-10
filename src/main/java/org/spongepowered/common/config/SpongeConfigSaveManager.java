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
package org.spongepowered.common.config;

import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/**
 * To avoid file saving issues with Windows and to allow some async file saving,
 * this manager acts as a staging ground for file saves so that we can do them
 * in batches.
 *
 * <p>This class is intended to be thread safe through the use of
 * synchronisation.</p>
 */
public class SpongeConfigSaveManager {

    private final Set<SpongeConfig<?>> stagedConfigs = new HashSet<>();

    public void save(SpongeConfig<?> spongeConfig) {
        synchronized (this) {
            if (!SpongeImpl.isInitialized() || // if we're not initialised, then we're likely testing and should just pass on through.
                    SpongeImpl.getGame().getState() == GameState.SERVER_STARTED || SpongeImpl.getGame().getState() == GameState.GAME_STOPPED) {
                if (!this.stagedConfigs.isEmpty()) {
                    // We want to save and flush now, but add this into the set in case it is already present.
                    this.stagedConfigs.add(spongeConfig);
                    flush();
                } else {
                    // just save
                    spongeConfig.saveNow();
                }
            } else {
                this.stagedConfigs.add(spongeConfig);
            }
        }
    }

    /**
     * Flush a specific config. Returns true if successful or if the config was
     * not in the set. False if there was an error on save.
     *
     * @param config The config to save now
     * @return {@code true} if successful or unneeded, false otherwise.
     */
    public boolean flush(SpongeConfig<?> config) {
        synchronized (this) {
            if (this.stagedConfigs.remove(config)) {
                return config.saveNow();
            }
        }

        return true;
    }

    public void flush() {
        if (!this.stagedConfigs.isEmpty()) {
            synchronized (this) {
                for (SpongeConfig<?> spongeConfig : this.stagedConfigs) {
                    spongeConfig.saveNow();
                }

                this.stagedConfigs.clear();
            }
        }
    }

}
