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

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public final class TeleportHelperCategory {

    @Setting("force-blacklist")
    @Comment("If 'true', this blacklist will always be respected, otherwise, plugins can choose whether \n"
             + "or not to respect it.")
    public boolean forceBlacklist = false;

    @Setting("unsafe-floor-blocks")
    @Comment("Blocks that are listed here will not be selected by Sponge's safe \n"
             + "teleport routine as a safe floor block.")
    public final List<String> unsafeFloorBlocks = new ArrayList<>();

    @Setting("unsafe-body-blocks")
    @Comment("Blocks that are listed here will not be selected by Sponge's safe teleport routine as \n"
              + "a safe block for players to warp into. \n"
              + "You should only list blocks here that are incorrectly selected, solid blocks that prevent \n"
              + "movement are automatically excluded.")
    public final List<String> unsafeBlockBlocks = new ArrayList<>();
}
