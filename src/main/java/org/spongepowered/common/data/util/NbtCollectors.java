package org.spongepowered.common.data.util;

import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;

import java.util.function.Function;
import java.util.stream.Collector;

public class NbtCollectors {

    private static final Collector<INBT, ?, ListNBT> TO_TAG_LIST = toList(value -> value);
    private static final Collector<Long, ?, ListNBT> TO_LONG_TAG_LIST = toList(LongNBT::new);
    private static final Collector<Integer, ?, ListNBT> TO_INT_TAG_LIST = toList(IntNBT::new);
    private static final Collector<Byte, ?, ListNBT> TO_BYTE_TAG_LIST = toList(ByteNBT::new);
    private static final Collector<Short, ?, ListNBT> TO_SHORT_TAG_LIST = toList(ShortNBT::new);
    private static final Collector<Boolean, ?, ListNBT> TO_BOOLEAN_TAG_LIST = toList(value -> new ByteNBT((byte) (value ? 1 : 0)));
    private static final Collector<Double, ?, ListNBT> TO_DOUBLE_TAG_LIST = toList(DoubleNBT::new);
    private static final Collector<Float, ?, ListNBT> TO_FLOAT_TAG_LIST = toList(FloatNBT::new);

    public static <E> Collector<E, ?, ListNBT> toList(Function<E, INBT> toTagFunction) {
        return Collector.of(ListNBT::new,
                (list, value) -> list.add(toTagFunction.apply(value)),
                (first, second) -> {
                    first.addAll(second);
                    return first;
                },
                list -> list);
    }

    public static Collector<INBT, ?, ListNBT> toList() {
        return TO_TAG_LIST;
    }

    public static Collector<Boolean, ?, ListNBT> toBooleanList() {
        return TO_BOOLEAN_TAG_LIST;
    }

    public static Collector<Byte, ?, ListNBT> toByteList() {
        return TO_BYTE_TAG_LIST;
    }

    public static Collector<Short, ?, ListNBT> toShortList() {
        return TO_SHORT_TAG_LIST;
    }

    public static Collector<Integer, ?, ListNBT> toIntList() {
        return TO_INT_TAG_LIST;
    }

    public static Collector<Long, ?, ListNBT> toLongList() {
        return TO_LONG_TAG_LIST;
    }

    public static Collector<Float, ?, ListNBT> toFloatList() {
        return TO_FLOAT_TAG_LIST;
    }

    public static Collector<Double, ?, ListNBT> toDoubleList() {
        return TO_DOUBLE_TAG_LIST;
    }
}
