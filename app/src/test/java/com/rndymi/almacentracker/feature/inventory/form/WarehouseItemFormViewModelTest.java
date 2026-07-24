package com.rndymi.almacentracker.feature.inventory.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.data.repository.WarehouseItemDetailResult;
import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

import org.junit.Rule;
import org.junit.Test;

import java.util.function.Consumer;

public final class WarehouseItemFormViewModelTest {

    @Rule
    public final InstantTaskExecutorRule
            instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Test
    public void createModeStartsEmptyAndEditable() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals(
                WarehouseItemFormMode.CREATE,
                state.getMode()
        );
        assertEquals("", state.getCategory());
        assertEquals("", state.getCode());
        assertEquals("", state.getSite());
        assertTrue(state.isEditable());
    }

    @Test
    public void createForwardsRequiredAndOptionalFields() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        fillForm(viewModel);
        viewModel.save();

        WarehouseItemFormData formData =
                dependencies.saveService.lastFormData;

        assertNotNull(formData);
        assertEquals(" mr ", formData.getCategory());
        assertEquals(" 1050 ", formData.getCode());
        assertEquals(" a1 ", formData.getSite());
        assertEquals(" Nivel 2 ", formData.getPosition());
        assertEquals(
                " Observación ",
                formData.getObservations()
        );
    }

    @Test
    public void validationResultMarksRequiredFields() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.save();
        dependencies.saveService.complete(
                WarehouseItemSaveResult.validationError(
                        true,
                        true,
                        true
                )
        );

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state.getCategoryError());
        assertNotNull(state.getCodeError());
        assertNotNull(state.getSiteError());
        assertFalse(state.isSaving());
    }

    @Test
    public void duplicateResultIsExposedAsFormError() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        fillForm(viewModel);
        viewModel.save();
        dependencies.saveService.complete(
                WarehouseItemSaveResult.duplicate()
        );

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertTrue(
                state.getGeneralError().contains(
                        "categoría y código"
                )
        );
        assertFalse(state.isSaving());
    }

    @Test
    public void doubleSaveStartsOnlyOneCreateOperation() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        fillForm(viewModel);
        viewModel.save();
        viewModel.save();

        assertEquals(
                1,
                dependencies.saveService.createCallCount
        );

        dependencies.saveService.complete(
                WarehouseItemSaveResult.persistenceError(
                        new IllegalStateException("write")
                )
        );
        viewModel.save();

        assertEquals(
                2,
                dependencies.saveService.createCallCount
        );
    }

    @Test
    public void creationSuccessIsDeliveredOnlyOnce() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        fillForm(viewModel);
        viewModel.save();
        dependencies.saveService.complete(
                WarehouseItemSaveResult.success(17L)
        );

        UiEvent<Long> event =
                viewModel.getCreationSuccess().getValue();

        assertNotNull(event);
        assertEquals(
                Long.valueOf(17L),
                event.getContentIfNotHandled()
        );
        assertNull(event.getContentIfNotHandled());
    }

    @Test
    public void editLoadsCompleteExistingItem() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertEquals(WarehouseItemFormMode.EDIT, state.getMode());
        assertEquals(8L, state.getWarehouseItemId());
        assertEquals("MR", state.getCategory());
        assertEquals("1050", state.getCode());
        assertEquals("A1", state.getSite());
        assertEquals("", state.getPosition());
        assertEquals("Observación", state.getObservations());
        assertTrue(state.isEditable());
    }

    @Test
    public void editKeepsIdAndForwardsChanges() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );
        viewModel.onSiteChanged("B2");
        viewModel.save();

        WarehouseItemFormData formData =
                dependencies.saveService.lastFormData;

        assertNotNull(formData);
        assertEquals(8L, dependencies.saveService.lastUpdatedId);
        assertEquals("MR", formData.getCategory());
        assertEquals("1050", formData.getCode());
        assertEquals("B2", formData.getSite());
    }

    @Test
    public void lateDetailEmissionDoesNotOverwriteUserInput() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        viewModel.onCategoryChanged("USER");
        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        assertEquals(
                "USER",
                viewModel.getUiState()
                        .getValue()
                        .getCategory()
        );
    }

    @Test
    public void missingEditItemIsNotEditable() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.notFound()
        );

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertTrue(state.isNotFound());
        assertFalse(state.isEditable());
    }

    private void fillForm(
            WarehouseItemFormViewModel viewModel
    ) {
        viewModel.onCategoryChanged(" mr ");
        viewModel.onCodeChanged(" 1050 ");
        viewModel.onSiteChanged(" a1 ");
        viewModel.onPositionChanged(" Nivel 2 ");
        viewModel.onObservationsChanged(" Observación ");
    }

    private WarehouseItem item(long id) {
        return new WarehouseItem(
                id,
                "MR",
                "1050",
                "A1",
                null,
                "Observación",
                100L,
                200L
        );
    }

    private static final class TestDependencies {

        private final RecordingSaveService saveService =
                new RecordingSaveService();
        private final MutableLiveData<WarehouseItemDetailResult>
                detailResult = new MutableLiveData<>();

        private WarehouseItemFormViewModel createViewModel(
                long warehouseItemId
        ) {
            WarehouseItemRepositoryStub repository =
                    new WarehouseItemRepositoryStub() {
                        @Override
                        public LiveData<WarehouseItemDetailResult>
                        observeById(long ignored) {
                            return detailResult;
                        }
                    };

            WarehouseItemFormViewModel viewModel =
                    new WarehouseItemFormViewModel(
                            saveService,
                            repository,
                            warehouseItemId
                    );

            viewModel.getUiState().observeForever(
                    ignored -> {
                    }
            );

            return viewModel;
        }
    }

    private static final class RecordingSaveService
            extends WarehouseItemSaveService {

        private int createCallCount;
        private long lastUpdatedId;
        private WarehouseItemFormData lastFormData;
        private Consumer<WarehouseItemSaveResult> callback;

        private RecordingSaveService() {
            super(
                    new WarehouseItemRepositoryStub(),
                    () -> 0L
            );
        }

        @Override
        public void create(
                WarehouseItemFormData formData,
                Consumer<WarehouseItemSaveResult> callback
        ) {
            createCallCount++;
            lastFormData = formData;
            this.callback = callback;
        }

        @Override
        public void update(
                long warehouseItemId,
                WarehouseItemFormData formData,
                Consumer<WarehouseItemSaveResult> callback
        ) {
            lastUpdatedId = warehouseItemId;
            lastFormData = formData;
            this.callback = callback;
        }

        private void complete(
                WarehouseItemSaveResult result
        ) {
            callback.accept(result);
        }
    }
}
