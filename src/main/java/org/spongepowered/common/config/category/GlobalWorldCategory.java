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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ConfigSerializable
public class GlobalWorldCategory extends WorldCategory {

    @Setting(value = "auto-player-save-interval", comment = 
            "The auto-save tick interval used when saving global player data. (Default: 900)"
            + "\nNote: 20 ticks is equivalent to 1 second. Set to 0 to disable.")
    private int autoPlayerSaveInterval = 900;

    @Setting(value = "leaf-decay", comment = "Enable to allow natural leaf decay.")
    private boolean leafDecay = true;

    @Setting(value = "gameprofile-lookup-batch-size", comment = 
            "The amount of GameProfile requests to make against Mojang's session server. (Default: 1)"
            + "\nNote: Mojang accepts a maximum of 600 requests every 10 minutes from a single IP address."
            + "\nIf you are running multiple servers behind the same IP, it is recommended to raise the 'gameprofile-task-interval' setting"
            + "\nin order to compensate for the amount requests being sent."
            + "\nFinally, if set to 0 or less, the default batch size will be used."
            + "\nFor more information visit http://wiki.vg/Mojang_API")
    private int gameProfileLookupBatchSize = 1;

    @Setting(value = "gameprofile-lookup-task-interval", comment = 
            "The interval, in seconds, used by the GameProfileQueryTask to process queued gameprofile requests. (Default: 4)"
            + "\nNote: This setting should be raised if you experience the following error:"
            + "\n\"The client has sent too many requests within a certain amount of time\"."
            + "\nFinally, if set to 0 or less, the default interval will be used.")
    private int gameProfileQueryTaskInterval = 4;

    @Setting(value = "invalid-lookup-uuids", comment = 
            "The list of uuid's that should never perform a lookup against Mojang's session server."
            + "\nNote: If you are using SpongeForge, make sure to enter any mod fake player's UUID to this list.")
    private List<UUID> invalidLookupUuids = new ArrayList<>();

    public GlobalWorldCategory() {
        this.invalidLookupUuids.add(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        this.invalidLookupUuids.add(UUID.fromString("0d0c4ca0-4ff1-11e4-916c-0800200c9a66")); // ComputerCraft FakePlayer
        this.invalidLookupUuids.add(UUID.fromString("41c82c87-7afb-4024-ba57-13d2c99cae77")); // Forge FakePlayer
    }

    public int getAutoPlayerSaveInterval() {
        return this.autoPlayerSaveInterval;
    }

    public boolean getLeafDecay() {
        return this.leafDecay;
    }

    public void setLeafDecay(boolean flag) {
        this.leafDecay = flag;
    }

    public int getGameProfileLookupBatchSize() {
        if (this.gameProfileLookupBatchSize <= 0) {
            this.gameProfileLookupBatchSize = 1;
        }
        return this.gameProfileLookupBatchSize;
    }

    public int getGameProfileQueryTaskInterval() {
        if (this.gameProfileQueryTaskInterval <= 0) {
            this.gameProfileQueryTaskInterval = 4;
        }
        return this.gameProfileQueryTaskInterval;
    }

    public List<UUID> getInvalidLookupUuids() {
        return this.invalidLookupUuids;
    }
}
