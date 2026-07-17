package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.rndymi.almacentracker.adapter.in.ui.state.UiEvent;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemDetailUiState;
import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;
import org.junit.runner.RunWith;

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

        GetWarehouseItemDetailUseCase detailUseCase =
                warehouseItemId -> detailResult;

        DeleteWarehouseItemUseCase deleteUseCase =
                new BackgroundDeleteUseCase(
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
                                    detailUseCase,
                                    deleteUseCase,
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

    private static final class BackgroundDeleteUseCase
            implements DeleteWarehouseItemUseCase {

        private final AtomicReference<Throwable> backgroundFailure;
        private final CountDownLatch callbackFinished;

        private BackgroundDeleteUseCase(
                AtomicReference<Throwable> backgroundFailure,
                CountDownLatch callbackFinished
        ) {
            this.backgroundFailure = backgroundFailure;
            this.callbackFinished = callbackFinished;
        }

        @Override
        public void deleteWarehouseItem(
                long warehouseItemId,
                Consumer<DeleteWarehouseItemResult> callback
        ) {
            Thread callbackThread = new Thread(() -> {
                try {
                    callback.accept(
                            DeleteWarehouseItemResult.success()
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
