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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ConfigSerializable
public class StatisticsCategory {

    @Setting(value = "default-permission", comment = "Determines whether plugins that are newly added are allowed to perform\n"
                                        + "data/statistics collection by default. Plugins detected by Sponge will be added "
                                        + "to the \"plugin-permissions\" section with this value.\n\n"
                                        + "Set to true to enable stats gathering by default, false otherwise.")
    private boolean defaultPermission = false;

    @Setting(value = "plugin-permissions", comment = "Provides (or revokes) permission for stat gathering on a per plugin basis.\n"
                                                   + "Entries should be in the format \"plugin-id=<true|false>\".\n\n"
                                                   + "Deleting an entry from this list will reset it to the default specified in\n"
                                                   + "\"default-permission\"")
    private Map<String, Boolean> perPluginPermissions = new HashMap<>();

    public boolean isGloballyEnabled() {
        return this.defaultPermission;
    }

    public Optional<Boolean> getPluginPermission(PluginContainer container) {
        return Optional.ofNullable(this.perPluginPermissions.get(container.getId()));
    }

    public Map<String, Boolean> getPluginPermissions() {
        return new HashMap<>(this.perPluginPermissions);
    }

}
