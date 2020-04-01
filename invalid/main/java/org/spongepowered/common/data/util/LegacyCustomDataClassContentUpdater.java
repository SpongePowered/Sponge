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
package org.spongepowered.common.data.util;

import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class LegacyCustomDataClassContentUpdater implements DataContentUpdater {

    @Override
    public int getInputVersion() {
        return Constants.Sponge.CLASS_BASED_CUSTOM_DATA;
    }

    @Override
    public int getOutputVersion() {
        return Constants.Sponge.CUSTOM_DATA_WITH_DATA_IDS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public DataView update(DataView content) {
        final String className = content.getString(Constants.Sponge.DATA_CLASS).get();

        final Optional<DataRegistration<?, ?>> registration = SpongeManipulatorRegistry.getInstance().getRegistrationForLegacyId(className);
        if (!registration.isPresent()) {
            return content;
        }
        content.set(Constants.Sponge.DATA_ID, registration.get().getId());
        content.remove(Constants.Sponge.DATA_CLASS);
        return content;
    }
}
