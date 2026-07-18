package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvShareFileCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvShareFileGateway;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.result.ShareWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.ShareableCsvFile;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class ShareWarehouseItemsServiceTest {

    @Test
    public void prepareWithItemsReturnsShareableFile() {
        WarehouseItem item = createItem();

        FakeRepository repository =
                new FakeRepository(
                        Collections.singletonList(item),
                        null
                );

        FakeShareGateway gateway =
                new FakeShareGateway();

        ShareWarehouseItemsService service =
                new ShareWarehouseItemsService(
                        repository,
                        gateway,
                        () -> "almacentracker-share-test.csv"
                );

        ResultHolder holder = new ResultHolder();

        service.prepareWarehouseItemsForSharing(
                holder::setResult
        );

        assertNotNull(holder.result);
        assertEquals(
                ShareWarehouseItemsResult.Status.SUCCESS,
                holder.result.getStatus()
        );

        assertNotNull(holder.result.getShareableFile());

        assertEquals(
                "content://test/shared.csv",
                holder.result
                        .getShareableFile()
                        .getContentReference()
        );

        assertEquals(
                1,
                holder.result
                        .getShareableFile()
                        .getRecordCount()
        );

        assertTrue(gateway.called);
    }

    @Test
    public void emptyDatabaseDoesNotInvokeGateway() {
        FakeRepository repository =
                new FakeRepository(
                        Collections.emptyList(),
                        null
                );

        FakeShareGateway gateway =
                new FakeShareGateway();

        ShareWarehouseItemsService service =
                new ShareWarehouseItemsService(
                        repository,
                        gateway,
                        () -> "almacentracker-share-test.csv"
                );

        ResultHolder holder = new ResultHolder();

        service.prepareWarehouseItemsForSharing(
                holder::setResult
        );

        assertEquals(
                ShareWarehouseItemsResult.Status
                        .EMPTY_DATABASE,
                holder.result.getStatus()
        );

        assertFalse(gateway.called);
    }

    @Test
    public void repositoryErrorReturnsReadError() {
        FakeRepository repository =
                new FakeRepository(
                        null,
                        new IllegalStateException("Room error")
                );

        FakeShareGateway gateway =
                new FakeShareGateway();

        ShareWarehouseItemsService service =
                new ShareWarehouseItemsService(
                        repository,
                        gateway,
                        () -> "almacentracker-share-test.csv"
                );

        ResultHolder holder = new ResultHolder();

        service.prepareWarehouseItemsForSharing(
                holder::setResult
        );

        assertEquals(
                ShareWarehouseItemsResult.Status.READ_ERROR,
                holder.result.getStatus()
        );

        assertFalse(gateway.called);
    }

    private WarehouseItem createItem() {
        return new WarehouseItem(
                1L,
                "MR",
                "1050",
                "A1",
                "Nivel 2",
                null,
                100L,
                100L
        );
    }

    private static final class ResultHolder {
        private ShareWarehouseItemsResult result;

        private void setResult(
                ShareWarehouseItemsResult result
        ) {
            this.result = result;
        }
    }

    private static final class FakeShareGateway
            implements WarehouseItemCsvShareFileGateway {

        private boolean called;

        @Override
        public void createShareableFile(
                List<WarehouseItem> warehouseItems,
                String suggestedFileName,
                WarehouseItemCsvShareFileCallback callback
        ) {
            called = true;

            callback.onSuccess(
                    new ShareableCsvFile(
                            "content://test/shared.csv",
                            suggestedFileName,
                            "text/csv",
                            warehouseItems.size()
                    )
            );
        }
    }

    private static final class FakeRepository
            implements WarehouseItemRepository {

        private final List<WarehouseItem> items;
        private final Throwable findAllError;

        private FakeRepository(
                List<WarehouseItem> items,
                Throwable findAllError
        ) {
            this.items = items;
            this.findAllError = findAllError;
        }

        @Override
        public void findAll(
                WarehouseItemsFindCallback callback
        ) {
            if (findAllError != null) {
                callback.onError(findAllError);
                return;
            }

            callback.onSuccess(items);
        }

        @Override
        public LiveData<WarehouseItemsResult> observeAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemsResult> search(
                String query
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemsResult> filter(
                WarehouseItemFilterCriteria criteria
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemFilterOptionsResult>
        observeFilterOptions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemDetailResult> observeById(
                long warehouseItemId
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findById(
                long warehouseItemId,
                WarehouseItemFindCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void existsByCategoryAndCode(
                String category,
                String code,
                WarehouseItemDuplicateCheckCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void existsByCategoryAndCodeExcludingId(
                String category,
                String code,
                long excludedWarehouseItemId,
                WarehouseItemDuplicateCheckCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(
                WarehouseItem warehouseItem,
                WarehouseItemInsertCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(
                WarehouseItem warehouseItem,
                WarehouseItemUpdateCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(
                long warehouseItemId,
                WarehouseItemDeleteCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteByIds(
                List<Long> warehouseItemIds,
                WarehouseItemsDeleteCallback callback
        ) {
            throw new UnsupportedOperationException();
        }
    }
}
