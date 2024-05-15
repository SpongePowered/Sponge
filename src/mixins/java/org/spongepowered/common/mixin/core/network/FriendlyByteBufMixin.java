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
package org.spongepowered.common.mixin.core.network;

import net.minecraft.network.FriendlyByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.network.FriendlyByteBufBridge;

import java.util.Locale;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin implements FriendlyByteBufBridge {

    private @Nullable Locale impl$locale;

    // TODO mc is using codec now to write to the Buffer, it might even not be a FriendlyByteBuf anymore
//    @ModifyVariable(method = "writeComponent", at = @At("HEAD"), argsOnly = true)
//    private Component localizeComponent(final Component input) {
//        return NativeComponentRenderer.apply(input, this.impl$locale == null ? Locales.DEFAULT : this.impl$locale);
//    }

//    @Override
//    public CompoundTag bridge$renderItemComponents(CompoundTag tag) {
//        if (tag == null || !tag.contains(Constants.Item.ITEM_DISPLAY, 10)) {
//            return tag;
//        }
//
//        final Locale locale = this.impl$locale == null ? Locales.DEFAULT : this.impl$locale;
//        CompoundTag display = tag.getCompound(Constants.Item.ITEM_DISPLAY);
//        boolean copy = true;
//
//        if (display.contains(Constants.Item.ITEM_NAME, 8)) {
//            final String nameStr = display.getString(Constants.Item.ITEM_NAME);
//            final Component name = Component.Serializer.fromJson(nameStr);
//            final Component renderedName = NativeComponentRenderer.apply(name, locale);
//
//            if (!renderedName.equals(name)) {
//                if (copy) {
//                    tag = tag.copy();
//                    display = tag.getCompound(Constants.Item.ITEM_DISPLAY);
//                    copy = false;
//                }
//
//                display.putString(Constants.Item.ITEM_ORIGINAL_NAME, nameStr);
//                display.putString(Constants.Item.ITEM_NAME, Component.Serializer.toJson(renderedName));
//            }
//        }
//
//        if (display.contains(Constants.Item.ITEM_LORE, 9)) {
//            final ListTag lore = display.getList(Constants.Item.ITEM_LORE, 8);
//
//            final Component[] renderedLines = new Component[lore.size()];
//            boolean equal = true;
//
//            for (int i = 0; i < renderedLines.length; i++) {
//                final String lineStr = lore.getString(i);
//                final Component line = Component.Serializer.fromJson(lineStr);
//                final Component renderedLine = NativeComponentRenderer.apply(line, locale);
//
//                renderedLines[i] = renderedLine;
//                equal = equal && renderedLine.equals(line);
//            }
//
//            if (!equal) {
//                if (copy) {
//                    tag = tag.copy();
//                    display = tag.getCompound(Constants.Item.ITEM_DISPLAY);
//                    copy = false;
//                }
//
//                final ListTag newLore = new ListTag();
//                for (Component renderedLine : renderedLines) {
//                    newLore.add(StringTag.valueOf(Component.Serializer.toJson(renderedLine)));
//                }
//
//                display.put(Constants.Item.ITEM_ORIGINAL_LORE, lore);
//                display.put(Constants.Item.ITEM_LORE, newLore);
//            }
//        }
//        return tag;
//    }
//
//    @Redirect(method = "readItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readNbt()Lnet/minecraft/nbt/CompoundTag;"))
//    public CompoundTag restoreItemComponents(final FriendlyByteBuf buf) {
//        CompoundTag tag = buf.readNbt();
//        if (tag == null || !tag.contains(Constants.Item.ITEM_DISPLAY, 10)) {
//            return tag;
//        }
//
//        final CompoundTag display = tag.getCompound(Constants.Item.ITEM_DISPLAY);
//
//        if (display.contains(Constants.Item.ITEM_ORIGINAL_NAME, 8)) {
//            final String name = display.getString(Constants.Item.ITEM_ORIGINAL_NAME);
//            display.remove(Constants.Item.ITEM_ORIGINAL_NAME);
//            display.putString(Constants.Item.ITEM_NAME, name);
//        }
//
//        if (display.contains(Constants.Item.ITEM_ORIGINAL_LORE, 9)) {
//            final ListTag lore = display.getList(Constants.Item.ITEM_ORIGINAL_LORE, 8);
//            display.remove(Constants.Item.ITEM_ORIGINAL_LORE);
//            display.put(Constants.Item.ITEM_LORE, lore);
//        }
//        return tag;
//    }

    @Override
    public void bridge$setLocale(final Locale locale) {
        this.impl$locale = locale;
    }
}
