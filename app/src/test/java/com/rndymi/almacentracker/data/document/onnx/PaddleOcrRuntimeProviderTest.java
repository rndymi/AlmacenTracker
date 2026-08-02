package com.rndymi.almacentracker.data.document.onnx;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class PaddleOcrRuntimeProviderTest {

    private ExecutorService executorService;

    @After
    public void tearDown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Test
    public void initialize_executesFactoryOnlyOnceForConcurrentRequests()
            throws InterruptedException {
        executorService =
                Executors.newSingleThreadExecutor();

        AtomicInteger initializationCount =
                new AtomicInteger();

        CountDownLatch releaseInitialization =
                new CountDownLatch(1);

        PaddleOcrRuntimeFactory factory = () -> {
            initializationCount.incrementAndGet();

            try {
                releaseInitialization.await(
                        2,
                        TimeUnit.SECONDS
                );
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }

            return PaddleOcrInitializationResult.error(
                    PaddleOcrInitializationError
                            .DETECTOR_SESSION_ERROR,
                    null
            );
        };

        PaddleOcrRuntimeProvider provider =
                new PaddleOcrRuntimeProvider(
                        executorService,
                        factory
                );

        CountDownLatch callbackLatch =
                new CountDownLatch(2);

        provider.initialize(
                result -> callbackLatch.countDown()
        );
        provider.initialize(
                result -> callbackLatch.countDown()
        );

        releaseInitialization.countDown();

        assertTrue(
                callbackLatch.await(
                        2,
                        TimeUnit.SECONDS
                )
        );
        assertEquals(
                1,
                initializationCount.get()
        );
        assertEquals(
                PaddleOcrRuntimeProvider.State.ERROR,
                provider.getState()
        );
    }

    @Test
    public void initialize_allowsRetryAfterError()
            throws InterruptedException {
        executorService =
                Executors.newSingleThreadExecutor();

        AtomicInteger initializationCount =
                new AtomicInteger();

        PaddleOcrRuntimeFactory factory = () -> {
            initializationCount.incrementAndGet();

            return PaddleOcrInitializationResult.error(
                    PaddleOcrInitializationError.UNKNOWN,
                    null
            );
        };

        PaddleOcrRuntimeProvider provider =
                new PaddleOcrRuntimeProvider(
                        executorService,
                        factory
                );

        CountDownLatch first =
                new CountDownLatch(1);
        provider.initialize(
                result -> first.countDown()
        );
        assertTrue(
                first.await(
                        2,
                        TimeUnit.SECONDS
                )
        );

        CountDownLatch second =
                new CountDownLatch(1);
        provider.initialize(
                result -> second.countDown()
        );
        assertTrue(
                second.await(
                        2,
                        TimeUnit.SECONDS
                )
        );

        assertEquals(
                2,
                initializationCount.get()
        );
    }

    @Test
    public void close_isIdempotent() {
        executorService =
                Executors.newSingleThreadExecutor();

        PaddleOcrRuntimeProvider provider =
                new PaddleOcrRuntimeProvider(
                        executorService,
                        () -> PaddleOcrInitializationResult
                                .error(
                                        PaddleOcrInitializationError
                                                .UNKNOWN,
                                        null
                                )
                );

        provider.close();
        provider.close();

        assertEquals(
                PaddleOcrRuntimeProvider.State.CLOSED,
                provider.getState()
        );
    }

    @Test
    public void initialize_afterCloseReturnsControlledError() {
        executorService =
                Executors.newSingleThreadExecutor();

        PaddleOcrRuntimeProvider provider =
                new PaddleOcrRuntimeProvider(
                        executorService,
                        () -> PaddleOcrInitializationResult
                                .error(
                                        PaddleOcrInitializationError
                                                .UNKNOWN,
                                        null
                                )
                );

        provider.close();

        AtomicReference<
                PaddleOcrInitializationResult
                > received =
                new AtomicReference<>();

        provider.initialize(received::set);

        assertNotNull(received.get());
        assertFalse(received.get().isReady());
        assertEquals(
                PaddleOcrInitializationError
                        .PROVIDER_CLOSED,
                received.get().getError()
        );
    }
}
