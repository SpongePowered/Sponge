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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tristate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class MetricsCategory {

    @Setting(value = "global-state", comment = ""
            + "The global collection state that should be respected by all plugins that have no specified "
            + "collection state.\n"
            + "If 'undefined' then it is treated as disabled.")
    private Tristate globalState = Tristate.UNDEFINED;

    @Setting(value = "plugin-states",
            comment = "Plugin-specific collection states that override the global collection state.")
    private final Map<String, Tristate> pluginStates = new HashMap<>();

    public Tristate getGlobalCollectionState() {
        return this.globalState;
    }

    public Tristate getCollectionState(PluginContainer container) {
        return this.pluginStates.getOrDefault(container.getId(), Tristate.UNDEFINED);
    }

    public Map<String, Tristate> getCollectionStates() {
        return Collections.unmodifiableMap(this.pluginStates);
    }

}
