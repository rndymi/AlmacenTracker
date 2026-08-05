package com.rndymi.almacentracker.architecture;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class RoomContractArchitectureTest {

    private static final List<String> WAREHOUSE_ITEM_COLUMNS =
            Arrays.asList(
                    "category",
                    "code",
                    "site",
                    "position",
                    "observations",
                    "created_at",
                    "updated_at"
            );

    @Test
    public void databaseIdentityAndVersionRemainStable()
            throws IOException {

        String appContainer = readSource(
                "app/AppContainer.java"
        );
        String database = readSource(
                "data/local/room/database/"
                        + "AlmacenTrackerDatabase.java"
        );

        assertTrue(
                "The existing database file name must remain stable",
                appContainer.contains(
                        "\"almacen_tracker.db\""
                )
        );
        assertTrue(
                "The database version must include the history schema",
                database.contains("version = 4")
        );
        assertTrue(
                "The history schema migration must remain registered",
                appContainer.contains(
                        "AlmacenTrackerMigrations.MIGRATION_1_2"
                )
        );
        assertTrue(
                "The destinations migration must remain registered",
                appContainer.contains(
                        "AlmacenTrackerMigrations.MIGRATION_2_3"
                )
        );
        assertTrue(
                "The global destination migration must remain registered",
                appContainer.contains(
                        "AlmacenTrackerMigrations.MIGRATION_3_4"
                )
        );
    }

    @Test
    public void warehouseItemSchemaRemainsStable()
            throws IOException {

        String entity = readSource(
                "data/local/room/entity/"
                        + "WarehouseItemEntity.java"
        );

        assertTrue(
                "Warehouse item table name must remain stable",
                entity.contains(
                        "tableName = \"warehouse_items\""
                )
        );
        assertTrue(
                "Warehouse item id must remain the generated key",
                entity.contains(
                        "@PrimaryKey(autoGenerate = true)"
                )
        );
        assertTrue(
                "Category and code must remain a composite index",
                entity.contains(
                        "value = {\"category\", \"code\"}"
                )
        );
        assertTrue(
                "Category and code index must remain unique",
                entity.contains("unique = true")
        );

        for (String column : WAREHOUSE_ITEM_COLUMNS) {
            assertTrue(
                    "Room column must remain stable: " + column,
                    entity.contains(
                            "@ColumnInfo(name = \""
                                    + column
                                    + "\")"
                    )
            );
        }
    }

    @Test
    public void daoKeepsConflictAndTransactionContracts()
            throws IOException {

        String dao = readSource(
                "data/local/room/dao/WarehouseItemDao.java"
        );

        assertTrue(
                "Room writes must keep ABORT conflict handling",
                dao.contains(
                        "onConflict = OnConflictStrategy.ABORT"
                )
        );
        assertTrue(
                "Batch insertion must remain transactional",
                hasTransactionalMethod(dao, "insertAll(")
        );
        assertTrue(
                "Backup restoration must remain transactional",
                hasTransactionalMethod(dao, "replaceAll(")
        );
    }

    private boolean hasTransactionalMethod(
            String source,
            String method
    ) {
        int methodIndex = source.indexOf(method);

        if (methodIndex < 0) {
            return false;
        }

        int transactionIndex = source.lastIndexOf(
                "@Transaction",
                methodIndex
        );

        return transactionIndex >= 0
                && source.substring(
                        transactionIndex,
                        methodIndex
                ).lines().count() <= 3;
    }

    private String readSource(String relativePath)
            throws IOException {

        Path source = findExistingPath(
                "src/main/java/com/rndymi/almacentracker/"
                        + relativePath,
                "app/src/main/java/com/rndymi/almacentracker/"
                        + relativePath
        );

        return Files.readString(
                source,
                StandardCharsets.UTF_8
        );
    }

    private Path findExistingPath(String... candidates) {
        for (String candidate : candidates) {
            Path path = Path.of(candidate)
                    .toAbsolutePath()
                    .normalize();

            if (Files.exists(path)) {
                return path;
            }
        }

        throw new IllegalStateException(
                "Could not locate project source: "
                        + Arrays.toString(candidates)
        );
    }
}
