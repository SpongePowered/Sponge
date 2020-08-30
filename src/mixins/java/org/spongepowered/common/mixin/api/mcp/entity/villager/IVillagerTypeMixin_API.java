package org.spongepowered.common.mixin.api.mcp.entity.villager;

import net.minecraft.entity.villager.IVillagerType;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.type.VillagerType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IVillagerType.class)
public interface IVillagerTypeMixin_API extends VillagerType {

    @Override
    default ResourceKey getKey() {
        return (ResourceKey) (Object) Registry.VILLAGER_TYPE.getKey((IVillagerType) this);
    }
}
