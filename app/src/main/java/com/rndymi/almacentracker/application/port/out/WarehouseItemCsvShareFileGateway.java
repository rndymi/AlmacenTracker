package com.rndymi.almacentracker.application.port.out;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

public interface WarehouseItemCsvShareFileGateway {

    void createShareableFile(
            List<WarehouseItem> warehouseItems,
            String suggestedFileName,
            WarehouseItemCsvShareFileCallback callback
    );
}