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
package org.spongepowered.common.mixin.api.mcp.entity.monster;

import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.DyeableData;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.golem.Shulker;
import org.spongepowered.api.entity.projectile.EntityTargetingProjectile;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.SpongeDyeableData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirectionalData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.util.DirectionResolver;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.projectile.ProjectileLauncher;
import org.spongepowered.common.interfaces.entity.monster.IMixinShulker;

import java.util.List;
import java.util.Optional;

@Mixin(EntityShulker.class)
public abstract class EntityShulkerMixin_API extends EntityGolemMixin_API implements Shulker, IMixinShulker {

    @Shadow @Final protected static DataParameter<Byte> COLOR;

    @Shadow @Final protected static DataParameter<EnumFacing> ATTACHED_FACE;

    @Override
    public DyeColor getColor() {
        return (DyeColor) (Object) EnumDyeColor.byMetadata(this.dataManager.get(COLOR) & 15);
    }

    @Override
    public void setColor(DyeColor color) {
        this.dataManager.set(COLOR, (byte) (this.dataManager.get(COLOR) & 240 | ((EnumDyeColor) (Object) color).getMetadata() & 15));
    }

    @Override
    public Direction getDirection() {
        return DirectionResolver.getFor(this.dataManager.get(ATTACHED_FACE));
    }

    @Override
    public void setDirection(Direction direction) {
        this.dataManager.set(ATTACHED_FACE, DirectionResolver.getFor(direction));
    }

    @Override
    public DyeableData getDyeData() {
        return new SpongeDyeableData(getColor());
    }

    @Override
    public Value<DyeColor> color() {
        return new SpongeValue<>(Keys.DYE_COLOR, Constants.Catalog.DEFAULT_SHULKER_COLOR, getColor());
    }

    @Override
    public DirectionalData getDirectionalData() {
        return new SpongeDirectionalData(DirectionResolver.getFor(this.dataManager.get(ATTACHED_FACE)));
    }

    @Override
    public Value<Direction> direction() {
        return new SpongeValue<>(Keys.DIRECTION, DirectionResolver.getFor(this.dataManager.get(ATTACHED_FACE)));
    }

    @Override
    public <P extends EntityTargetingProjectile> Optional<P> launchWithTarget(Class<P> projectileClass, org.spongepowered.api.entity.Entity target) {
        return ProjectileLauncher.launchWithArgs(projectileClass, Shulker.class, this, null, target);
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(List<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(getDyeData());
        manipulators.add(getDirectionalData());
    }
}
