package org.spongepowered.common.data.manipulator.mutable.tileentity;

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
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBannerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongePatternListValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;
import com.google.common.collect.Lists;

public class SpongeBannerData extends AbstractData<BannerData, ImmutableBannerData> implements BannerData {
	
	private DyeColor baseColor;
    private List<PatternLayer> patternsList;

    public SpongeBannerData() {
    	this(DyeColors.WHITE, new ArrayList<PatternLayer>());
    }

	public SpongeBannerData(DyeColor baseColor, List<PatternLayer> patternsList) {
		super(BannerData.class);
		this.baseColor = baseColor;
		this.patternsList = patternsList;
	}

	@Override
	public BannerData copy() {
		return new SpongeBannerData(baseColor, patternsList);
	}

	@Override
	public ImmutableBannerData asImmutable() {
		return new ImmutableSpongeBannerData(baseColor, patternsList);
	}

	@Override
	public int compareTo(BannerData o) {
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
	public Value<DyeColor> baseColor() {
		return new SpongeValue<DyeColor>(Keys.BANNER_BASE_COLOR, DyeColors.WHITE, this.baseColor);
	}

	@Override
	public PatternListValue patternsList() {
	    return new SpongePatternListValue(Keys.BANNER_PATTERNS, Lists.newArrayList(this.patternsList));
	}

	public DyeColor getBaseColor() {
	    return this.baseColor;
	}

	public List<PatternLayer> getPatternsList() {
	    return this.patternsList;
	}
	
	public void setBaseColor(DyeColor baseColor) {
	    this.baseColor = baseColor;
	}
	
	public void setPatternsList(List<PatternLayer> patternsList) {
        this.patternsList = patternsList;
    }

    @Override
    protected void registerGettersAndSetters() {
        //TODO
        registerFieldGetter(Keys.BANNER_BASE_COLOR, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getBaseColor();
            }
        });
        registerFieldSetter(Keys.BANNER_BASE_COLOR, new SetterFunction<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public void set(Object value) {
                setBaseColor((DyeColor) value);
            }
        });
        registerKeyValue(Keys.BANNER_BASE_COLOR, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return baseColor();
            }
        });
        registerFieldGetter(Keys.BANNER_PATTERNS, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getPatternsList();
            }
        });
        registerFieldSetter(Keys.BANNER_PATTERNS, new SetterFunction<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public void set(Object value) {
                setPatternsList((List<PatternLayer>) value);
            }
        });
        registerKeyValue(Keys.BANNER_PATTERNS, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return patternsList();
            }
        });
    }

}
