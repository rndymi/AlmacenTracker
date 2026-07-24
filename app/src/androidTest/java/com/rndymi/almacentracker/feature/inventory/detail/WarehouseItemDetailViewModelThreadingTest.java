package com.rndymi.almacentracker.feature.inventory.detail;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.WarehouseItemDetailResult;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteResult;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteService;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@RunWith(AndroidJUnit4.class)
public final class WarehouseItemDetailViewModelThreadingTest {

    @Test
    public void deletionResultFromBackgroundThreadEmitsSuccess()
            throws InterruptedException {

        MutableLiveData<WarehouseItemDetailResult> detailResult =
                new MutableLiveData<>();

        AtomicReference<Throwable> backgroundFailure =
                new AtomicReference<>();

        CountDownLatch callbackFinished =
                new CountDownLatch(1);

        CountDownLatch successEmitted =
                new CountDownLatch(1);

        WarehouseItemRepository repository =
                repositoryObserving(detailResult);

        WarehouseItemDeleteService deleteService =
                new BackgroundDeleteService(
                        backgroundFailure,
                        callbackFinished
                );

        AtomicReference<WarehouseItemDetailViewModel> viewModel =
                new AtomicReference<>();

        Observer<WarehouseItemDetailUiState> stateObserver =
                state -> {
                };

        Observer<UiEvent<Boolean>> successObserver =
                event -> {
                    if (event != null
                            && Boolean.TRUE.equals(
                            event.peekContent()
                    )) {
                        successEmitted.countDown();
                    }
                };

        InstrumentationRegistry
                .getInstrumentation()
                .runOnMainSync(() -> {
                    WarehouseItemDetailViewModel createdViewModel =
                            new WarehouseItemDetailViewModel(
                                    repository,
                                    deleteService,
                                    7L
                            );

                    viewModel.set(createdViewModel);

                    createdViewModel.getUiState().observeForever(
                            stateObserver
                    );

                    createdViewModel.getDeletionSuccess()
                            .observeForever(successObserver);

                    detailResult.setValue(
                            WarehouseItemDetailResult.found(
                                    createWarehouseItem()
                            )
                    );

                    createdViewModel.deleteWarehouseItem();
                });

        assertTrue(
                "Deletion callback did not finish",
                callbackFinished.await(2, TimeUnit.SECONDS)
        );

        assertTrue(
                "Deletion success was not emitted",
                successEmitted.await(2, TimeUnit.SECONDS)
        );

        assertNull(backgroundFailure.get());

        InstrumentationRegistry
                .getInstrumentation()
                .runOnMainSync(() -> {
                    viewModel.get().getUiState().removeObserver(
                            stateObserver
                    );

                    viewModel.get().getDeletionSuccess()
                            .removeObserver(successObserver);
                });
    }

    private static WarehouseItem createWarehouseItem() {
        return new WarehouseItem(
                7L,
                "MR",
                "1050",
                "A1",
                null,
                null,
                1000L,
                1000L
        );
    }

    private static WarehouseItemRepository repositoryObserving(
            MutableLiveData<WarehouseItemDetailResult> detailResult
    ) {
        return (WarehouseItemRepository) Proxy.newProxyInstance(
                WarehouseItemRepository.class.getClassLoader(),
                new Class<?>[]{WarehouseItemRepository.class},
                (proxy, method, arguments) -> {
                    if ("observeById".equals(method.getName())) {
                        return detailResult;
                    }

                    throw new AssertionError(
                            "Unexpected repository operation: "
                                    + method.getName()
                    );
                }
        );
    }

    private static final class BackgroundDeleteService
            extends WarehouseItemDeleteService {

        private final AtomicReference<Throwable> backgroundFailure;
        private final CountDownLatch callbackFinished;

        private BackgroundDeleteService(
                AtomicReference<Throwable> backgroundFailure,
                CountDownLatch callbackFinished
        ) {
            super(repositoryObserving(new MutableLiveData<>()));
            this.backgroundFailure = backgroundFailure;
            this.callbackFinished = callbackFinished;
        }

        @Override
        public void delete(
                long warehouseItemId,
                Consumer<WarehouseItemDeleteResult> callback
        ) {
            Thread callbackThread = new Thread(() -> {
                try {
                    callback.accept(
                            WarehouseItemDeleteResult.success(1)
                    );
                } catch (Throwable throwable) {
                    backgroundFailure.set(throwable);
                } finally {
                    callbackFinished.countDown();
                }
            });

            callbackThread.start();
        }
    }
}
