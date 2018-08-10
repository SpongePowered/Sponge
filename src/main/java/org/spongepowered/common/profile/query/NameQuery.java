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
package org.spongepowered.common.profile.query;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.api.profile.ProfileNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class NameQuery<T> extends Query<T> {

    protected NameQuery(GameProfileCache cache, boolean useCache) {
        super(cache, useCache);
    }

    public static final class SingleGet extends NameQuery<GameProfile> {

        private final String name;

        public SingleGet(GameProfileCache cache, String name, boolean useCache) {
            super(cache, useCache);
            this.name = name;
        }

        @Override
        public GameProfile call() throws Exception {
            List<GameProfile> profiles = this.fromNames(Collections.singleton(this.name));
            if (profiles.isEmpty()) {
                throw new ProfileNotFoundException("Failed to find profile for name " + this.name);
            }
            return profiles.get(0);
        }
    }

    public static final class MultiGet extends NameQuery<Collection<GameProfile>> {

        private final Iterator<String> iterator;

        public MultiGet(GameProfileCache cache, Iterable<String> iterable, boolean useCache) {
            super(cache, useCache);
            this.iterator = iterable.iterator();
        }

        @Override
        public Collection<GameProfile> call() throws Exception {
            if (!this.iterator.hasNext()) {
                return ImmutableSet.of();
            }

            return this.fromNames(Sets.newHashSet(this.iterator));
        }
    }

}
