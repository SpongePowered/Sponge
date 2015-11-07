package org.spongepowered.common.mixin.core.entity.ai;

import net.minecraft.entity.ai.EntityAITarget;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.TargetAITask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityAITarget.class)
public abstract class MixinEntityAITarget<A extends TargetAITask<A>> extends MixinEntityAIBase implements TargetAITask<A> {

    @Shadow protected boolean shouldCheckSight;
    @Shadow private boolean nearbyOnly;
    @Shadow private int targetSearchStatus;
    @Shadow private int targetSearchDelay;
    @Shadow private int targetUnseenTicks;

    @Override
    public boolean shouldCheckSight() {
        return shouldCheckSight;
    }

    @Override
    public A setCheckSight(boolean checkSight) {
        this.shouldCheckSight = checkSight;
        return (A) this;
    }

    @Override
    public boolean onlyNearby() {
        return nearbyOnly;
    }

    @Override
    public A setOnlyNearby(boolean nearby) {
        this.nearbyOnly = nearby;
        return (A) this;
    }

    @Override
    public int getSearchStatus() {
        return targetSearchStatus;
    }

    @Override
    public A setSearchStatus(int status) {
        this.targetSearchStatus = status;
        return (A) this;
    }

    @Override
    public int getSearchDelay() {
        return targetSearchDelay;
    }

    @Override
    public A setSearchDelay(int delay) {
        this.targetSearchDelay = delay;
        return (A) this;
    }

    @Override
    public int getInterruptIfTargetUnseenTicks() {
        return targetUnseenTicks;
    }

    @Override
    public A setInterruptIfTargetUnseenTicks(int ticks) {
        this.targetUnseenTicks = ticks;
        return (A) this;
    }
}
