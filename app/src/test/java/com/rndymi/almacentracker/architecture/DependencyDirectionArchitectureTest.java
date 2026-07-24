package com.rndymi.almacentracker.architecture;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class DependencyDirectionArchitectureTest {

    private static final String PROJECT_PACKAGE =
            "com.rndymi.almacentracker.";

    private static final List<String>
            FORBIDDEN_FEATURE_INFRASTRUCTURE_TYPES =
            Arrays.asList(
                    "WarehouseItemDao",
                    "AlmacenTrackerDatabase",
                    "RoomWarehouseItemRepository",
                    "ContentResolver"
            );

    private static final List<String>
            LEGACY_HEXAGONAL_PACKAGES =
            Arrays.asList(
                    "adapter" + ".in.ui",
                    "adapter" + ".out.file",
                    "adapter" + ".out.persistence",
                    "configura" + "tion",
                    "applica" + "tion"
            );

    private static final List<String>
            FORBIDDEN_DATA_MANAGEMENT_VIEW_MODEL_TYPES =
            Arrays.asList(
                    "WarehouseItemCsvCodec",
                    "WarehouseBackupCsvCodec",
                    "WarehouseItemNormalizer",
                    "WarehouseItemValidator",
                    "WarehouseItemIdentity",
                    "WarehouseItemDao",
                    "AlmacenTrackerDatabase",
                    "RoomWarehouseItemRepository",
                    "WarehouseItemEntity",
                    "ContentResolver",
                    "android.net.Uri",
                    "java.io.",
                    "LocalDate",
                    "LocalDateTime",
                    "System.currentTimeMillis",
                    "new WarehouseItem("
            );

    private static final Pattern SERVICE_CONSTRUCTION =
            Pattern.compile(
                    "new\\s+[A-Za-z0-9_]*Service\\s*\\("
            );

    @Test
    public void domainDependsOnlyOnJavaAndDomain()
            throws IOException {

        Path domainDirectory = findSourceDirectory("domain");

        try (Stream<Path> files = javaFiles(domainDirectory)) {
            files.forEach(this::assertDomainImportsAreAllowed);
        }
    }

    @Test
    public void coreDependsOnlyOnJavaCoreAndDomain()
            throws IOException {

        Path coreDirectory = findSourceDirectory("core");

        try (Stream<Path> files = javaFiles(coreDirectory)) {
            files.forEach(this::assertCoreImportsAreAllowed);
        }
    }

    @Test
    public void dataDoesNotDependOnFeatures()
            throws IOException {

        String dataSource = readJavaSource(
                findSourceDirectory("data")
        );

        assertFalse(
                "The data layer must not import feature classes",
                dataSource.contains(
                        "import " + PROJECT_PACKAGE + "feature."
                )
        );
    }

    @Test
    public void legacyHexagonalPackagesAreAbsent()
            throws IOException {

        Path productionPackage = findExistingPath(
                "src/main/java/com/rndymi/almacentracker",
                "app/src/main/java/com/rndymi/almacentracker"
        );
        String productionSource =
                readJavaSource(productionPackage);

        for (String legacyPackage
                : LEGACY_HEXAGONAL_PACKAGES) {
            assertFalse(
                    "Legacy package reference must be removed: "
                            + legacyPackage,
                    productionSource.contains(
                            PROJECT_PACKAGE + legacyPackage
                    )
            );
            assertFalse(
                    "Legacy package directory must be removed: "
                            + legacyPackage,
                    Files.exists(
                            productionPackage.resolve(
                                    legacyPackage.replace(
                                            '.',
                                            '/'
                                    )
                            )
                    )
            );
        }
    }

    @Test
    public void featuresUseOnlyDataRepositoryContracts()
            throws IOException {

        String featureSource = readJavaSource(
                findSourceDirectory("feature")
        );

        for (String forbiddenType
                : FORBIDDEN_FEATURE_INFRASTRUCTURE_TYPES) {
        assertFalse(
                "Feature code must not access infrastructure "
                        + "directly: " + forbiddenType,
                featureSource.contains(forbiddenType)
            );
        }

        assertFalse(
                "Feature code must not import Room or file "
                        + "infrastructure",
                featureSource.contains(
                        "import " + PROJECT_PACKAGE + "data.local."
                )
                        || featureSource.contains(
                        "import " + PROJECT_PACKAGE + "data.file."
                )
        );
    }

    @Test
    public void dataManagementViewModelOnlyCoordinatesUi()
            throws IOException {

        String viewModelSource = Files.readString(
                findExistingPath(
                        "src/main/java/com/rndymi/"
                                + "almacentracker/feature/"
                                + "data_management/common/"
                                + "DataManagementViewModel.java",
                        "app/src/main/java/com/rndymi/"
                                + "almacentracker/feature/"
                                + "data_management/common/"
                                + "DataManagementViewModel.java"
                ),
                StandardCharsets.UTF_8
        );

        for (String forbiddenType
                : FORBIDDEN_DATA_MANAGEMENT_VIEW_MODEL_TYPES) {
            assertFalse(
                    "DataManagementViewModel must delegate "
                            + "non-UI responsibility: "
                            + forbiddenType,
                    viewModelSource.contains(forbiddenType)
            );
        }
    }

    @Test
    public void activitiesDoNotConstructServices()
            throws IOException {

        Path featureDirectory =
                findSourceDirectory("feature");

        try (Stream<Path> files = javaFiles(featureDirectory)) {
            files.filter(path ->
                            path.getFileName()
                                    .toString()
                                    .endsWith("Activity.java")
                    )
                    .forEach(this::assertActivityDoesNotCompose);
        }
    }

    @Test
    public void appContainerDelegatesFeatureComposition()
            throws IOException {

        String appContainer = readFile(
                findExistingPath(
                        "src/main/java/com/rndymi/"
                                + "almacentracker/app/"
                                + "AppContainer.java",
                        "app/src/main/java/com/rndymi/"
                                + "almacentracker/app/"
                                + "AppContainer.java"
                )
        );

        assertTrue(
                "AppContainer must compose the inventory module",
                appContainer.contains("new InventoryModule(")
        );
        assertTrue(
                "AppContainer must compose the data module",
                appContainer.contains(
                        "new DataManagementModule("
                )
        );
        assertFalse(
                "Feature services belong in composition modules",
                appContainer.contains(
                        "feature.data_management."
                                + "export.ExportWarehouseItemsService"
                )
        );
    }

    @Test
    public void compositionModulesStayIndependent()
            throws IOException {

        Path dependencyInjectionDirectory =
                findExistingPath(
                        "src/main/java/com/rndymi/"
                                + "almacentracker/app/di",
                        "app/src/main/java/com/rndymi/"
                                + "almacentracker/app/di"
                );
        String inventoryModule = readFile(
                dependencyInjectionDirectory.resolve(
                        "InventoryModule.java"
                )
        );
        String dataManagementModule = readFile(
                dependencyInjectionDirectory.resolve(
                        "DataManagementModule.java"
                )
        );

        assertFalse(
                "Inventory composition must not depend on "
                        + "data management",
                inventoryModule.contains(
                        PROJECT_PACKAGE
                                + "feature.data_management."
                )
        );
        assertFalse(
                "Data management composition must not depend "
                        + "on inventory",
                dataManagementModule.contains(
                        PROJECT_PACKAGE + "feature.inventory."
                )
        );
    }

    private void assertDomainImportsAreAllowed(Path sourceFile) {
        try (Stream<String> lines = Files.lines(
                sourceFile,
                StandardCharsets.UTF_8
        )) {
            lines.map(String::trim)
                    .filter(line -> line.startsWith("import "))
                    .forEach(line -> assertTrue(
                            "Domain import is not allowed in "
                                    + sourceFile + ": " + line,
                            isAllowedDomainImport(line)
                    ));
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not inspect domain source: "
                            + sourceFile,
                    exception
            );
        }
    }

    private void assertCoreImportsAreAllowed(Path sourceFile) {
        try (Stream<String> lines = Files.lines(
                sourceFile,
                StandardCharsets.UTF_8
        )) {
            lines.map(String::trim)
                    .filter(line -> line.startsWith("import "))
                    .forEach(line -> assertTrue(
                            "Core import is not allowed in "
                                    + sourceFile + ": " + line,
                            isAllowedCoreImport(line)
                    ));
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not inspect core source: "
                            + sourceFile,
                    exception
            );
        }
    }

    private void assertActivityDoesNotCompose(Path activityFile) {
        String source = readFile(activityFile);

        assertFalse(
                "Activity must obtain dependencies from AppContainer: "
                        + activityFile,
                SERVICE_CONSTRUCTION.matcher(source).find()
        );
    }

    private String readFile(Path path) {
        try {
            return Files.readString(
                    path,
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not inspect source file: " + path,
                    exception
            );
        }
    }

    private boolean isAllowedDomainImport(String importLine) {
        String importedType = importLine
                .substring("import ".length())
                .replaceFirst("^static ", "");

        return importedType.startsWith("java.")
                || importedType.startsWith("javax.")
                || importedType.startsWith(
                        PROJECT_PACKAGE + "domain."
                );
    }

    private boolean isAllowedCoreImport(String importLine) {
        String importedType = importLine
                .substring("import ".length())
                .replaceFirst("^static ", "");

        return importedType.startsWith("java.")
                || importedType.startsWith("javax.")
                || importedType.startsWith(
                        PROJECT_PACKAGE + "core."
                )
                || importedType.startsWith(
                        PROJECT_PACKAGE + "domain."
                );
    }

    private Path findSourceDirectory(String layer) {
        return findExistingPath(
                "src/main/java/com/rndymi/almacentracker/"
                        + layer,
                "app/src/main/java/com/rndymi/almacentracker/"
                        + layer
        );
    }

    private String readJavaSource(
            Path sourceDirectory
    ) throws IOException {

        StringBuilder content = new StringBuilder();

        try (Stream<Path> files = javaFiles(sourceDirectory)) {
            files.forEach(path ->
                    appendFileContent(content, path)
            );
        }

        return content.toString();
    }

    private Stream<Path> javaFiles(
            Path sourceDirectory
    ) throws IOException {

        return Files.walk(sourceDirectory)
                .filter(Files::isRegularFile)
                .filter(path ->
                        path.toString().endsWith(".java")
                )
                .sorted();
    }

    private void appendFileContent(
            StringBuilder destination,
            Path path
    ) {
        try {
            destination.append(
                    Files.readString(
                            path,
                            StandardCharsets.UTF_8
                    )
            );
            destination.append(System.lineSeparator());
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not inspect source file: " + path,
                    exception
            );
        }
    }

    private Path findExistingPath(
            String... candidates
    ) {
        for (String candidate : candidates) {
            Path path = Path.of(candidate)
                    .toAbsolutePath()
                    .normalize();

            if (Files.exists(path)) {
                return path;
            }
        }

        throw new IllegalStateException(
                "Could not locate any expected project path: "
                        + Arrays.toString(candidates)
        );
    }
}
