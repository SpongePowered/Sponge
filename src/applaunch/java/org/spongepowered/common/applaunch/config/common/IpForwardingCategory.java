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
public class IpForwardingCategory {

    @Setting
    @Comment(
        "The IP forwarding mode to use with a proxy. Supported values:\n"
             + "  - NONE: Do not forward IP addresses\n"
             + "  - LEGACY: Use the BungeeCord/pre-1.13 protocol for IP forwarding (CAUTION: This protocol is insecure)\n"
             + "  - MODERN: Use the Velocity protocol for IP forwarding\n"
            + "When any forwarding mode but NONE is selected, the server will be "
            + "put into offline mode and will only accept connections from proxies."
    )
    public Mode mode = Mode.NONE;

    @Setting
    @Comment("The player info forwarding secret from your Velocity configuration.\n"
                 + "Only used with 'MODERN' forwarding mode.")
    public String secret = "";

    public enum Mode {
        NONE,
        LEGACY,
        MODERN
    }

}
