package com.rndymi.almacentracker.application.port.in;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;

public interface ObserveWarehouseItemFilterOptionsUseCase {

    LiveData<WarehouseItemFilterOptionsResult>
    observeFilterOptions();
}