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
package org.spongepowered.common.command.manager;

import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.command.CommandSourceProviderBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;

public final class SpongeCommandCauseFactory implements CommandCause.Factory {

    public static final SpongeCommandCauseFactory INSTANCE = new SpongeCommandCauseFactory();

    private SpongeCommandCauseFactory() { }

    @Override
    @NonNull
    public CommandCause create() {
        final Cause cause = PhaseTracker.getCauseStackManager().getCurrentCause();
        final CommandSource commandSource =
                cause.first(CommandSourceProviderBridge.class).orElseGet(() ->
                        (CommandSourceProviderBridge) SpongeCommon.getServer()).bridge$getCommandSource(cause);

        // We don't want the command source to have altered the cause here, so we reset it back to what it was
        // (in the ctor of CommandSource, it will add the current source to the cause - that's for if the
        // source is created elsewhere, not here)
        ((CommandSourceBridge) commandSource).bridge$setCause(cause);
        return (CommandCause) commandSource;
    }

}
