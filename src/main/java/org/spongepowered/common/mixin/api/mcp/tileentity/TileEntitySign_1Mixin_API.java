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
package org.spongepowered.common.mixin.api.mcp.tileentity;

import net.minecraft.command.ICommandSender;
import net.minecraft.tileentity.SignTileEntity;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.SignSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.command.WrapperCommandSource;

@Mixin(targets = "net/minecraft/tileentity/TileEntitySign$1")
public abstract class TileEntitySign_1Mixin_API implements SignSource {

    @Shadow(aliases = {"this$0", "field_174795_a"})
    @Final
    private SignTileEntity field_174795_a;

    @SuppressWarnings({"ConstantConditions", "RedundantCast"}) // This is an ICommandSender anonymous class
    @Override
    public CommandSource getOriginalSource() {
        return WrapperCommandSource.of(((ICommandSender) (Object) this).getCommandSenderEntity());
    }

    @Override
    public Sign getSign() {
        return (Sign) this.field_174795_a;
    }

    @Override
    public World getWorld() {
        return (World) this.field_174795_a.getWorld();
    }

    @Override
    public Location<World> getLocation() {
        return ((Sign) this.field_174795_a).getLocation();
    }
}
