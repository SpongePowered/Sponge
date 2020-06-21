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
package org.spongepowered.common.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CommandsHiddenCategory extends ConfigCategory {

    @Setting(value = "hide-on-discovery-attempt",
             comment = "If this is true, when a user tries to tab complete a command, or use \"/sponge which\" or\n"
                     + "\"/sponge:help\" this prevents commands a user does not have permission for from being completed.\n\n"
                     + "Note that some commands may not show up during tab complete if a user does not have permission\n"
                     + "regardless of this setting.")
    private boolean hideDuringDiscovery = true;

    @Setting(value = "hide-on-execution-attempt",
             comment = "If this is true, when a user tries to use a command they don't have permission for, Sponge\n"
                     + "will act as if the command doesn't exist, rather than showing a no permissions message.")
    private boolean hideDuringExecution = false;

    public boolean isHideDuringDiscovery() {
        return this.hideDuringDiscovery;
    }

    public boolean isHideDuringExecution() {
        return this.hideDuringExecution;
    }

}
