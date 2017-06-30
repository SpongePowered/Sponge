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
package org.spongepowered.common.mixin.core.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.util.VecHelper;

@Mixin({EntityPlayerMP.class, TileEntityCommandBlock.class, EntityMinecartCommandBlock.class})
public abstract class MixinLocatedSource implements Locatable, IMixinCommandSource {

    @Override
    public Location<World> getLocation() {
        return new Location<>(getWorld(), VecHelper.toVector3d(asICommandSender().getPositionVector()));
    }

    @Override
    public World getWorld() {
        ICommandSender commandSender = asICommandSender();
        // May be null in some cases like when constructing
        if (commandSender == null) {
            if ((Object) this instanceof Entity) {
                return (World) ((Entity) (Object) this).world;
            } else if ((Object) this instanceof TileEntity) {
                return (World) ((TileEntity) (Object) this).getWorld();
            }
            commandSender = SpongeImpl.getServer();
        }
        return (World) commandSender.getEntityWorld();
    }

}
