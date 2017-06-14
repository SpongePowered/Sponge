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
package org.spongepowered.common.mixin.core.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * @author gabizou - January 25th, 2016
 * Updated: February 2nd - Rewrite overwrite to use two redirects
 *
 * Normally this shouldn't be necessary, however, due to unforseen consequences
 * of creating block snapshots, there are corner cases where mod authors are
 * setting nulls into the compound for their tile entities. This overwrite
 * prevents an NPE crashing the game. A pretty warning message will be printed
 * out for the client to see and report to both Sponge and the mod author.
 */
@Mixin(NBTTagCompound.class)
public abstract class MixinNBTTagCompound extends NBTBase {

    private static final String SET_TAG = "Lnet/minecraft/nbt/NBTTagCompound;setTag(Ljava/lang/String;Lnet/minecraft/nbt/NBTBase;)V";

    @Shadow @Final private Map<String, NBTBase> tagMap;

    @Redirect(method = "copy", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTBase;copy()Lnet/minecraft/nbt/NBTBase;"))
    @Nullable
    private NBTBase onTagCopy(@Nullable NBTBase base) {
        return base == null ? null : base.copy();
    }

    @Redirect(method = "copy", at = @At(value = "INVOKE", target = SET_TAG))
    private void onCopySet(NBTTagCompound compound, String string, @Nullable NBTBase base) {
        if (base == null) {
            IllegalStateException exception = new IllegalStateException("There is a null NBTBase in the compound for key: " + string);
            SpongeImpl.getLogger().error("Printing out a stacktrace to catch an exception in performing an NBTTagCompound.copy!\n"
                                         + "If you are seeing this, then Sponge is preventing an exception from being thrown due to unforseen\n"
                                         + "possible bugs in any mods present. Please report this to SpongePowered and/or the relative mod\n"
                                         + "authors for the offending compound data!", exception);
        } else {
            compound.setTag(string, base);
        }
    }

}
