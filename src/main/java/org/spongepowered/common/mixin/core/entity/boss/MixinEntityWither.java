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
package org.spongepowered.common.mixin.core.entity.boss;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import org.spongepowered.api.entity.living.monster.Wither;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.mixin.core.entity.monster.MixinEntityMob;

import java.util.Optional;

@Mixin(value = EntityWither.class)
public abstract class MixinEntityWither extends MixinEntityMob implements FusedExplosiveBridge {

    @Shadow public abstract void setInvulTime(int ticks);
    @Shadow public abstract int getInvulTime();

    @Shadow private int blockBreakCounter;
    private int explosionRadius = 7;
    private int fuseDuration = 220;

    /**
     * @author gabizou - April 11th, 2018
     * @reason Due to changes in forge, the gamerule retrieval is now an event
     * method, which prevents this redirect from working in forge, but will work
     * in vanilla. As such, redirecting the 3rd field getter for this method allows
     * us to still perform the boolean comparison. for the sake of comparison...
     *
     * @param thisEntity
     * @return
     */
    @Redirect(
        method = "updateAITasks",
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lnet/minecraft/entity/boss/EntityWither;blockBreakCounter:I",
                opcode = Opcodes.PUTFIELD
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/math/MathHelper;floor(D)I"
            )
        ),
        at =
            @At(
                value = "FIELD",
                target = "Lnet/minecraft/entity/boss/EntityWither;blockBreakCounter:I",
                opcode = Opcodes.GETFIELD
            )
    )
    private int spongeImpl$onCanGrief(EntityWither thisEntity) {
        return this.blockBreakCounter == 0 ? ((GrieferBridge) this).bridge$CanGrief() ? 0 : -1 : -1;
    }

    @ModifyArg(method = "launchWitherSkullToCoords", at = @At(value = "INVOKE",
               target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private Entity onSpawnWitherSkull(Entity entity) {
        ((GrieferBridge) entity).bridge$SetCanGrief(((GrieferBridge) this).bridge$CanGrief());
        return entity;
    }

    // FusedExplosive Impl

    @Override
    public Optional<Integer> getExplosionRadius() {
        return Optional.of(this.explosionRadius);
    }

    @Override
    public void setExplosionRadius(Optional<Integer> radius) {
        this.explosionRadius = radius.orElse(7);
    }

    @Override
    public int bridge$getFuseDuration() {
        return this.fuseDuration;
    }

    @Override
    public void bridge$setFuseDuration(int fuseTicks) {
        this.fuseDuration = fuseTicks;
    }

    @Override
    public int bridge$getFuseTicksRemaining() {
        return getInvulTime();
    }

    @Override
    public void bridge$setFuseTicksRemaining(int fuseTicks) {
        setInvulTime(fuseTicks);
    }

    /**
     * Called when a Wither is "primed" for the first time on spawn.
     *
     * @param self This entity
     * @param fuseTicks Ticks until detonation
     */
    @Redirect(method = "ignite", at = @At(value = "INVOKE",
              target = "Lnet/minecraft/entity/boss/EntityWither;setInvulTime(I)V"))
    private void onSpawnPrime(EntityWither self, int fuseTicks) {
        ((Wither) this).prime();
    }

    @Redirect(method = "updateAITasks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;newExplosion"
                                                                            + "(Lnet/minecraft/entity/Entity;DDDFZZ)Lnet/minecraft/world/Explosion;"))
    private net.minecraft.world.Explosion spongeImpl$UseSpongeExplosion(net.minecraft.world.World worldObj, Entity self, double x,
                                                      double y, double z, float strength, boolean flaming,
                                                      boolean smoking) {
        return detonate(Explosion.builder()
                .sourceExplosive((Wither) this)
                .location(new Location<>((World) worldObj, new Vector3d(x, y, z)))
                .radius(this.explosionRadius)
                .canCauseFire(flaming)
                .shouldPlaySmoke(smoking)
                .shouldBreakBlocks(smoking && ((GrieferBridge) this).bridge$CanGrief()))
                .orElse(null);
    }

}
