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

import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

import javax.annotation.Nullable;
import net.minecraft.block.TallGrassBlock;

@Mixin(TallGrassBlock.EnumType.class)
@Implements(@Interface(iface = ShrubType.class, prefix = "shrub$"))
public abstract class BlockTallGrass_EnumTypeMixin_API implements ShrubType {

    @Shadow @Final private String name;

    @Nullable private Translation api$translation;

    public String shrub$getId() {
        return "minecraft:" + this.name;
    }

    @Intrinsic
    public String shrub$getName() {
        return this.name;
    }

    private Translation impl$resolveTranslation() {
        switch ((TallGrassBlock.EnumType) (Object) this) {
            case DEAD_BUSH:
                return new SpongeTranslation("tile.tallgrass.shrub.name");
            case FERN:
                return new SpongeTranslation("tile.tallgrass.fern.name");
            case GRASS:
                return new SpongeTranslation("tile.tallgrass.grass.name");
            default:
                return new SpongeTranslation("tile.tallgrass.name");
        }
    }

    public Translation shrub$getTranslation() {
        // Maybe move this to a @Inject at the end of the constructor
        if (this.api$translation == null) {
            this.api$translation = impl$resolveTranslation();
        }
        return this.api$translation;
    }

}
