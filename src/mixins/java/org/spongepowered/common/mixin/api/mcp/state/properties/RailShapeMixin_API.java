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
package org.spongepowered.common.mixin.api.mcp.state.properties;

import net.minecraft.state.properties.RailShape;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.plugin.PluginContainer;

@Mixin(RailShape.class)
public abstract class RailShapeMixin_API implements RailDirection {

    private ResourceKey api$key;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void api$setKey(String enumName, int ordinal, String p_i225774_3_, CallbackInfo ci) {
        this.api$key = ResourceKey.of(SpongeCommon.getActivePlugin(), p_i225774_3_.toLowerCase());
    }

    @Override
    public ResourceKey getKey() {
        return this.api$key;
    }

    @Override
    public RailDirection cycleNext() {
        final SimpleRegistry<RailDirection> registry = SpongeCommon.getRegistry().getCatalogRegistry().getRegistry(RailDirection.class);
        RailDirection next = registry.getByValue(((RailShape) (Object) this).ordinal() + 1);
        if (next == null) {
            next = registry.getByValue(0);
        }
        return next;
    }
}
