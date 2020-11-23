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
package org.spongepowered.common.config.inheritable;

import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class BlockEntityActivationModCategory {

    @Setting
    @Comment("If 'false', tileentity activation rules for this mod will be ignored and always tick.")
    private boolean enabled = true;
    @Setting
    private Integer defaultBlockRange;
    @Setting
    private Integer defaultTickRate;
    @Setting("block-range")
    private Map<String, Integer> tileEntityRangeList = new HashMap<>();
    @Setting("tick-rate")
    private Map<String, Integer> tileEntityTickRateList = new HashMap<>();

    public BlockEntityActivationModCategory() {
    }

    public BlockEntityActivationModCategory(final String modId) {
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
        return this.enabled;
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
