package org.spongepowered.common.entity.ai.target;

import org.spongepowered.api.entity.ai.task.builtin.creature.target.TargetAITask;

public abstract class SpongeTargetAIBuilder<A extends TargetAITask<A>, B extends TargetAITask.Builder<A, B>> implements TargetAITask.Builder<A, B> {

    protected boolean checkSight, onlyNearby;
    protected int searchDelay, interruptTargetUnseenTicks;

    @Override
    public B checkSight() {
        this.checkSight = true;
        return (B) this;
    }

    @Override
    public B onlyNearby() {
        this.onlyNearby = true;
        return (B) this;
    }

    @Override
    public B searchDelay(int delayTicks) {
        this.searchDelay = delayTicks;
        return (B) this;
    }

    @Override
    public B interruptTargetUnseenTicks(int unseenTicks) {
        this.interruptTargetUnseenTicks = unseenTicks;
        return (B) this;
    }
}
