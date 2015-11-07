package org.spongepowered.common.mixin.core.entity.ai;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.FindNearestAttackableTargetAITask;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.util.GuavaJavaUtils;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityAINearestAttackableTarget.class)
public abstract class MixinEntityAINearestAttackableTarget extends MixinEntityAITarget<FindNearestAttackableTargetAITask>
        implements FindNearestAttackableTargetAITask {

    @Shadow protected Class targetClass;
    @Shadow private int targetChance;
    @Shadow protected Predicate targetEntitySelector;

    @Override
    public Class<? extends Living> getTargetClass() {
        return targetClass;
    }

    @Override
    public FindNearestAttackableTargetAITask setTargetClass(Class<? extends Living> targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    @Override
    public int getChance() {
        return targetChance;
    }

    @Override
    public FindNearestAttackableTargetAITask setChance(int chance) {
        this.targetChance = chance;
        return this;
    }

    @Override
    public FindNearestAttackableTargetAITask filter(java.util.function.Predicate<Living> predicate) {
        this.targetEntitySelector = predicate == null ? null : GuavaJavaUtils.asGuavaPredicate(predicate);
        return this;
    }

    @Redirect(method = "shouldExecute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesWithinAABB(Ljava/lang/Class;"
            + "Lnet/minecraft/util/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> onGetEntitiesWithinAABB(World world, Class clazz, AxisAlignedBB aabb, Predicate predicate) {
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : (List<Entity>) world.getEntities(this.targetClass, predicate)) {
            if (entity.getEntityBoundingBox().intersectsWith(aabb)) {
                entities.add(entity);
            }
        }
        return entities;
    }

}
