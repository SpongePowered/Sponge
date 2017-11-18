package org.spongepowered.test;

import static org.spongepowered.api.data.DataTransactionResult.DataCategory.REPLACED;
import static org.spongepowered.api.data.DataTransactionResult.DataCategory.SUCCESSFUL;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.data.GetKey;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

@Plugin(id = "food-change-test", authors = "Aaron1011")
public class FoodChangeTest {

    @Listener
    public void onChange(ChangeDataHolderEvent.ValueChange event) {
        int oldFood = 0;
        int newFood = 0;
        for (ImmutableValue<?> val: event.getChanges().getReplacedData()) {
            if (val.getKey().equals(Keys.FOOD_LEVEL)) {
                oldFood = (Integer) val.get();
            }
        }

        for (ImmutableValue<?> val: event.getChanges().getSuccessfulData()) {
            if (val.getKey().equals(Keys.FOOD_LEVEL)) {
                newFood = (Integer) val.get();
            }
        }

        MessageChannel.TO_ALL.send(Text.of(String.format("Simple listener: %s %s", oldFood, newFood)));
    }

    @Listener
    public void onChange(ChangeDataHolderEvent.ValueChange event,
            @GetKey(value = "FOOD_LEVEL", from = REPLACED) int oldFood,
            @GetKey(value = "FOOD_LEVEL", from = SUCCESSFUL) MutableBoundedValue<Integer> newFood,
            @First(tag = "a") Player player,
            @GetKey(value = "DISPLAY_NAME", tag = "a") Text name) {

        MessageChannel.TO_ALL.send(Text.of(String.format("GetKey listener: %s %s from player ", oldFood, newFood.get())).concat(name));

        event.setChanges(DataTransactionResult.builder().from(event.getChanges()).absorbResult(DataTransactionResult.successResult(newFood.asImmutable().with(newFood.getMaxValue()))).build());
    }

}
