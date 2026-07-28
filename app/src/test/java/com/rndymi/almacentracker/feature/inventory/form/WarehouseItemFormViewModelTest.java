package com.rndymi.almacentracker.feature.inventory.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.WarehouseItemDetailResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;
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
                        new WarehouseItemNormalizer(),
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

    @Test
    public void initialCodeIsAppliedInCreateMode() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.applyInitialCode(" 001050 ");

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals("001050", state.getCode());
        assertTrue(state.isEditable());
        assertEquals(0, dependencies.saveService.createCallCount);
    }

    @Test
    public void initialCodeIsNormalizedToUppercase() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.applyInitialCode(" 1210a ");

        assertEquals(
                "1210 A",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void initialCodeIsIgnoredInEditMode() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        viewModel.applyInitialCode("9999");

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        assertEquals(
                "1050",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void initialCodeIsAppliedOnlyOnce() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.applyInitialCode("1050");
        viewModel.applyInitialCode("9999");

        assertEquals(
                "1050",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void initialCodeDoesNotOverwriteUserInput() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.onCodeChanged("MANUAL");
        viewModel.applyInitialCode("SCANNED");

        assertEquals(
                "MANUAL",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void emptyInitialCodeIsIgnored() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.applyInitialCode("   ");

        assertEquals(
                "",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void scannedCodeReplacesOnlyCodeInCreateMode() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.onCategoryChanged("MR");
        viewModel.onCodeChanged("1000");
        viewModel.onSiteChanged("A1");
        viewModel.onPositionChanged("Nivel 2");
        viewModel.onObservationsChanged(
                "Revisar embalaje"
        );

        viewModel.applyScannedCode(" 001050 ");

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals("MR", state.getCategory());
        assertEquals("001050", state.getCode());
        assertEquals("A1", state.getSite());
        assertEquals("Nivel 2", state.getPosition());
        assertEquals(
                "Revisar embalaje",
                state.getObservations()
        );
    }

    @Test
    public void scannedCodeClearsOnlyCodeError() {
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

        viewModel.applyScannedCode("1050");

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertNotNull(state.getCategoryError());
        assertNull(state.getCodeError());
        assertNotNull(state.getSiteError());
    }

    @Test
    public void invalidScannedCodeDoesNotReplaceCurrentCode() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.onCodeChanged("CURRENT");
        viewModel.applyScannedCode("   ");

        assertEquals(
                "CURRENT",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void scannedCodeIsIgnoredWhileSaving() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        fillForm(viewModel);
        viewModel.save();
        viewModel.applyScannedCode("9999");

        assertEquals(
                " 1050 ",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void normalizeScannedCodeUsesDomainNormalizer() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        assertEquals(
                "001050A",
                viewModel.normalizeScannedCode(
                        " 001050a "
                )
        );
    }

    @Test
    public void editAppliesConfirmedScannedCode() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        viewModel.applyScannedCode(
                " 001050a "
        );

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals(8L, state.getWarehouseItemId());
        assertEquals("MR", state.getCategory());
        assertEquals("001050A", state.getCode());
        assertEquals("A1", state.getSite());
        assertEquals("", state.getPosition());
        assertEquals(
                "Observación",
                state.getObservations()
        );
    }

    @Test
    public void editScannedCodeKeepsLeadingZeros() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        viewModel.applyScannedCode(
                "001050"
        );

        assertEquals(
                "001050",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void editRejectsEmptyScannedCode() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        viewModel.applyScannedCode(
                "   "
        );

        assertEquals(
                "1050",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void editRejectsNullScannedCode() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        viewModel.applyScannedCode(
                null
        );

        assertEquals(
                "1050",
                viewModel.getUiState()
                        .getValue()
                        .getCode()
        );
    }

    @Test
    public void editIgnoresScannedCodeWhileLoading() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        viewModel.applyScannedCode(
                "2050"
        );

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertTrue(state.isLoading());
        assertEquals("", state.getCode());
    }

    @Test
    public void editIgnoresScannedCodeWhileSaving() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        viewModel.save();

        viewModel.applyScannedCode(
                "2050"
        );

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertTrue(state.isSaving());
        assertEquals("1050", state.getCode());
    }

    @Test
    public void editScannedCodeDoesNotSaveAutomatically() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        viewModel.applyScannedCode(
                "2050"
        );

        assertEquals(
                0L,
                dependencies.saveService.lastUpdatedId
        );

        assertNull(
                dependencies.saveService.lastFormData
        );
    }

    @Test
    public void editSavesScannedCodeUsingExistingUpdateFlow() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(8L);

        dependencies.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        item(8L)
                )
        );

        viewModel.applyScannedCode(
                "2050"
        );

        viewModel.save();

        assertEquals(
                8L,
                dependencies.saveService.lastUpdatedId
        );

        assertNotNull(
                dependencies.saveService.lastFormData
        );

        assertEquals(
                "2050",
                dependencies.saveService
                        .lastFormData
                        .getCode()
        );
    }

    @Test
    public void createModeKeepsScannedCodeBehavior() {
        TestDependencies dependencies =
                new TestDependencies();

        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.onCategoryChanged("MR");
        viewModel.onSiteChanged("A1");

        viewModel.applyScannedCode(
                " 001050 "
        );

        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals(
                WarehouseItemFormMode.CREATE,
                state.getMode()
        );
        assertEquals("MR", state.getCategory());
        assertEquals("001050", state.getCode());
        assertEquals("A1", state.getSite());
    }
}
