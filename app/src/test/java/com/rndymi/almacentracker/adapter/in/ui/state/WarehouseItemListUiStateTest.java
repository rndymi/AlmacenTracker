package com.rndymi.almacentracker.adapter.in.ui.state;

import static org.junit.Assert.assertEquals;

import com.rndymi.almacentracker.application.port.in.PositionFilter;
import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptions;

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
                Collections.emptyList(),
                WarehouseItemFilterCriteria.empty(),
                WarehouseItemFilterOptions.empty()
        ).getNoResultsReason();
    }
}