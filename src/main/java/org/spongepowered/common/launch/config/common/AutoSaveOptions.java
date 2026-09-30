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
package org.spongepowered.common.launch.config.common;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class AutoSaveOptions {

    @Setting("interval")
    @Comment("The tick interval used to execute the auto save. \n"
        + "Set to 0 to disable. (Default: 6000) \n"
        + "Note: 20 ticks is equivalent to 1 second.")
    public int interval = 6000;

    @Setting("batch-interval")
    @Comment("The tick interval used to process the items in \n"
        + "the auto save queue once the auto save has been triggered. \n"
        + "Setting this to 0 will execute all of the tasks in the same tick. (Default: 0) \n"
        + "Note: 20 ticks is equivalent to 1 second.")
    public int batchInterval = 0;

    @Setting("batch-amount")
    @Comment("The amount of items to process per batch. (Default: 1) \n"
        + "Note: Has no effect if batch-interval is disabled.")
    public int batchAmount = 1;

    @Comment("Log when a auto-saves is triggered. \n"
        + "Note: This may be spammy depending on the interval configured.")
    public boolean log = false;
}
