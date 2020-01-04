package org.spongepowered.common.mixin.api.mcp.stats;

import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.stats.StatTypeBridge;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Mixin(StatType.class)
public abstract class StatTypeMixin_API implements StatisticCategory {

    @Shadow @Final private Map<Object, Stat<Object>> map;

    @Override
    public CatalogKey getKey() {
        return ((StatTypeBridge) this).bridge$getKey();
    }

    @Override
    public Collection<? extends Statistic> getStatistics() {
        return Collections.unmodifiableCollection((Collection<? extends Statistic>) this.map.values());
    }
}
