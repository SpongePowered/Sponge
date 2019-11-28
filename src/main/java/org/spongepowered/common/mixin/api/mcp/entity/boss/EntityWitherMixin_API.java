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
package org.spongepowered.common.mixin.api.mcp.entity.boss;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.monster.Wither;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.mixin.api.mcp.entity.monster.EntityMobMixin_API;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.world.ServerBossInfo;

@Mixin(value = WitherEntity.class)
public abstract class EntityWitherMixin_API extends EntityMobMixin_API implements Wither {

    @Shadow @Final private ServerBossInfo bossInfo;
    @Shadow public abstract int getWatchedTargetId(int p_82203_1_);
    @Shadow public abstract void updateWatchedTargetId(int targetOffset, int newId);
    @Shadow public abstract void setInvulTime(int ticks);

    private int fuseDuration = 220;

    @Override
    public List<Living> getTargets() {
        List<Living> values = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int id = getWatchedTargetId(i);
            if (id > 0) {
                values.add((Living) this.world.func_73045_a(id));
            }
        }
        return values;
    }

    @Override
    public void setTargets(List<Living> targets) {
        checkNotNull(targets, "Targets are null!");
        for (int i = 0; i < 2; i++) {
            updateWatchedTargetId(i, targets.size() > i ? ((LivingEntity) targets.get(i)).func_145782_y() : 0);
        }
    }

    @Override
    public ServerBossBar getBossBar() {
        return (ServerBossBar) this.bossInfo;
    }


    @Override
    public void detonate() {
        ((FusedExplosiveBridge) this).bridge$setFuseTicksRemaining(1);
    }

    @Override
    public void prime() {
        checkState(!isPrimed(), "already primed");
        if (((FusedExplosiveBridge) this).bridge$shouldPrime()) {
            ((FusedExplosiveBridge) this).bridge$setFuseTicksRemaining(this.fuseDuration);
            ((FusedExplosiveBridge) this).bridge$postPrime();
        }
    }

    @Override
    public void defuse() {
        checkState(isPrimed(), "not primed");
        if (((FusedExplosiveBridge) this).bridge$shouldDefuse()) {
            setInvulTime(0);
            ((FusedExplosiveBridge) this).bridge$postDefuse();
        }
    }

    @Override
    public boolean isPrimed() {
        return ((FusedExplosiveBridge) this).bridge$getFuseTicksRemaining() > 0;
    }

}
