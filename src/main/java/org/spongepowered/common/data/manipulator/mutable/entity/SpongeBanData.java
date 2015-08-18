package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBanData;
import org.spongepowered.api.data.manipulator.mutable.entity.BanData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeBanData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;

import java.util.Set;

public class SpongeBanData extends AbstractSingleData<Set, BanData, ImmutableBanData> implements BanData {

    private final Set<Ban.User> bans;

    public SpongeBanData(Set<Ban.User> bans) {
        super(BanData.class, bans, Keys.USER_BANS);
        this.bans = bans;
    }

    @Override
    protected Value<?> getValueGetter() {
        return bans();
    }

    @Override
    public BanData copy() {
        return new SpongeBanData(this.bans);
    }

    @Override
    public ImmutableBanData asImmutable() {
        return new ImmutableSpongeBanData(this.bans);
    }

    @Override
    public int compareTo(BanData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.USER_BANS, this.bans);
    }

    @Override
    public SetValue<Ban.User> bans() {
        return new SpongeSetValue<Ban.User>(Keys.USER_BANS, this.bans);
    }
}
