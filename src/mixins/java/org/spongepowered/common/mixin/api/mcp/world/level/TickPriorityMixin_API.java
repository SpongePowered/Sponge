package org.spongepowered.common.mixin.api.mcp.world.level;

import net.minecraft.world.level.TickPriority;
import org.spongepowered.api.scheduler.TaskPriority;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TickPriority.class)
public class TickPriorityMixin_API implements TaskPriority {
}
