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

import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public final class ModuleCategory {

    @Setting
    @Comment("Enables support for BungeeCord and Velocity IP forwarding.\n" +
        "Additional options must be configured in the 'ip-forwarding' configuration section.")
    public boolean ipForwarding = false;

    @Setting("entity-activation-range")
    public boolean entityActivationRange = true;

    @Setting("block-entity-activation")
    @Comment("Controls block range and tick rate of block entities. \n"
             + "Use with caution as this can break intended functionality.")
    public boolean blockEntityActivationRange = false;

    @Setting("entity-collision")
    public boolean entityCollision = true;

    @Setting
    public boolean timings = true;

    @Setting
    @Comment("Controls whether any exploit patches are applied.\n"
             + "If there are issues with any specific exploits, please\n"
             + "test in the exploit category first, before disabling all\n"
             + "exploits with this toggle.")
    public boolean exploits = true;

    @Setting
    public boolean optimizations = true;

    @Setting
    public boolean tracking = true;

    @Setting("real-time")
    @Comment("Use real (wall) time instead of ticks as much as possible")
    public boolean realTime = false;

    @Setting("movement-checks")
    @Comment("Allows configuring Vanilla movement and speed checks")
    public boolean movementChecks = false;
}
