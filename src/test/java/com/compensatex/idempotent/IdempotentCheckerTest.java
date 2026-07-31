package com.compensatex.idempotent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IdempotentCheckerTest {

    @Test
    void fallsBackToMemoryAndRejectsDuplicateKey() {
        IdempotentChecker checker = new IdempotentChecker(null);

        assertThat(checker.checkAndMark("order-1001")).isTrue();
        assertThat(checker.checkAndMark("order-1001")).isFalse();
        assertThat(checker.checkAndMark("order-1002")).isTrue();
    }

    @Test
    void treatsBlankKeyAsNonIdempotent() {
        IdempotentChecker checker = new IdempotentChecker(null);

        assertThat(checker.checkAndMark("")).isTrue();
        assertThat(checker.checkAndMark("  ")).isTrue();
        assertThat(checker.checkAndMark(null)).isTrue();
    }
}
