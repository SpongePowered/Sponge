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
package org.spongepowered.common.mixin.core.util.text.event;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.HoverEvent;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.util.text.event.HoverEventBridge;
import org.spongepowered.common.bridge.util.text.ITextComponentBridge;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import java.util.UUID;

@Mixin(HoverEvent.class)
public abstract class HoverEventMixin implements HoverEventBridge {

    @Shadow @Final private HoverEvent.Action action;
    @Shadow @Final private ITextComponent value;

    private HoverAction<?> handle;
    private boolean initialized;

    @Override
    public HoverAction<?> bridge$getHandle() {
        if (!this.initialized) {
            try {
                // This is inefficient, but at least we only need to do it once
                switch (this.action) {
                    case SHOW_TEXT:
                        bridge$setHandle(TextActions.showText(((ITextComponentBridge) this.value).bridge$toText()));
                        break;
                    case SHOW_ITEM:
                        bridge$setHandle(TextActions.showItem(ItemStackUtil.snapshotOf(new net.minecraft.item.ItemStack(loadNbt()))));
                        break;
                    case SHOW_ENTITY:
                        CompoundNBT nbt = loadNbt();
                        String name = nbt.func_74779_i("name");
                        EntityType type = null;
                        if (nbt.func_150297_b("type", Constants.NBT.TAG_STRING)) {
                            type = SpongeImpl.getGame().getRegistry().getType(EntityType.class, name).orElse(null);
                        }

                        UUID uniqueId = UUID.fromString(nbt.func_74779_i("id"));
                        bridge$setHandle(TextActions.showEntity(uniqueId, name, type));
                        break;
                    default:
                }
            } finally {
                this.initialized = true;
            }
        }

        return this.handle;
    }

    private CompoundNBT loadNbt() {
        try {
            return checkNotNull(JsonToNBT.func_180713_a(this.value.func_150260_c()), "NBT");
        } catch (NBTException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bridge$setHandle(HoverAction<?> handle) {
        if (this.initialized) {
            return;
        }

        this.handle = checkNotNull(handle, "handle");
        this.initialized = true;
    }

}
