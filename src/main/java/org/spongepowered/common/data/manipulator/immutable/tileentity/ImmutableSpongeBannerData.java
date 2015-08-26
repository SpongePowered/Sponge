package org.spongepowered.common.data.manipulator.immutable.tileentity;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.value.immutable.ImmutablePatternListValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBannerData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongePatternListValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.GetterFunction;
import com.google.common.collect.Lists;

public class ImmutableSpongeBannerData extends AbstractImmutableData<ImmutableBannerData, BannerData> implements ImmutableBannerData {

    private final DyeColor baseColor;
    private final List<PatternLayer> patternsList;

    public ImmutableSpongeBannerData() {
        this(DyeColors.WHITE, new ArrayList<PatternLayer>());
    }

    public ImmutableSpongeBannerData(DyeColor baseColor, List<PatternLayer> patternsList) {
        super(ImmutableBannerData.class);
        this.baseColor = baseColor;
        this.patternsList = patternsList;
    }

    @Override
    public ImmutableBannerData copy() {
        return new ImmutableSpongeBannerData(baseColor, patternsList);
    }

    @Override
    public BannerData asMutable() {
        return new SpongeBannerData(baseColor, patternsList);
    }

    @Override
    public int compareTo(ImmutableBannerData o) {
        // TODO
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.BANNER_BASE_COLOR.getQuery(), baseColor)
            .set(Keys.BANNER_PATTERNS.getQuery(), patternsList);    
    }

    @Override
    public ImmutableValue<DyeColor> baseColor() {
        return new ImmutableSpongeValue<DyeColor>(Keys.BANNER_BASE_COLOR, DyeColors.WHITE, this.baseColor);
    }

    @Override
    public ImmutablePatternListValue patterns() {
        return new ImmutableSpongePatternListValue(Keys.BANNER_PATTERNS, Lists.newArrayList(this.patternsList));
    }

    public DyeColor getBaseColor() {
        return this.baseColor;
    }

    public List<PatternLayer> getPatternsList() {
        return this.patternsList;
    }
    

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.BANNER_BASE_COLOR, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getBaseColor();
            }
        });
        registerKeyValue(Keys.BANNER_BASE_COLOR, new GetterFunction<ImmutableValue<?>>() {
            @Override
            public ImmutableValue<?> get() {
                return baseColor();
            }
        });
        registerFieldGetter(Keys.BANNER_PATTERNS, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getPatternsList();
            }
        });
        registerKeyValue(Keys.BANNER_PATTERNS, new GetterFunction<ImmutableValue<?>>() {
            @Override
            public ImmutableValue<?> get() {
                return patterns();
            }
        });
    }

}
