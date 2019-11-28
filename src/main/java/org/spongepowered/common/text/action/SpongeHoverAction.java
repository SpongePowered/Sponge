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
package org.spongepowered.common.text.action;

import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.bridge.util.text.event.HoverEventBridge;
import org.spongepowered.common.text.SpongeTexts;

public class SpongeHoverAction {

    private SpongeHoverAction() {
    }

    private static HoverEvent.Action getType(HoverAction<?> action) {
        if (action instanceof HoverAction.ShowEntity) {
            return HoverEvent.Action.SHOW_ENTITY;
        } else if (action instanceof HoverAction.ShowItem) {
            return HoverEvent.Action.SHOW_ITEM;
        } else if (action instanceof HoverAction.ShowText) {
            return HoverEvent.Action.SHOW_TEXT;
        }

        throw new UnsupportedOperationException(action.getClass().toString());
    }

    public static HoverEvent getHandle(HoverAction<?> action) {
        HoverEvent.Action type = getType(action);
        ITextComponent component;

        switch (type) {
            case SHOW_ENTITY: {
                HoverAction.ShowEntity.Ref entity = ((HoverAction.ShowEntity) action).getResult();

                CompoundNBT nbt = new CompoundNBT();
                nbt.func_74778_a("id", entity.getUniqueId().toString());

                if (entity.getType().isPresent()) {
                    nbt.func_74778_a("type", EntityList.func_191306_a(((SpongeEntityType) entity.getType().get()).entityClass).toString());
                }

                nbt.func_74778_a("name", entity.getName());
                component = new StringTextComponent(nbt.toString());
                break;
            }
            case SHOW_ITEM: {
                ItemStack item = (ItemStack) ((ItemStackSnapshot) action.getResult()).createStack();
                CompoundNBT nbt = new CompoundNBT();
                item.func_77955_b(nbt);
                component = new StringTextComponent(nbt.toString());
                break;
            }
            case SHOW_TEXT:
                component = SpongeTexts.toComponent((Text) action.getResult());
                break;
            default:
                throw new AssertionError();
        }

        HoverEvent event = new HoverEvent(type, component);
        ((HoverEventBridge) event).bridge$setHandle(action);
        return event;
    }

}
