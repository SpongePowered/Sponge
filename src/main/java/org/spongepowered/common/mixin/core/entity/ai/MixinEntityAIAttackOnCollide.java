package org.spongepowered.common.mixin.core.entity.ai;

import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import org.spongepowered.api.entity.ai.task.builtin.creature.AttackLivingAITask;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityAIAttackOnCollide.class)
public abstract class MixinEntityAIAttackOnCollide implements AttackLivingAITask {
    @Shadow Class classTarget;
    @Shadow double speedTowardsTarget;
    @Shadow boolean longMemory;

    @Override
    public Class<? extends Living> getTargetClass() {
        return (Class<? extends Living>) classTarget;
    }

    @Override
    public AttackLivingAITask setTargetClass(Class<? extends Living> targetClass) {
        this.classTarget = targetClass;
        return this;
    }

    @Override
    public double getSpeed() {
        return speedTowardsTarget;
    }

    @Override
    public AttackLivingAITask setSpeed(double speed) {
        this.speedTowardsTarget = speed;
        return this;
    }

    @Override
    public boolean hasLongMemory() {
        return longMemory;
    }

    @Override
    public AttackLivingAITask setLongMemory(boolean longMemory) {
        this.longMemory = longMemory;
        return this;
    }

}
