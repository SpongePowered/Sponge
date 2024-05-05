/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event.tracking.context.transaction;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.StubPhaseState;
import org.spongepowered.common.event.tracking.context.transaction.effect.InventoryEffect;
import org.spongepowered.common.test.UnitTestExtension;

import java.util.Iterator;
import java.util.stream.Stream;

@Disabled
@SuppressWarnings("deprecation")
@ExtendWith(UnitTestExtension.class)
public class GameTransactionIteratorTest {

    @Test
    public void verifyIterator() {
        final StubTransaction stubTransaction = new StubTransaction();
        final Iterator<GameTransaction<@NonNull ?>> iterator = stubTransaction.childIterator();
        Assertions.assertFalse(iterator.hasNext());
    }

    @Test
    public void verifyNestedIterator() {
        final StubTransaction stubTransaction = new StubTransaction();
        final ResultingTransactionBySideEffect effect = new ResultingTransactionBySideEffect(
            InventoryEffect.getInstance());
        stubTransaction.addLast(effect);
        final StubTransaction child1 = new StubTransaction();
        effect.addChild(PhaseContext.empty(), child1);
        final Iterator<GameTransaction<@NonNull ?>> iterator = stubTransaction.childIterator();
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(child1, iterator.next());
    }

    public static Stream<Arguments> verifyDeeplyNestedIterator() {
        return Stream.of(
            Arguments.of(1, 1),
            Arguments.of(2, 4),
            Arguments.of(30, 45),
            Arguments.of(1340, 843)
        );
    }

    @ParameterizedTest
    @MethodSource(value = "verifyDeeplyNestedIterator")
    public void verifyDeeplyNestedIterator(final int effectCount, final int childCount) {
        final StubTransaction parent = new StubTransaction();
        final StubTransaction[][] children = new StubTransaction[effectCount][childCount];
        final PhaseContext<@NonNull ?> phaseContext = StubPhaseState.getInstance().createPhaseContext(PhaseTracker.getInstance());
        phaseContext.buildAndSwitch();
        for (int i = 0; i < effectCount; i++) {
            final ResultingTransactionBySideEffect effect = new ResultingTransactionBySideEffect(
                InventoryEffect.getInstance());
            parent.addLast(effect);
            for (int j = 0; j < childCount; j++) {
                final StubTransaction child = new StubTransaction(String.format("effect[%d]child[%d]", i, j));
                children[i][j] = child;
                effect.addChild(phaseContext, child);
            }
        }
        final Iterator<GameTransaction<@NonNull ?>> iterator = parent.childIterator();
        for (int i = 0; i < effectCount; i++) {
            Assertions.assertTrue(iterator.hasNext());
            for (int j = 0; j < childCount; j++) {
                Assertions.assertTrue(iterator.hasNext());
                final GameTransaction<@NonNull ?> next = iterator.next();
                Assertions.assertEquals(children[i][j], next);
            }
        }
        final Iterator<GameTransaction<@NonNull ?>> reverseIterator = parent.reverseChildIterator();
        for (int i = effectCount - 1; i >= 0; i--) {
            for (int j = childCount - 1; j >= 0; j--) {
                Assertions.assertTrue(reverseIterator.hasNext());
                final GameTransaction<@NonNull ?> previous = reverseIterator.next();
                Assertions.assertEquals(children[i][j], previous);
            }

        }
    }

    private static Stream<Arguments> validateTransactionalIterator() {
        return Stream.of(
            Arguments.of(1, 1, 1),
            Arguments.of(4, 3, 5),
            Arguments.of(8, 39, 29),
            Arguments.of(293, 87, 112)
        );
    }
    @ParameterizedTest
    @MethodSource(value = "validateTransactionalIterator")
    public void validateTransactionalIterator(final int transactionCount, final int effectCount, final int childCount) {
        final StubTransaction[][][] children = new StubTransaction[transactionCount][effectCount][childCount];
        final StubTransaction[] transactions = new StubTransaction[transactionCount];
        final PhaseContext<@NonNull ?> phaseContext = StubPhaseState.getInstance().createPhaseContext(PhaseTracker.getInstance());
        phaseContext.buildAndSwitch();
        final TransactionalCaptureSupplier transactor = phaseContext.getTransactor();

        for (int t = 0; t < transactionCount; t++) {
            final StubTransaction transaction = new StubTransaction("transaction[" + t + "]");
            transactions[t] = transaction;
            transactor.logTransaction(transaction);
            for (int i = 0; i < effectCount; i++) {
                final ResultingTransactionBySideEffect effect = new ResultingTransactionBySideEffect(
                    InventoryEffect.getInstance());
                try (final EffectTransactor ignored = transactor.pushEffect(effect)) {
                    for (int j = 0; j < childCount; j++) {
                        final StubTransaction child = new StubTransaction(String.format("effect[%d]child[%d]", i, j));
                        children[t][i][j] = child;
                        transactor.logTransaction(child);
                    }
                }
            }
        }
        final Iterator<GameTransaction<@NonNull ?>> iterator = transactor.iterator();

        for (int t = 0; t < transactionCount; t++) {
            Assertions.assertTrue(iterator.hasNext());
            final StubTransaction parent = transactions[t];
            Assertions.assertEquals(parent, iterator.next());
            for (int i = 0; i < effectCount; i++) {
                Assertions.assertTrue(iterator.hasNext());
                for (int j = 0; j < childCount; j++) {
                    Assertions.assertTrue(iterator.hasNext());
                    final GameTransaction<@NonNull ?> next = iterator.next();
                    Assertions.assertEquals(children[t][i][j], next);
                }
            }
        }
        Assertions.assertFalse(iterator.hasNext());

        final Iterator<GameTransaction<@NonNull ?>> reverseIterator = transactor.descendingIterator();

        for (int t = transactionCount - 1; t >= 0; t--) {
            for (int i = effectCount - 1; i >= 0; i--) {
                for (int j = childCount - 1; j >= 0; j--) {
                    final GameTransaction<@NonNull ?> next = reverseIterator.next();
                    Assertions.assertEquals(children[t][i][j], next);
                }
            }
            Assertions.assertTrue(reverseIterator.hasNext());
            final StubTransaction parent = transactions[t];
            Assertions.assertEquals(parent, reverseIterator.next());
        }
        Assertions.assertFalse(reverseIterator.hasNext());
    }

}
