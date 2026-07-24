package com.rndymi.almacentracker.feature.data_management.share;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.core.csv.share.WarehouseItemCsvShareFileGateway.ShareFileCallback;
import com.rndymi.almacentracker.core.csv.share.WarehouseItemCsvShareFileGateway;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;
import com.rndymi.almacentracker.core.csv.share.ShareableCsvFile;
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
                ShareFileCallback callback
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
            extends WarehouseItemRepositoryStub {

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
                RepositoryCallback<List<WarehouseItem>> callback
        ) {
            if (findAllError != null) {
                callback.onError(findAllError);
                return;
            }

            callback.onSuccess(items);
        }

    }
}
