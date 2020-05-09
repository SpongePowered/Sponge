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
public class TileEntityActivationModCategory extends ConfigCategory {

    @Setting(value = "enabled",
            comment = "If 'false', tileentity activation rules for this mod will be ignored and always tick.")
    private boolean isEnabled = true;

    @Setting(value = "default-block-range",
            comment = "Default activation block range used for all tileentities unless overridden.")
    private Integer defaultBlockRange;

    @Setting(value = "default-tick-rate",
            comment = "Default tick rate used for all tileentities unless overridden.")
    private Integer defaultTickRate;

    @Setting(value = "block-range",
            comment = "A list of tile/block entities with block name as key and their range as value.")
    private Map<String, Integer> tileEntityRangeList = new HashMap<>();

    @Setting(value = "tick-rate",
            comment = "A list of tile/block entities with block name as key and their tick rate as value")
    private Map<String, Integer> tileEntityTickRateList = new HashMap<>();

    public TileEntityActivationModCategory() {
    }

    public TileEntityActivationModCategory(String modId) {
        if (modId.equalsIgnoreCase("computercraft")) {
            this.tileEntityRangeList.put("advanced_modem", 0);
            this.tileEntityRangeList.put("ccprinter", 0);
            this.tileEntityRangeList.put("diskdrive", 0);
            this.tileEntityRangeList.put("turtleex", 0);
            this.tileEntityRangeList.put("wiredmodem", 0);
            this.tileEntityRangeList.put("wirelessmodem", 0);
        } else if (modId.equalsIgnoreCase("plethora-core")) {
            this.tileEntityRangeList.put("plethora:manipulator", 0);
        }
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public Integer getDefaultBlockRange() {
        return this.defaultBlockRange;
    }

    public Integer getDefaultTickRate() {
        return this.defaultTickRate;
    }

    public Map<String, Integer> getTileEntityRangeList() {
        return this.tileEntityRangeList;
    }

    public Map<String, Integer> getTileEntityTickRateList() {
        return this.tileEntityTickRateList;
    }

}
