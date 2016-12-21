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
package org.spongepowered.common.statistic;

import com.google.common.base.CaseFormat;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.common.interfaces.statistic.IMixinStatBase;

import java.text.NumberFormat;

import javax.annotation.Nullable;

public interface SpongeStatistic extends Statistic {

    @Override
    default String getId() {
        String spongeId = getSpongeId();
        if (spongeId == null) {
            spongeId = getMinecraftId();
            int prefixStop = spongeId.indexOf(".");
            if (prefixStop != -1) {
                spongeId = spongeId.substring(prefixStop + 1);
            }
            spongeId = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, spongeId);
            setSpongeId(spongeId);
        }
        return spongeId;
    }

    @Nullable
    String getSpongeId();

    void setSpongeId(String id);

    String getMinecraftId();

    @Override
    default NumberFormat getFormat() {
        return ((IMixinStatBase) this).format();
    }

}
