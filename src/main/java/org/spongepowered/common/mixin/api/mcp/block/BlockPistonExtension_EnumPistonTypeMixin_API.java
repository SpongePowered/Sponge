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
package org.spongepowered.common.mixin.api.mcp.block;

import org.spongepowered.api.data.type.PistonType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

import javax.annotation.Nullable;
import net.minecraft.block.PistonHeadBlock;

@Mixin(PistonHeadBlock.EnumPistonType.class)
@Implements(@Interface(iface = PistonType.class, prefix = "piston$"))
public abstract class BlockPistonExtension_EnumPistonTypeMixin_API implements PistonType {

    @Shadow public abstract String shadow$getName();

    @Nullable private Translation api$translation;

    @Override
    public String getId() {
        return "minecraft:" + shadow$getName();
    }

    @Intrinsic
    public String piston$getName() {
        return shadow$getName();
    }

    @Override
    public Translation getTranslation() {
        if (this.api$translation == null) {
            final String internalName = shadow$getName();
            final String translationId;
            if ("normal".equals(internalName)) {
                translationId = "tile.pistonBase.name";
            } else if ("sticky".equals(internalName)) {
                translationId = "tile.pistonStickyBase.name";
            } else {
                translationId = "tile.pistonBase.name";
            }
            this.api$translation = new SpongeTranslation(translationId);
        }
        return this.api$translation;
    }

}
