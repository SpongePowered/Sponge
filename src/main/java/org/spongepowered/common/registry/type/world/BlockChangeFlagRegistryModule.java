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
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

public final class BlockChangeFlagRegistryModule implements RegistryModule {

    @RegisterCatalog(BlockChangeFlags.class)
    private final Map<String, SpongeBlockChangeFlag> flags = new LinkedHashMap<>();
    private final Int2ObjectMap<SpongeBlockChangeFlag> maskedFlags = new Int2ObjectLinkedOpenHashMap<>(70);
    private static BlockChangeFlagRegistryModule INSTANCE = new BlockChangeFlagRegistryModule();

    public static BlockChangeFlagRegistryModule getInstance() {
        return INSTANCE;
    }

    public static SpongeBlockChangeFlag fromNativeInt(int flag) {
        if (flag == 3) {
            return (SpongeBlockChangeFlag) BlockChangeFlags.ALL;
        }
        if (flag == 2) {
            return (SpongeBlockChangeFlag) BlockChangeFlags.PHYSICS_OBSERVER;
        }
        final SpongeBlockChangeFlag spongeBlockChangeFlag = getInstance().maskedFlags.get(flag);
        if (spongeBlockChangeFlag != null) {
            return spongeBlockChangeFlag;
        }
        return (SpongeBlockChangeFlag) BlockChangeFlags.ALL;
    }

    private BlockChangeFlagRegistryModule() {
    }

    @Override
    public void registerDefaults() {
        // A documentation note:
        /*
        Due to the way that Mojang handles block physics, there are four flags inverted here:
        1) Flags.IGNORE_RENDER - Prevents the block from being re-rendered in the client world
        2) Flags.FORCE_RE_RENDER - Requires the block to be re-rendered on the main thread for a client, as long as IGNORE_RENDER is clear
        3) Flags.OBSERVER - Prevents observer blocks from being told about block changes, separate from neighbor notifications
        4) Flags.PHYSICS - Sponge specific, prevents block.onAdd logic being called

        The other two flags:
        1) Flags.NEIGHBOR - Notify neighbor blocks
        2) Flags.NOTIFY_CLIENTS - Notify clients of block change

        are always true based. If they are set, they will process those two flags.
        This is why there are so many permutations.
         */

        // devise all permutations
        for (int i = 0; i < 64; i++) { // 64 because we get to the 6th bit of possible combinations
            final StringJoiner builder = new StringJoiner("|");
            if ((i & Flags.NEIGHBOR_MASK) != 0) {
                builder.add(Flag.NOTIFY_NEIGHBOR.name);
            }
            if ((i & Flags.NOTIFY_CLIENTS) != 0) {
                // We don't want to confuse that there are going to be multiple flags
                // but with slight differences because of the notify flag
                builder.add(Flag.NOTIFY_CLIENTS.name);
            }
            if ((i & Flags.IGNORE_RENDER) != 0) {
                // We don't want to confuse that there are going to be multiple flags
                // but with a slight difference because of the ignore render flag
                builder.add(Flag.IGNORE_RENDER.name);
            }
            if ((i & Flags.FORCE_RE_RENDER) != 0) {
                // We don't want to confuse that there are going to be multiple flags
                // but with a slight difference due to the client only flag.
                builder.add(Flag.FORCE_RE_RENDER.name);
            }
            if ((i & Flags.OBSERVER_MASK) == 0) {
                builder.add(Flag.IGNORE_OBSERVER.name);
            }
            if ((i & Flags.PHYSICS_MASK) == 0) {
                builder.add(Flag.IGNORE_PHYSICS.name);
            }
            if (Flags.NONE == i) {
                register(new SpongeBlockChangeFlag("NONE".toLowerCase(Locale.ENGLISH), i));
            } else if (Flags.ALL == i) {
                register(new SpongeBlockChangeFlag("ALL".toLowerCase(Locale.ENGLISH), i));
                register(new SpongeBlockChangeFlag("NEIGHBOR_PHYSICS_OBSERVER".toLowerCase(Locale.ENGLISH), i));
            } else if (Flags.NEIGHBOR == i) {
                register(new SpongeBlockChangeFlag("NEIGHBOR".toLowerCase(Locale.ENGLISH), i));
            } else if (Flags.PHYSICS == i) {
                register(new SpongeBlockChangeFlag("PHYSICS".toLowerCase(Locale.ENGLISH), i));
            } else if (Flags.OBSERVER == i) {
                register(new SpongeBlockChangeFlag("OBSERVER".toLowerCase(Locale.ENGLISH), i));
            } else if (Flags.NEIGHBOR_PHSYICS == i) {
                register(new SpongeBlockChangeFlag("NEIGHBOR_PHYSICS".toLowerCase(Locale.ENGLISH), i));
            } else if (Flags.NEIGHBOR_OBSERVER == i) {
                register(new SpongeBlockChangeFlag("NEIGHBOR_OBSERVER".toLowerCase(Locale.ENGLISH), i));
                // Since the next one is already considered as "ALL", it's not switched in
//            } else if (Flags.NEIGHBOR_PHYSICS_OBSERVER == i) {
//                register(new SpongeBlockChangeFlag("NEIGHBOR_PHYSICS_OBSERVER", i));
            } else if (Flags.PHYSICS_OBSERVER == i) {
                register(new SpongeBlockChangeFlag("PHYSICS_OBSERVER".toLowerCase(Locale.ENGLISH), i));
            } else {
                register(new SpongeBlockChangeFlag(builder.toString().toLowerCase(Locale.ENGLISH), i));
            }
        }
        RegistryHelper.mapFields(BlockChangeFlags.class, this.flags);
    }

    private void register(SpongeBlockChangeFlag flag) {
        this.maskedFlags.put(flag.getRawFlag(), flag);
        this.flags.put(flag.getName(), flag);
    }

    public Collection<SpongeBlockChangeFlag> getValues() {
        return Collections.unmodifiableCollection(this.flags.values());
    }

    public static final class Flag {

        public static final Flag NOTIFY_NEIGHBOR = new Flag("NEIGHBOR", Flags.NEIGHBOR_MASK);
        public static final Flag NOTIFY_CLIENTS = new Flag("NOTIFY_CLIENTS", Flags.NOTIFY_CLIENTS);
        public static final Flag IGNORE_RENDER = new Flag("IGNORE_RENDER", Flags.IGNORE_RENDER);
        public static final Flag FORCE_RE_RENDER = new Flag("FORCE_RE_RENDER", Flags.FORCE_RE_RENDER);
        public static final Flag IGNORE_OBSERVER = new Flag("OBSERVER", Flags.OBSERVER_MASK);
        public static final Flag IGNORE_PHYSICS = new Flag("PHYSICS", Flags.PHYSICS_MASK);

        private static final ImmutableList<Flag> flags = ImmutableList.of(NOTIFY_NEIGHBOR, NOTIFY_CLIENTS, IGNORE_RENDER, FORCE_RE_RENDER, IGNORE_OBSERVER, IGNORE_PHYSICS);

        private final String name;
        private final int mask;

        public static Collection<Flag> values() {
            return flags;
        }

        private Flag(String name, int mask) {
            this.name = name;
            this.mask = mask;
        }

    }

    public static final class Flags {

        public static final int NEIGHBOR_MASK               = 0b00000001;
        public static final int NOTIFY_CLIENTS              = 0b00000010;
        public static final int IGNORE_RENDER               = 0b00000100;
        public static final int FORCE_RE_RENDER             = 0b00001000;
        public static final int OBSERVER_MASK               = 0b00010000;
        public static final int PHYSICS_MASK                = 0b00100000;
        // All of these flags are what we "expose" to the API
        // The flags that are naturally inverted are already inverted here by being masked in
        // with the opposite OR.
        // Example: If we DO want physics, we don't include the physics flag, if we DON'T want physics, we | it in.
        public static final int ALL                         = NOTIFY_CLIENTS | NEIGHBOR_MASK;
        public static final int NONE                        = NOTIFY_CLIENTS | OBSERVER_MASK | PHYSICS_MASK;
        public static final int NEIGHBOR                    = NOTIFY_CLIENTS | NEIGHBOR_MASK | PHYSICS_MASK | OBSERVER_MASK;
        public static final int PHYSICS                     = NOTIFY_CLIENTS | OBSERVER_MASK;
        public static final int OBSERVER                    = NOTIFY_CLIENTS | PHYSICS_MASK;
        public static final int NEIGHBOR_PHSYICS            = NOTIFY_CLIENTS | NEIGHBOR_MASK | OBSERVER_MASK;
        public static final int NEIGHBOR_OBSERVER           = NOTIFY_CLIENTS | NEIGHBOR_MASK | PHYSICS_MASK;
        public static final int NEIGHBOR_PHYSICS_OBSERVER   = NOTIFY_CLIENTS | NEIGHBOR_MASK;
        public static final int PHYSICS_OBSERVER            = NOTIFY_CLIENTS;

    }
}
