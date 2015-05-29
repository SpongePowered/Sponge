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
package org.spongepowered.common.data.component.item;

import com.google.common.base.Objects;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.item.DurabilityComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.common.data.component.AbstractIntComponent;

public class SpongeDurabilityComponent extends AbstractIntComponent<DurabilityComponent> implements DurabilityComponent {

    private boolean breakable = true;

    public SpongeDurabilityComponent(int maxDurability) {
        super(DurabilityComponent.class, 0, 0, maxDurability);
    }

    @Override
    public boolean isBreakable() {
        return this.breakable;
    }

    @Override
    public DurabilityComponent setBreakable(boolean breakable) {
        this.breakable = breakable;
        return this;
    }

    @Override
    public DurabilityComponent copy() {
        return new SpongeDurabilityComponent(this.getMaximum()).setValue(this.getValue()).setBreakable(this.breakable);
    }

    @Override
    public DurabilityComponent reset() {
        return setValue(getMinimum());
    }

    @Override
    public int compareTo(DurabilityComponent o) {
        return this.breakable ? o.isBreakable() ? this.getValue() - o.getValue() : -1 : o.isBreakable() ? 1 : this.getValue() - o.getValue();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Tokens.DURABILITY.getQuery(), this.getValue())
                .set(Tokens.UNBREAKABLE.getQuery(), this.breakable)
                .set(Tokens.MAX_DURABILITY.getQuery(), this.getMaximum());
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(this.breakable);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final SpongeDurabilityComponent other = (SpongeDurabilityComponent) obj;
        return Objects.equal(this.breakable, other.breakable);
    }
}
