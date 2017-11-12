package org.spongepowered.test;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

@Plugin(id = "food-change-test", authors = "Aaron1011")
public class FoodChangeTest {

    @Listener
    public void onChange(ChangeDataHolderEvent.ValueChange event) {
        ImmutableValue<Integer> food = null;
        for (ImmutableValue<?> val: event.getEndResult().getReplacedData()) {
            if (val.getKey().equals(Keys.FOOD_LEVEL)) {
                MessageChannel.TO_ALL.send(Text.of("Old food " + val.getDirect().get()));
            }
        }

        for (ImmutableValue<?> val: event.getEndResult().getSuccessfulData()) {
            if (val.getKey().equals(Keys.FOOD_LEVEL)) {
                MessageChannel.TO_ALL.send(Text.of("New food: " + val.getDirect().get()));
                food = (ImmutableValue) val;
            }
        }

        event.proposeChanges(DataTransactionResult.successResult(food.with(20)));

    }

}
