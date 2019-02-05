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
package org.spongepowered.common.data.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.TypeReferences;
import org.spongepowered.common.data.util.NbtDataUtil;

public final class SpongeEntityNotifierFix extends DataFix {

    public SpongeEntityNotifierFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    // <root> -> ForgeData -> SpongeData -> Notifier -> Two Longs (uuid_least/uuid_most)
    // Goal: uuid_least -> UUIDLeast, uuid_most -> UUIDMost
    @Override
    protected TypeRewriteRule makeRule() {
        final Type<?> type = this.getInputSchema().getType(TypeReferences.ENTITY);
        final OpticFinder<?> forgeData = type.findField(NbtDataUtil.FORGE_DATA);
        final OpticFinder<?> spongeData = forgeData.type().findField(NbtDataUtil.SPONGE_DATA);
        final OpticFinder<?> notifier = spongeData.type().findField(NbtDataUtil.SPONGE_ENTITY_NOTIFIER);
        return this.fixTypeEverywhereTyped("SpongeEntityNotifierFix", type, level -> level.updateTyped(notifier,
            item -> item.update(DSL.remainderFinder(), this::fixTag)));
    }

    private Dynamic<?> fixTag(final Dynamic<?> dynamic) {
        final Number least = dynamic.getNumber(NbtDataUtil.Deprecated.World.WORLD_UUID_LEAST_1_8).orElse(null);
        final Number most = dynamic.getNumber(NbtDataUtil.Deprecated.World.WORLD_UUID_MOST_1_8).orElse(null);

        if(least != null && most != null) {
            return dynamic
                .remove(NbtDataUtil.Deprecated.World.WORLD_UUID_LEAST_1_8)
                .remove(NbtDataUtil.Deprecated.World.WORLD_UUID_MOST_1_8)
                .set(NbtDataUtil.UUID + "Least", dynamic.createLong(least.longValue()))
                .set(NbtDataUtil.UUID + "Most", dynamic.createLong(most.longValue()));
        }

        return dynamic;
    }
}
