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

import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeCommon;

import java.util.Map;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * @author Zidane - Minecraft 1.14.4
 *
 * Normally this shouldn't be necessary, however, due to unforseen consequences
 * of creating block snapshots, there are corner cases where mod authors are
 * setting nulls into the compound for their tile entities. This overwrite
 * prevents an NPE crashing the game. A pretty warning message will be printed
 * out for the client to see and report to both Sponge and the mod author.
 */
@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin {

    // @formatter:off
    @Shadow @Final private Map<String, Tag> tags;
    // @formatter:on

    @Redirect(method = "copy", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/Tag;copy()Lnet/minecraft/nbt/Tag;"))
    @Nullable
    private Tag impl$checkForOverflowOnCopy(Tag inbt) {
        try {
            return inbt == null ? null : inbt.copy();
        } catch (StackOverflowError e) {
            final PrettyPrinter printer = new PrettyPrinter(60)
                .add("StackOverflow from trying to copy this compound")
                .centre()
                .hr();
            printer.addWrapped(70, "Sponge caught a stack overflow error, printing out some special"
                                   + " handling and printouts to assist in finding out where this"
                                   + " recursion is coming from.");
            printer.add();
            try {
                printer.addWrapped(80, "%s : %s", "This compound", this);
            } catch (Throwable error) {
                printer.addWrapped(80, "Unable to get the string of this compound. Printing out some of the entries to better assist");

                for (final Map.Entry<String, Tag> entry : this.tags.entrySet()) {
                    try {
                        printer.addWrapped(80, "%s : %s", entry.getKey(), entry.getValue());
                    } catch (Throwable throwable) {
                        printer.add();
                        printer.addWrapped(80, "The offending key entry is belonging to " + entry.getKey());
                        break;
                    }
                }
            }
            printer.add();
            printer.log(SpongeCommon.logger(), Level.ERROR);
            return null;
        }
    }

    @Redirect(method = "copy", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"))
    private Tag impl$checkForNullNBTValuesDuringCopy(CompoundTag compound, String key, Tag value) {
        if (value == null) {
            final IllegalStateException exception = new IllegalStateException("There is a null NBT component in the compound for key: " + key);
            SpongeCommon.logger().error("Printing out a stacktrace to catch an exception in performing an NBTTagCompound.copy!\n"
                                         + "If you are seeing this, then Sponge is preventing an exception from being thrown due to unforseen\n"
                                         + "possible bugs in any mods present. Please report this to SpongePowered and/or the relative mod\n"
                                         + "authors for the offending compound data!", exception);
        } else {
            compound.put(key, value);
        }

        return value;
    }

}
