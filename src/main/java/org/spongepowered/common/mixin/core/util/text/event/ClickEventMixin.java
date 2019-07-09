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

import net.minecraft.util.text.event.ClickEvent;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.util.text.event.ClickEventBridge;
import org.spongepowered.common.text.action.SpongeCallbackHolder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ClickEvent.class)
public abstract class ClickEventMixin implements ClickEventBridge {

    @Shadow @Final private ClickEvent.Action action;
    @Shadow @Final private String value;

    private ClickAction<?> handle;
    private volatile boolean initialized;

    @Override
    public ClickAction<?> bridge$getHandle() {
        if (!this.initialized) {
            try {
                switch (this.action) {
                    case OPEN_URL:
                        try {
                            bridge$setHandle(TextActions.openUrl(new URL(this.value)));
                        } catch (MalformedURLException e) {
                            SpongeImpl.getLogger().debug("Tried to parse invalid URL \"{}\": {}", this.value, e.getMessage());
                        }
                        break;
                    case RUN_COMMAND:
                        if (this.value.startsWith(SpongeCallbackHolder.CALLBACK_COMMAND_QUALIFIED)) {
                            try {
                                UUID callbackId = UUID.fromString(this.value.substring(SpongeCallbackHolder.CALLBACK_COMMAND_QUALIFIED.length() + 1));
                                Optional<Consumer<CommandSource>> callback = SpongeCallbackHolder.getInstance().getCallbackForUUID(callbackId);
                                if (callback.isPresent()) {
                                    bridge$setHandle(TextActions.executeCallback(callback.get()));
                                    break;
                                }
                            } catch (IllegalArgumentException ex) {
                            }
                        }
                        bridge$setHandle(TextActions.runCommand(this.value));
                        break;
                    case SUGGEST_COMMAND:
                        bridge$setHandle(TextActions.suggestCommand(this.value));
                        break;
                    case CHANGE_PAGE:
                        bridge$setHandle(TextActions.changePage(Integer.parseInt(this.value)));
                        break;
                    default:
                }
            } finally {
                this.initialized = true;
            }
        }

        return this.handle;
    }

    @Override
    public void bridge$setHandle(ClickAction<?> handle) {
        if (this.initialized) {
            return;
        }

        this.handle = checkNotNull(handle, "handle");
        this.initialized = true;
    }

}
