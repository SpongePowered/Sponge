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
package org.spongepowered.common.data.builder.manipulator;

import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.common.util.Constants;

public class InvisibilityDataAddVanishUpdater implements DataContentUpdater {

    // TODO: 'Invisiblity' is a typo, was it a typo orgiginally or is this
    // updater not removing the correct data?
    public static final DataQuery INVISIBLE_COLLISION = DataQuery.of("InvisiblityIgnoresCollision");
    public static final DataQuery INVISIBLE_TARGET = DataQuery.of("InvisibilityPreventsTargeting");
    public static final DataQuery INVISIBLE = DataQuery.of("Invisible");

    public static final DataQuery VANISH_COLLISION = DataQuery.of("VanishIgnoresCollision");
    public static final DataQuery VANISH_TARGET = DataQuery.of("VanishPreventsTargeting");
    public static final DataQuery VANISH_GENERAL = DataQuery.of("Vanish");

    @Override
    public int getInputVersion() {
        return Constants.Sponge.InvisibilityData.INVISIBILITY_DATA_PRE_1_9;
    }

    @Override
    public int getOutputVersion() {
        return Constants.Sponge.InvisibilityData.INVISIBILITY_DATA_WITH_VANISH;
    }

    @Override
    public DataView update(DataView content) {

        // Get old data
        final boolean oldInvisible = content.getBoolean(INVISIBLE).get();
        final boolean oldCollision = content.getBoolean(INVISIBLE_COLLISION).get();
        final boolean oldTarget = content.getBoolean(INVISIBLE_TARGET).get();

        // Remove old queries - retain INVISIBLE because it's still used
        content.remove(INVISIBLE_COLLISION);
        content.remove(INVISIBLE_TARGET);

        // set new data
        content.set(VANISH_GENERAL, oldInvisible);
        content.set(VANISH_COLLISION, oldCollision);
        content.set(VANISH_TARGET, oldTarget);
        return content;
    }
}
