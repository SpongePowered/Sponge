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
package org.spongepowered.common.applaunch.config.common;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class OptimizationCategory {

    @Setting("cache-tameable-owners")
    @Comment("Caches tameable entities owners to avoid constant lookups against data watchers. If mods \n"
             + "cause issues, disable this.")
    public boolean cacheTameableOwners = true;

    @Setting(value = "bell-leak-fix")
    @Comment("Bells will store references of nearby entities when rang.\n"
            + "The entity list is never cleared, thus leaking memory until\n"
            + "the chunk is unloaded. Since the entity list is reused if the\n"
            + "bell is rang again within 60 ticks, this provides an option to\n"
            + "clear the list if it is not needed anymore.")
    public boolean bellLeak = true;

    @Setting("enable-lazydfu")
    @Comment("By default, Vanilla 'warms-up' all migration rules for\n"
            + "every Minecraft version when the game starts. This often\n"
            + "causes a period of extremely high CPU usage when the game\n"
            + "starts, often for no benefit since the typical pattern is\n"
            + "that most chunks do not have to be migrated, or only have\n"
            + "to be migrated from just a few versions. This option disables\n"
            + "migration rules from being 'warmed-up' and instead forces them\n"
            + "to be generated on demand. This is a very safe optimization and\n"
            + "should usually remain enabled.")
    public boolean enableLazyDFU = true;
}
