package org.spongepowered.common.entity.ai.target;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.FindNearestAttackableTargetAITask;
import org.spongepowered.api.entity.living.Creature;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.util.GuavaJavaUtils;

import java.util.function.Predicate;

public final class SpongeFindNearestAttackableTargetAIBuilder extends SpongeTargetAIBuilder<FindNearestAttackableTargetAITask, FindNearestAttackableTargetAITask.Builder>
        implements FindNearestAttackableTargetAITask.Builder {

    private Class<? extends Living> targetClass;
    private int chance;
    private Predicate<? extends Living> predicate;

    @Override
    public FindNearestAttackableTargetAITask.Builder target(Class<? extends Living> targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    @Override
    public FindNearestAttackableTargetAITask.Builder chance(int chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public FindNearestAttackableTargetAITask.Builder filter(Predicate<? extends Living> predicate) {
        this.predicate = predicate;
        return null;
    }

    @Override
    public FindNearestAttackableTargetAITask.Builder reset() {
        checkSight = false;
        onlyNearby = false;
        searchDelay = 0;
        interruptTargetUnseenTicks = 0;
        targetClass = null;
        predicate = null;
        return this;
    }

    @Override
    public FindNearestAttackableTargetAITask build(Creature owner) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(targetClass);
        return (FindNearestAttackableTargetAITask) new EntityAINearestAttackableTarget((EntityCreature) owner, targetClass, chance, checkSight,
                onlyNearby, predicate == null ? Predicates.alwaysTrue() : GuavaJavaUtils.asGuavaPredicate(predicate));
    }
}
