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

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class StructureModCategory {

    @Setting("enabled")
    @Comment("If 'false', this mod will never save its structures. This may\n"
                                          + "break some mod functionalities when requesting to locate their\n"
                                          + "structures in a World. If true, allows structures not overridden\n"
                                          + "in the section below to be saved by default. If you wish to find\n"
                                          + "a structure to prevent it being saved, enable 'auto-populate' and\n"
                                          + "restart the server/world instance.")
    private boolean isEnabled = true;
    @Setting("structures")
    @Comment("Per structure override. Having the value of 'false' will prevent\n"
                                             + "that specific named structure from saving.")
    private Map<String, Boolean> structureList = new HashMap<>();

    public StructureModCategory() {
    }

    public StructureModCategory(final String modId) {
        if (modId.equals("minecraft")) {
            this.structureList.put("mineshaft", false);
        }
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public Map<String, Boolean>  getStructureList() {
        return this.structureList;
    }
}
