package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBanData;
import org.spongepowered.api.data.manipulator.mutable.entity.BanData;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBanData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;

import java.util.Set;

public class ImmutableSpongeBanData extends AbstractImmutableSingleData<Set, ImmutableBanData, BanData>
        implements ImmutableBanData {

    private final Set<Ban.User> bans;

    public ImmutableSpongeBanData(Set<Ban.User> bans) {
        super(ImmutableBanData.class, bans, null);
        this.bans = bans;
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return bans();
    }

    @Override
    public BanData asMutable() {
        return new SpongeBanData(this.bans);
    }

    @Override
    public ImmutableSetValue<Ban.User> bans() {
        return new ImmutableSpongeSetValue<Ban.User>(Keys.USER_BANS, this.bans);
    }

    @Override
    public int compareTo(ImmutableBanData o) {
        return 0;
    }
}
