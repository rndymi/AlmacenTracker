package com.rndymi.almacentracker.feature.inventory.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.rndymi.almacentracker.application.port.in.PositionFilter;
import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptions;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.Collections;

public class WarehouseItemListUiStateTest {

    @Test
    public void derivesSearchReason() {
        WarehouseItemListUiState state =
                WarehouseItemListUiState.noResults(
                        WarehouseItemFilterCriteria.of(
                                "105",
                                null,
                                null,
                                PositionFilter.all()
                        ),
                        WarehouseItemFilterOptions.empty()
                );

        assertEquals(
                NoResultsReason.SEARCH,
                state.getNoResultsReason()
        );
    }

    @Test
    public void derivesFiltersReason() {
        WarehouseItemListUiState state =
                WarehouseItemListUiState.noResults(
                        WarehouseItemFilterCriteria.of(
                                "",
                                "MR",
                                null,
                                PositionFilter.all()
                        ),
                        WarehouseItemFilterOptions.empty()
                );

        assertEquals(
                NoResultsReason.FILTERS,
                state.getNoResultsReason()
        );
    }

    @Test
    public void derivesSearchAndFiltersReason() {
        WarehouseItemListUiState state =
                WarehouseItemListUiState.noResults(
                        WarehouseItemFilterCriteria.of(
                                "105",
                                "MR",
                                "A1",
                                PositionFilter.exact(
                                        "Nivel 2"
                                )
                        ),
                        WarehouseItemFilterOptions.empty()
                );

        assertEquals(
                NoResultsReason.SEARCH_AND_FILTERS,
                state.getNoResultsReason()
        );
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsNoResultsReasonForContentState() {
        WarehouseItemListUiState.content(
                Collections.singletonList(item()),
                WarehouseItemFilterCriteria.empty(),
                WarehouseItemFilterOptions.empty()
        ).getNoResultsReason();
    }

    @Test
    public void contentRequiresAtLeastOneItem() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WarehouseItemListUiState.content(
                        Collections.emptyList(),
                        WarehouseItemFilterCriteria.empty(),
                        WarehouseItemFilterOptions.empty()
                )
        );
    }

    @Test
    public void noResultsRequiresActiveCriteria() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WarehouseItemListUiState.noResults(
                        WarehouseItemFilterCriteria.empty(),
                        WarehouseItemFilterOptions.empty()
                )
        );
    }

    @Test
    public void errorRequiresMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WarehouseItemListUiState.error(
                        WarehouseItemFilterCriteria.empty(),
                        WarehouseItemFilterOptions.empty(),
                        "   "
                )
        );
    }

    private WarehouseItem item() {
        return new WarehouseItem(
                1L,
                "MR",
                "1050",
                "A1",
                null,
                null,
                100L,
                100L
        );
    }
}
