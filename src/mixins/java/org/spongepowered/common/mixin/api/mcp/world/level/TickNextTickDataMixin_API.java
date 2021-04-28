package org.spongepowered.common.mixin.api.mcp.world.level;

import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.TickPriority;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.scheduler.TaskPriority;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.level.TickNextTickDataBridge;

import java.time.Duration;

@Mixin(TickNextTickData.class)
public class TickNextTickDataMixin_API<T> implements ScheduledUpdate<T> {
    @Shadow @Final private T type;
    @Shadow @Final public TickPriority priority;

    @Override
    public T target() {
        return this.type;
    }

    @Override
    public Duration delay() {
        return ((TickNextTickDataBridge) this).bridge$getScheduledDelayWhenCreated();
    }

    @Override
    public TaskPriority priority() {
        return (TaskPriority) (Object) this.priority;
    }

    @Override
    public State state() {
        return ((TickNextTickDataBridge) this).bridge$internalState();
    }

    @Override
    public boolean cancel() {
        return ((TickNextTickDataBridge) this).bridge$cancelForcibly();
    }

    @Override
    public World<?, ?> world() {
        return ((TickNextTickDataBridge) this).bridge$getLocation().world();
    }

    @Override
    public Location<?, ?> location() {
        return ((TickNextTickDataBridge) this).bridge$getLocation();
    }
}
