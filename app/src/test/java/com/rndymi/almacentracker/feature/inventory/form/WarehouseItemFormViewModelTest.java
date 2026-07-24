package com.rndymi.almacentracker.feature.inventory.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.result.CreateWarehouseItemResult;
import com.rndymi.almacentracker.application.result.UpdateWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

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

        CreateWarehouseItemCommand command =
                dependencies.createUseCase.lastCommand;

        assertNotNull(command);
        assertEquals(" mr ", command.getCategory());
        assertEquals(" 1050 ", command.getCode());
        assertEquals(" a1 ", command.getSite());
        assertEquals(" Nivel 2 ", command.getPosition());
        assertEquals(
                " Observación ",
                command.getObservations()
        );
    }

    @Test
    public void validationResultMarksRequiredFields() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemFormViewModel viewModel =
                dependencies.createViewModel(0L);

        viewModel.save();
        dependencies.createUseCase.complete(
                CreateWarehouseItemResult.validationError(
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
        dependencies.createUseCase.complete(
                CreateWarehouseItemResult.duplicate()
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
                dependencies.createUseCase.callCount
        );

        dependencies.createUseCase.complete(
                CreateWarehouseItemResult.persistenceError(
                        new IllegalStateException("write")
                )
        );
        viewModel.save();

        assertEquals(
                2,
                dependencies.createUseCase.callCount
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
        dependencies.createUseCase.complete(
                CreateWarehouseItemResult.success(17L)
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

        UpdateWarehouseItemCommand command =
                dependencies.updateUseCase.lastCommand;

        assertNotNull(command);
        assertEquals(8L, command.getWarehouseItemId());
        assertEquals("MR", command.getCategory());
        assertEquals("1050", command.getCode());
        assertEquals("B2", command.getSite());
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

        private final RecordingCreateUseCase
                createUseCase =
                new RecordingCreateUseCase();
        private final RecordingUpdateUseCase
                updateUseCase =
                new RecordingUpdateUseCase();
        private final MutableLiveData<WarehouseItemDetailResult>
                detailResult = new MutableLiveData<>();

        private WarehouseItemFormViewModel createViewModel(
                long warehouseItemId
        ) {
            GetWarehouseItemDetailUseCase detailUseCase =
                    ignored -> detailResult;

            WarehouseItemFormViewModel viewModel =
                    new WarehouseItemFormViewModel(
                    createUseCase,
                    updateUseCase,
                    detailUseCase,
                    warehouseItemId
            );

            viewModel.getUiState().observeForever(
                    ignored -> {
                    }
            );

            return viewModel;
        }
    }

    private static final class RecordingCreateUseCase
            implements CreateWarehouseItemUseCase {

        private int callCount;
        private CreateWarehouseItemCommand lastCommand;
        private Consumer<CreateWarehouseItemResult> callback;

        @Override
        public void createWarehouseItem(
                CreateWarehouseItemCommand command,
                Consumer<CreateWarehouseItemResult> callback
        ) {
            callCount++;
            lastCommand = command;
            this.callback = callback;
        }

        private void complete(
                CreateWarehouseItemResult result
        ) {
            callback.accept(result);
        }
    }

    private static final class RecordingUpdateUseCase
            implements UpdateWarehouseItemUseCase {

        private UpdateWarehouseItemCommand lastCommand;

        @Override
        public void updateWarehouseItem(
                UpdateWarehouseItemCommand command,
                Consumer<UpdateWarehouseItemResult> callback
        ) {
            lastCommand = command;
        }
    }
}
