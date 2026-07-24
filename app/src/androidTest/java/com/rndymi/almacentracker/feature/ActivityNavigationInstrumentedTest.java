package com.rndymi.almacentracker.feature;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.widget.EditText;
import android.widget.ListAdapter;

import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.data.local.room.dao.WarehouseItemDao;
import com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.data.local.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.feature.data_management.common.DataManagementActivity;
import com.rndymi.almacentracker.feature.inventory.detail.ItemDetailActivity;
import com.rndymi.almacentracker.feature.inventory.form.ItemFormActivity;
import com.rndymi.almacentracker.feature.inventory.list.MainActivity;
import com.rndymi.almacentracker.feature.inventory.list.WarehouseItemListUiState;
import com.rndymi.almacentracker.feature.inventory.list.WarehouseItemListViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public final class ActivityNavigationInstrumentedTest {

    @Before
    public void setUp() {
        clearWarehouseItems();
    }

    @After
    public void tearDown() {
        clearWarehouseItems();
    }

    @Test
    public void migratedActivitiesRemainDeclared()
            throws PackageManager.NameNotFoundException {

        Context context =
                ApplicationProvider.getApplicationContext();
        PackageManager packageManager =
                context.getPackageManager();
        List<Class<? extends Activity>> activities =
                Arrays.asList(
                        MainActivity.class,
                        ItemFormActivity.class,
                        ItemDetailActivity.class,
                        DataManagementActivity.class
                );

        for (Class<? extends Activity> activity : activities) {
            ActivityInfo activityInfo =
                    packageManager.getActivityInfo(
                            new ComponentName(
                                    context,
                                    activity
                            ),
                            0
                    );

            assertNotNull(activityInfo);
            assertEquals(
                    activity.getName(),
                    activityInfo.name
            );
        }
    }

    @Test
    public void inventoryNavigationIntentsKeepItemIdentity() {
        Context context =
                ApplicationProvider.getApplicationContext();
        long warehouseItemId = 42L;

        Intent detailIntent =
                ItemDetailActivity.createIntent(
                        context,
                        warehouseItemId
                );
        Intent editIntent =
                ItemFormActivity.createEditIntent(
                        context,
                        warehouseItemId
                );

        assertIntent(
                detailIntent,
                ItemDetailActivity.class,
                ItemDetailActivity.EXTRA_WAREHOUSE_ITEM_ID,
                warehouseItemId
        );
        assertIntent(
                editIntent,
                ItemFormActivity.class,
                ItemFormActivity.EXTRA_WAREHOUSE_ITEM_ID,
                warehouseItemId
        );
    }

    @Test
    public void migratedActivitiesLaunchSuccessfully() {
        Context context =
                ApplicationProvider.getApplicationContext();

        assertLaunches(
                new Intent(context, MainActivity.class)
        );
        assertLaunches(
                new Intent(context, ItemFormActivity.class)
        );
        assertLaunches(
                ItemDetailActivity.createIntent(
                        context,
                        1L
                )
        );
        assertLaunches(
                new Intent(
                        context,
                        DataManagementActivity.class
                )
        );
    }

    @Test
    public void createFormInputSurvivesActivityRecreation() {
        Context context =
                ApplicationProvider.getApplicationContext();

        try (ActivityScenario<ItemFormActivity> scenario =
                     ActivityScenario.launch(
                             new Intent(
                                     context,
                                     ItemFormActivity.class
                             )
                     )) {
            scenario.onActivity(activity -> {
                ((EditText) activity.findViewById(
                        R.id.categoryEditText
                )).setText("MR");
                ((EditText) activity.findViewById(
                        R.id.codeEditText
                )).setText("ROTATE-100");
                ((EditText) activity.findViewById(
                        R.id.siteEditText
                )).setText("A1");
                ((EditText) activity.findViewById(
                        R.id.positionEditText
                )).setText("Nivel 2");
                ((EditText) activity.findViewById(
                        R.id.observationsEditText
                )).setText("Conservada");
            });

            scenario.recreate();

            scenario.onActivity(activity -> {
                assertEditText(
                        activity,
                        R.id.categoryEditText,
                        "MR"
                );
                assertEditText(
                        activity,
                        R.id.codeEditText,
                        "ROTATE-100"
                );
                assertEditText(
                        activity,
                        R.id.siteEditText,
                        "A1"
                );
                assertEditText(
                        activity,
                        R.id.positionEditText,
                        "Nivel 2"
                );
                assertEditText(
                        activity,
                        R.id.observationsEditText,
                        "Conservada"
                );
            });
        }
    }

    @Test
    public void longPressSelectionSurvivesActivityRecreation()
            throws InterruptedException {

        long warehouseItemId = insertWarehouseItem();
        Context context =
                ApplicationProvider.getApplicationContext();

        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(
                             new Intent(
                                     context,
                                     MainActivity.class
                             )
                     )) {
            WarehouseItemListViewModel beforeRotation =
                    awaitContentViewModel(scenario);

            onView(withText("MR · ROTATE-100"))
                    .perform(longClick());
            onView(withId(R.id.selectionToolbar))
                    .check(matches(isDisplayed()));

            scenario.onActivity(activity -> {
                assertTrue(beforeRotation.hasSelection());
                assertTrue(
                        beforeRotation.getSelectionUiState()
                                .getValue()
                                .getSelectedIds()
                                .contains(warehouseItemId)
                );
            });

            scenario.recreate();

            scenario.onActivity(activity -> {
                WarehouseItemListViewModel afterRotation =
                        new ViewModelProvider(activity)
                                .get(
                                        WarehouseItemListViewModel.class
                                );

                assertSame(beforeRotation, afterRotation);
                assertTrue(afterRotation.hasSelection());
                assertTrue(
                        afterRotation.getSelectionUiState()
                                .getValue()
                                .getSelectedIds()
                                .contains(warehouseItemId)
                );
            });

            onView(withId(R.id.selectionToolbar))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void filterOptionsRemainAvailableAfterRecreation()
            throws InterruptedException {

        insertWarehouseItem();
        Context context =
                ApplicationProvider.getApplicationContext();

        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(
                             new Intent(
                                     context,
                                     MainActivity.class
                             )
                     )) {
            awaitFilterOptions(scenario);

            scenario.onActivity(activity -> {
                MaterialAutoCompleteTextView dropdown =
                        activity.findViewById(
                                R.id.categoryFilterDropdown
                        );

                assertFalse(dropdown.isSaveEnabled());
                assertDropdownContains(dropdown, "MR");

                dropdown.requestFocus();
                dropdown.showDropDown();
            });

            scenario.recreate();
            awaitFilterOptions(scenario);

            scenario.onActivity(activity -> {
                MaterialAutoCompleteTextView dropdown =
                        activity.findViewById(
                                R.id.categoryFilterDropdown
                        );

                assertFalse(dropdown.isSaveEnabled());
                assertDropdownContains(dropdown, "MR");
            });
        }
    }

    private void assertIntent(
            Intent intent,
            Class<? extends Activity> destination,
            String extraName,
            long expectedId
    ) {
        assertNotNull(intent.getComponent());
        assertEquals(
                destination.getName(),
                intent.getComponent().getClassName()
        );
        assertEquals(
                expectedId,
                intent.getLongExtra(extraName, -1L)
        );
    }

    private void assertLaunches(Intent intent) {
        try (ActivityScenario<? extends Activity> scenario =
                     ActivityScenario.launch(intent)) {
            scenario.onActivity(
                    activity -> assertFalse(
                            activity.isFinishing()
                    )
            );
        }
    }

    private void assertEditText(
            Activity activity,
            int viewId,
            String expected
    ) {
        EditText editText =
                activity.findViewById(viewId);

        assertEquals(
                expected,
                editText.getText().toString()
        );
    }

    private WarehouseItemListViewModel awaitContentViewModel(
            ActivityScenario<MainActivity> scenario
    ) throws InterruptedException {
        AtomicReference<WarehouseItemListViewModel> result =
                new AtomicReference<>();

        for (int attempt = 0; attempt < 30; attempt++) {
            scenario.onActivity(activity -> {
                WarehouseItemListViewModel viewModel =
                        new ViewModelProvider(activity)
                                .get(
                                        WarehouseItemListViewModel.class
                                );
                WarehouseItemListUiState state =
                        viewModel.getUiState().getValue();

                if (state != null
                        && state.getStatus()
                        == WarehouseItemListUiState.Status.CONTENT) {
                    result.set(viewModel);
                }
            });

            if (result.get() != null) {
                return result.get();
            }

            Thread.sleep(100L);
        }

        throw new AssertionError(
                "Inventory content was not rendered"
        );
    }

    private void awaitFilterOptions(
            ActivityScenario<MainActivity> scenario
    ) throws InterruptedException {

        AtomicReference<Boolean> optionsAvailable =
                new AtomicReference<>(false);

        for (int attempt = 0; attempt < 30; attempt++) {
            scenario.onActivity(activity -> {
                MaterialAutoCompleteTextView dropdown =
                        activity.findViewById(
                                R.id.categoryFilterDropdown
                        );

                optionsAvailable.set(
                        dropdownContains(dropdown, "MR")
                );
            });

            if (optionsAvailable.get()) {
                return;
            }

            Thread.sleep(100L);
        }

        throw new AssertionError(
                "Category filter options were not rendered"
        );
    }

    private void assertDropdownContains(
            MaterialAutoCompleteTextView dropdown,
            String expectedOption
    ) {
        assertTrue(
                "Missing dropdown option: " + expectedOption,
                dropdownContains(dropdown, expectedOption)
        );
    }

    private boolean dropdownContains(
            MaterialAutoCompleteTextView dropdown,
            String expectedOption
    ) {
        ListAdapter adapter = dropdown.getAdapter();

        if (adapter == null) {
            return false;
        }

        for (int index = 0; index < adapter.getCount(); index++) {
            if (expectedOption.equals(
                    String.valueOf(adapter.getItem(index))
            )) {
                return true;
            }
        }

        return false;
    }

    private long insertWarehouseItem() {
        AlmacenTrackerDatabase database = openDatabase();

        try {
            return database.warehouseItemDao().insert(
                    new WarehouseItemEntity(
                            0L,
                            "MR",
                            "ROTATE-100",
                            "A1",
                            null,
                            null,
                            100L,
                            100L
                    )
            );
        } finally {
            database.close();
        }
    }

    private void clearWarehouseItems() {
        AlmacenTrackerDatabase database = openDatabase();

        try {
            WarehouseItemDao dao =
                    database.warehouseItemDao();
            dao.deleteAllInternal();
        } finally {
            database.close();
        }
    }

    private AlmacenTrackerDatabase openDatabase() {
        Context context =
                ApplicationProvider.getApplicationContext();

        return Room.databaseBuilder(
                context,
                AlmacenTrackerDatabase.class,
                "almacen_tracker.db"
        )
                .allowMainThreadQueries()
                .build();
    }
}
