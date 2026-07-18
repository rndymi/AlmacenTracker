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
import java.util.stream.Stream;

public class OfflineArchitectureTest {

    private static final List<String> FORBIDDEN_NETWORK_PERMISSIONS =
            Arrays.asList(
                    "android.permission.INTERNET",
                    "android.permission.ACCESS_NETWORK_STATE"
            );

    private static final List<String> FORBIDDEN_REMOTE_DEPENDENCIES =
            Arrays.asList(
                    "retrofit",
                    "okhttp",
                    "volley",
                    "firebase",
                    "supabase",
                    "aws-sdk",
                    "graphql",
                    "websocket"
            );

    private static final List<String> FORBIDDEN_NETWORK_APIS =
            Arrays.asList(
                    "java.net",
                    "HttpURLConnection",
                    "URLConnection",
                    "ConnectivityManager",
                    "NetworkCapabilities",
                    "android.webkit.WebView"
            );

    @Test
    public void manifestDoesNotRequestNetworkPermissions()
            throws IOException {

        String manifest = readProjectFile(
                "src/main/AndroidManifest.xml",
                "app/src/main/AndroidManifest.xml"
        );

        for (String permission : FORBIDDEN_NETWORK_PERMISSIONS) {
            assertFalse(
                    "The offline-only application must not request "
                            + permission,
                    manifest.contains(permission)
            );
        }
    }

    @Test
    public void productionDependenciesDoNotIncludeRemoteClients()
            throws IOException {

        String appGradle = readProjectFile(
                "build.gradle",
                "app/build.gradle"
        ).toLowerCase();

        String versionCatalog = readProjectFile(
                "../gradle/libs.versions.toml",
                "gradle/libs.versions.toml"
        ).toLowerCase();

        String dependencyConfiguration =
                appGradle + System.lineSeparator() + versionCatalog;

        for (String forbiddenDependency
                : FORBIDDEN_REMOTE_DEPENDENCIES) {

            assertFalse(
                    "Remote dependency detected: "
                            + forbiddenDependency,
                    dependencyConfiguration.contains(
                            forbiddenDependency
                    )
            );
        }
    }

    @Test
    public void productionSourceDoesNotUseNetworkApis()
            throws IOException {

        Path sourceDirectory = findExistingPath(
                "src/main/java",
                "app/src/main/java"
        );

        String productionSource =
                readJavaSourceFiles(sourceDirectory);

        for (String forbiddenApi : FORBIDDEN_NETWORK_APIS) {
            assertFalse(
                    "Network API detected in production source: "
                            + forbiddenApi,
                    productionSource.contains(forbiddenApi)
            );
        }
    }

    @Test
    public void appContainerUsesRoomRepositoryAsDataSource()
            throws IOException {

        String appContainer = readProjectFile(
                "src/main/java/com/rndymi/almacentracker/"
                        + "configuration/AppContainer.java",
                "app/src/main/java/com/rndymi/almacentracker/"
                        + "configuration/AppContainer.java"
        );

        assertTrue(
                "AppContainer must construct the Room repository",
                appContainer.contains(
                        "new RoomWarehouseItemRepository("
                )
        );

        assertTrue(
                "AppContainer must create the Room database",
                appContainer.contains(
                        "Room.databaseBuilder("
                )
        );

        assertFalse(
                "AppContainer must not construct a remote repository",
                appContainer.contains(
                        "RemoteWarehouseItemRepository"
                )
        );
    }

    private String readJavaSourceFiles(
            Path sourceDirectory
    ) throws IOException {

        StringBuilder content = new StringBuilder();

        try (Stream<Path> files = Files.walk(sourceDirectory)) {
            files.filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString().endsWith(".java")
                    )
                    .sorted()
                    .forEach(path ->
                            appendFileContent(content, path)
                    );
        }

        return content.toString();
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

    private String readProjectFile(
            String... candidates
    ) throws IOException {

        Path path = findExistingPath(candidates);

        return Files.readString(
                path,
                StandardCharsets.UTF_8
        );
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