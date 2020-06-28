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
package org.spongepowered.common.registry.type.world;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

public final class BlockChangeFlagRegistryModule implements RegistryModule {

    @RegisterCatalog(org.spongepowered.api.world.BlockChangeFlags.class)
    private final Map<String, SpongeBlockChangeFlag> flags = new LinkedHashMap<>();
    private final SpongeBlockChangeFlag[] maskedFlags = new SpongeBlockChangeFlag[64];
    private static BlockChangeFlagRegistryModule INSTANCE = new BlockChangeFlagRegistryModule();

    public static BlockChangeFlagRegistryModule getInstance() {
        return INSTANCE;
    }

    public static SpongeBlockChangeFlag fromNativeInt(int flag) {
        if (flag >= getInstance().maskedFlags.length) {
            return (SpongeBlockChangeFlag) org.spongepowered.api.world.BlockChangeFlags.ALL;
        }
        if (flag == 3) {
            return (SpongeBlockChangeFlag) org.spongepowered.api.world.BlockChangeFlags.ALL;
        }
        if (flag == 2) {
            return (SpongeBlockChangeFlag) org.spongepowered.api.world.BlockChangeFlags.PHYSICS_OBSERVER;
        }
        final SpongeBlockChangeFlag spongeBlockChangeFlag = getInstance().maskedFlags[flag];
        return spongeBlockChangeFlag;
    }

    public static BlockChangeFlag andNotifyClients(BlockChangeFlag flag) {
        final int rawFlag = ((SpongeBlockChangeFlag) flag).getRawFlag();
        if ((rawFlag & Constants.BlockChangeFlags.NOTIFY_CLIENTS) != 0){
            return flag; // We don't need to rerun the flag
        }
        return fromNativeInt(rawFlag & ~Constants.BlockChangeFlags.NOTIFY_CLIENTS);
    }

    private BlockChangeFlagRegistryModule() {
    }

    @Override
    public void registerDefaults() {
        // A documentation note:
        /*
        Due to the way that Mojang handles block physics, there are four flags inverted here:
        1) BlockChangeFlags.IGNORE_RENDER - Prevents the block from being re-rendered in the client world
        2) BlockChangeFlags.FORCE_RE_RENDER - Requires the block to be re-rendered on the main thread for a client, as long as IGNORE_RENDER is clear
        3) BlockChangeFlags.OBSERVER - Prevents observer blocks from being told about block changes, separate from neighbor notifications
        4) BlockChangeFlags.PHYSICS - Sponge specific, prevents block.onAdd logic being called

        The other two flags:
        1) BlockChangeFlags.NEIGHBOR - Notify neighbor blocks
        2) BlockChangeFlags.NOTIFY_CLIENTS - Notify clients of block change

        are always true based. If they are set, they will process those two flags.
        This is why there are so many permutations.
         */

        // devise all permutations
        for (int i = 0; i < 64; i++) { // 64 because we get to the 6th bit of possible combinations
            final StringJoiner builder = new StringJoiner("|");
            if ((i & Constants.BlockChangeFlags.NEIGHBOR_MASK) != 0) {
                builder.add("NEIGHBOR");
            }
            if ((i & Constants.BlockChangeFlags.NOTIFY_CLIENTS) != 0) {
                // We don't want to confuse that there are going to be multiple flags
                // but with slight differences because of the notify flag
                builder.add("NOTIFY_CLIENTS");
            }
            if ((i & Constants.BlockChangeFlags.IGNORE_RENDER) != 0) {
                // We don't want to confuse that there are going to be multiple flags
                // but with a slight difference because of the ignore render flag
                builder.add("IGNORE_RENDER");
            }
            if ((i & Constants.BlockChangeFlags.FORCE_RE_RENDER) != 0) {
                // We don't want to confuse that there are going to be multiple flags
                // but with a slight difference due to the client only flag.
                builder.add("FORCE_RE_RENDER");
            }
            if ((i & Constants.BlockChangeFlags.OBSERVER_MASK) == 0) {
                builder.add("OBSERVER");
            }
            if ((i & Constants.BlockChangeFlags.PHYSICS_MASK) == 0) {
                builder.add("PHYSICS");
            }
            if (Constants.BlockChangeFlags.NONE == i) {
                register(new SpongeBlockChangeFlag("NONE".toLowerCase(Locale.ENGLISH), i));
            } else if (Constants.BlockChangeFlags.ALL == i) {
                register(new SpongeBlockChangeFlag("ALL".toLowerCase(Locale.ENGLISH), i));
                register(new SpongeBlockChangeFlag("NEIGHBOR_PHYSICS_OBSERVER".toLowerCase(Locale.ENGLISH), i));
            } else if (Constants.BlockChangeFlags.NEIGHBOR == i) {
                register(new SpongeBlockChangeFlag("NEIGHBOR".toLowerCase(Locale.ENGLISH), i));
            } else if (Constants.BlockChangeFlags.PHYSICS == i) {
                register(new SpongeBlockChangeFlag("PHYSICS".toLowerCase(Locale.ENGLISH), i));
            } else if (Constants.BlockChangeFlags.OBSERVER == i) {
                register(new SpongeBlockChangeFlag("OBSERVER".toLowerCase(Locale.ENGLISH), i));
            } else if (Constants.BlockChangeFlags.NEIGHBOR_PHYSICS == i) {
                register(new SpongeBlockChangeFlag("NEIGHBOR_PHYSICS".toLowerCase(Locale.ENGLISH), i));
            } else if (Constants.BlockChangeFlags.NEIGHBOR_OBSERVER == i) {
                register(new SpongeBlockChangeFlag("NEIGHBOR_OBSERVER".toLowerCase(Locale.ENGLISH), i));
            } else if (Constants.BlockChangeFlags.PHYSICS_OBSERVER == i) {
                register(new SpongeBlockChangeFlag("PHYSICS_OBSERVER".toLowerCase(Locale.ENGLISH), i));
            } else {
                register(new SpongeBlockChangeFlag(builder.toString().toLowerCase(Locale.ENGLISH), i));
            }
        }
    }

    private void register(SpongeBlockChangeFlag flag) {
        this.maskedFlags[flag.getRawFlag()] = flag;
        this.flags.put(flag.getName(), flag);
    }

    public Collection<SpongeBlockChangeFlag> getValues() {
        return Collections.unmodifiableCollection(this.flags.values());
    }

}
