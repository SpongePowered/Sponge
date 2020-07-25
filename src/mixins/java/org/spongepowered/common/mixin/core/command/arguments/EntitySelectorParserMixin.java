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
package org.spongepowered.common.mixin.core.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.EntitySelectorParser;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.command.selector.SelectorType;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.command.arguments.EntityOptionsAccessor;
import org.spongepowered.common.accessor.command.arguments.EntityOptions_OptionHandlerAccessor;
import org.spongepowered.common.bridge.command.arguments.EntitySelectorParserBridge;

@Mixin(EntitySelectorParser.class)
public abstract class EntitySelectorParserMixin implements EntitySelectorParserBridge {

    @Shadow protected abstract void shadow$parseSelector() throws CommandSyntaxException;

    @Shadow @Final private StringReader reader;
    private Tristate impl$overrideInvert = Tristate.UNDEFINED;
    @Nullable private StringReader impl$readerOverride;

    @Override
    public void bridge$parseSelector(final SelectorType selectorType) throws CommandSyntaxException {
        try {
            this.impl$readerOverride = new StringReader(selectorType.selectorToken());
            this.impl$readerOverride.skip(); // skip over the @ symbol
            this.shadow$parseSelector();
        } finally {
            this.impl$readerOverride = null;
            this.impl$overrideInvert = Tristate.UNDEFINED;
        }
    }

    @Override
    public void bridge$handleValue(final String id, final String input, final Tristate shouldInvert) throws CommandSyntaxException {
        try {
            this.impl$overrideInvert = shouldInvert;
            this.impl$readerOverride = new StringReader(input);
            final EntityOptions_OptionHandlerAccessor option = EntityOptionsAccessor.accessor$getREGISTRY().get(id);
            if (option.accessor$getCanHandle().test((EntitySelectorParser) (Object) this)) {
                option.accessor$getHandler().handle((EntitySelectorParser) (Object) this);
            }
        } finally {
            this.impl$readerOverride = null;
            this.impl$overrideInvert = Tristate.UNDEFINED;
        }
    }

    @Inject(method = "getReader", at = @At("HEAD"), cancellable = true)
    private void impl$overrideReaderIfSet(final CallbackInfoReturnable<StringReader> cir) {
        if (this.impl$readerOverride != null) {
            cir.setReturnValue(this.impl$readerOverride);
        }
    }

    @Inject(method = "shouldInvertValue", at = @At("HEAD"), cancellable = true)
    private void impl$overrideShouldInvertIfSet(final CallbackInfoReturnable<Boolean> cir) {
        if (this.impl$overrideInvert != Tristate.UNDEFINED) {
            cir.setReturnValue(this.impl$overrideInvert.asBoolean());
        }
    }

    // This intended to redirect ALL accesses.
    @Redirect(method = "parseSelector",
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/command/arguments/EntitySelectorParser;reader:Lcom/mojang/brigadier/StringReader;"))
    private StringReader impl$redirectParseSelectorReaderAccesses(final EntitySelectorParser entitySelectorParser) {
        if (this.impl$readerOverride != null) {
            return this.impl$readerOverride;
        }
        return this.reader;
    }

}
