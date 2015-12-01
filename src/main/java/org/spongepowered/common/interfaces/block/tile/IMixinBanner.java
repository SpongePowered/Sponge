package org.spongepowered.common.interfaces.block.tile;

import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.DyeColor;

import java.util.List;

public interface IMixinBanner {

    List<PatternLayer> getLayers();

    void setLayers(List<PatternLayer> layers);

    DyeColor getBaseColor();

    void setBaseColor(DyeColor baseColor);

}
