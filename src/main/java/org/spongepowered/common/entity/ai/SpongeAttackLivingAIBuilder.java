package org.spongepowered.common.entity.ai;

import com.google.common.base.Preconditions;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import org.spongepowered.api.entity.ai.task.builtin.creature.AttackLivingAITask;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.entity.living.Living;

public final class SpongeAttackLivingAIBuilder implements AttackLivingAITask.Builder {
    private Class<? extends Living> targetClass;
    private double speed;
    private boolean longMemory;

    @Override
    public AttackLivingAITask.Builder target(Class<? extends Living> targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    @Override
    public AttackLivingAITask.Builder speed(double speed) {
        this.speed = speed;
        return this;
    }

    @Override
    public AttackLivingAITask.Builder longMemory() {
        this.longMemory = true;
        return this;
    }

    @Override
    public AttackLivingAITask.Builder reset() {
        this.targetClass = null;
        this.speed = 0;
        this.longMemory = false;
        return this;
    }

    @Override
    public AttackLivingAITask build(Creature owner) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(targetClass);
        return (AttackLivingAITask) new EntityAIAttackOnCollide((EntityCreature) owner, targetClass, speed, longMemory);
    }
}
