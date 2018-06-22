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

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class BlockTrackerCategory extends ConfigCategory {

    @Setting(value = "auto-populate", comment = "If 'true', newly discovered blocks will be added to this config with default settings.")
    private boolean autoPopulate = false;

    @Setting(value = "mods", comment = "Per-mod block id mappings for controlling tracking behavior")
    private Map<String, BlockTrackerModCategory> modMapping = new HashMap<>();

    public BlockTrackerCategory() {
        this.modMapping.put("minecraft", new BlockTrackerModCategory("minecraft"));
        this.modMapping.put("extrautils2", new BlockTrackerModCategory("extrautils2"));
    }

    public Map<String, BlockTrackerModCategory> getModMappings() {
        return this.modMapping;
    }

    public boolean autoPopulateData() {
        return this.autoPopulate;
    }

}
