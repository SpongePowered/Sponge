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
package org.spongepowered.common.data.component.entity;

import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.TargetLivingComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.component.AbstractListComponent;

import java.util.List;
import java.util.UUID;

public class SpongeTargetLivingComponent extends AbstractListComponent<Living, TargetLivingComponent> implements TargetLivingComponent {

    public SpongeTargetLivingComponent() {
        super(TargetLivingComponent.class);
    }

    @Override
    public TargetLivingComponent copy() {
        return new SpongeTargetLivingComponent().set(this.elementList);
    }

    @Override
    public int compareTo(TargetLivingComponent o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        List<UUID> entityIds = Lists.newArrayList();
        for (Living living : this.elementList) {
            entityIds.add(living.getUniqueId());
        }
        return new MemoryDataContainer().set(Tokens.LIVING_TARGETS.getQuery(), entityIds);
    }
}
