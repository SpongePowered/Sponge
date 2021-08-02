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
package org.spongepowered.vanilla.applaunch.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

// backport of a filter that exists in log4j 2.14, but not 2.11
public class DenyAllFilter extends AbstractFilter {

    @Override
    public Result filter(final LogEvent event) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final Message msg, final Throwable t
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final Object msg, final Throwable t
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object... params
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2,
        final Object p3
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2,
        final Object p3, final Object p4
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2,
        final Object p3, final Object p4, final Object p5
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2,
        final Object p3, final Object p4, final Object p5, final Object p6
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2,
        final Object p3, final Object p4, final Object p5, final Object p6, final Object p7
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2,
        final Object p3, final Object p4, final Object p5, final Object p6, final Object p7,
        final Object p8
    ) {
        return Result.DENY;
    }

    @Override
    public Result filter(
        final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2,
        final Object p3, final Object p4, final Object p5, final Object p6, final Object p7,
        final Object p8, final Object p9
    ) {
        return Result.DENY;
    }
}
