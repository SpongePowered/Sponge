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
package org.spongepowered.common.mixin.api.mcp.entity.projectile;

import org.spongepowered.api.data.type.PickupRule;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Locale;

import javax.annotation.Nullable;
import net.minecraft.entity.projectile.AbstractArrowEntity;

@Mixin(AbstractArrowEntity.PickupStatus.class)
public class EntityArrow_PickupStatusMixin_API implements PickupRule {

    @Nullable private String api$id;
    @Nullable private String api$name;

    @Override
    public String getId() {
        if (this.api$id == null) {
            this.api$id = this.getName().toLowerCase(Locale.ENGLISH);
        }
        return this.api$id;
    }

    @Override
    public String getName() {
        if (this.api$name == null) {
            this.api$name = ((AbstractArrowEntity.PickupStatus) (Object) this).name();
        }
        return this.api$name;
    }

}
