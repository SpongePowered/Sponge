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
package org.spongepowered.forge.mixin.core.minecraftforge.event.block;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.forge.launch.bridge.event.ForgeEventBridge_Forge;

@Mixin(value = PlayerInteractEvent.RightClickBlock.class, remap = false)
public abstract class PlayerInteractEvent_RightClickBlockMixin_Forge implements ForgeEventBridge_Forge {

    // @formatter:off
    @Shadow
    public abstract BlockHitResult shadow$getHitVec();
    @Shadow
    public abstract void shadow$setUseBlock(net.minecraftforge.eventbus.api.Event.Result e);
    @Shadow public abstract net.minecraftforge.eventbus.api.Event.Result shadow$getUseBlock();
    @Shadow
    public abstract void shadow$setUseItem(net.minecraftforge.eventbus.api.Event.Result e);
    @Shadow public abstract net.minecraftforge.eventbus.api.Event.Result shadow$getUseItem();
    // @formatter:on

    @Override
    public void bridge$syncFrom(Event event) {
        // spongeEvent.useBlockResult() // Do I need this?
    }

    @Override
    public void bridge$syncTo(Event event) {
        final InteractBlockEvent.Secondary spongeEvent = (InteractBlockEvent.Secondary) event;
        spongeEvent.setCancelled(((net.minecraftforge.eventbus.api.Event) (Object) this).isCanceled());
    }

    @Override
    public @Nullable Event bridge$createSpongeEvent() {
//        final BlockSnapshot block, final Vector3d interactionPoint, final Direction targetSide
        return null;
    }

    private static net.minecraftforge.eventbus.api.Event.Result tristateToResult(Tristate tristate) {
        if (tristate == Tristate.TRUE) {
            return net.minecraftforge.eventbus.api.Event.Result.ALLOW;
        } else if (tristate == Tristate.UNDEFINED) {
            return net.minecraftforge.eventbus.api.Event.Result.DEFAULT;
        } else {
            return net.minecraftforge.eventbus.api.Event.Result.DENY;
        }
    }

    private static Tristate resultToTristate(net.minecraftforge.eventbus.api.Event.Result result) {
        if (result == net.minecraftforge.eventbus.api.Event.Result.ALLOW) {
            return Tristate.TRUE;
        } else if (result == net.minecraftforge.eventbus.api.Event.Result.DEFAULT) {
            return Tristate.UNDEFINED;
        } else {
            return Tristate.FALSE;
        }
    }
}
