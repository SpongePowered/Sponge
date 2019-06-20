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

import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class TimingsCategory extends ConfigCategory {

    @Setting
    private boolean verbose = false;

    @Setting
    private boolean enabled = true;

    @Setting("server-name-privacy")
    private boolean serverNamePrivacy = false;

    @Setting("hidden-config-entries")
    private List<String> hiddenConfigEntries = Lists.newArrayList("sponge.sql");

    @Setting("history-interval")
    private int historyInterval = 300;

    @Setting("history-length")
    private int historyLength = 3600;

    public boolean isVerbose() {
        return this.verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isServerNamePrivate() {
        return this.serverNamePrivacy;
    }

    public List<String> getHiddenConfigEntries() {
        return this.hiddenConfigEntries;
    }

    public int getHistoryInterval() {
        return this.historyInterval;
    }

    public void setHistoryInterval(int historyInterval) {
        this.historyInterval = historyInterval;
    }

    public int getHistoryLength() {
        return this.historyLength;
    }

    public void setHistoryLength(int historyLength) {
        this.historyLength = historyLength;
    }

}