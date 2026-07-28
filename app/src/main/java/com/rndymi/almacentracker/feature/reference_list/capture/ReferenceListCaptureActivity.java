package com.rndymi.almacentracker.feature.reference_list.capture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.app.AlmacenTrackerApplication;
import com.rndymi.almacentracker.core.document.DocumentImageLoader;
import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.databinding.ActivityReferenceListCaptureBinding;
import com.rndymi.almacentracker.feature.reference_list.review.ReferenceListReviewActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ReferenceListCaptureActivity
        extends AppCompatActivity {

    private static final String STATE_PENDING_CAPTURE_PATH =
            "pendingCapturePath";

    private static final int PREVIEW_MAX_SIZE = 1200;

    private ActivityReferenceListCaptureBinding binding;
    private ReferenceListCaptureViewModel viewModel;
    private DocumentImageLoader<Bitmap> imageLoader;

    private ActivityResultLauncher<Uri> takePictureLauncher;

    private ActivityResultLauncher <PickVisualMediaRequest>
            photoPickerLauncher;

    private final ExecutorService previewExecutor = Executors.newSingleThreadExecutor();

    private File pendingCaptureFile;
    private File activeCapturedFile;
    private String renderedImageUri;
    private ReferenceListCaptureUiState.Status lastAnnouncedErrorStatus;

    public static Intent createIntent(
            Context context
    ) {
        return new Intent(
                context,
                ReferenceListCaptureActivity.class
        );
    }

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding =
                ActivityReferenceListCaptureBinding
                        .inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        restorePendingCapture(savedInstanceState);
        configureToolbar();
        configureActivityResults();
        configureViewModel();
        configureActions();
        observeState();
    }

    private void configureToolbar() {
        binding.toolbar.setNavigationOnClickListener(
                ignored -> finish()
        );
    }

    private void configureActivityResults() {
        takePictureLauncher =
                registerForActivityResult(
                        new ActivityResultContracts
                                .TakePicture(),
                        this::handlePictureResult
                );

        photoPickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts
                                .PickVisualMedia(),
                        this::handleSelectedImage
                );
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication)
                        getApplication();

        ReferenceListCaptureViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideReferenceListCaptureViewModelFactory();
        imageLoader =
                application
                        .getAppContainer()
                        .provideDocumentImageLoader();

        viewModel =
                new ViewModelProvider(
                        this,
                        factory
                ).get(
                        ReferenceListCaptureViewModel.class
                );
    }

    private void configureActions() {
        binding.takePhotoButton.setOnClickListener(
                ignored -> launchCamera()
        );

        binding.selectImageButton.setOnClickListener(
                ignored -> launchPhotoPicker()
        );

        binding.processImageButton.setOnClickListener(
                ignored ->
                        viewModel.processSelectedImage()
        );

        binding.retryButton.setOnClickListener(
                ignored ->
                        viewModel.processSelectedImage()
        );

        binding.changeImageButton.setOnClickListener(
                ignored -> changeImage()
        );

        binding.cancelButton.setOnClickListener(
                ignored -> finish()
        );

        binding.reviewReferencesButton
                .setOnClickListener(
                        ignored ->
                                openReferenceReview()
                );
        binding.toggleRawTextButton
                .setOnClickListener(
                        ignored ->
                                viewModel.toggleRawText()
                );
    }

    private void observeState() {
        viewModel.getUiState().observe(
                this,
                this::render
        );
    }

    private void launchCamera() {
        ReferenceListCaptureUiState state =
                viewModel.getUiState().getValue();

        if (state != null
                && !state.canChooseImage()) {
            return;
        }

        deletePendingCapture();

        File captureFile =
                createCaptureFile();

        if (captureFile == null) {
            showTemporaryImageCreationError();
            return;
        }

        pendingCaptureFile = captureFile;

        Uri captureUri =
                FileProvider.getUriForFile(
                        this,
                        getPackageName()
                                + ".fileprovider",
                        captureFile
                );

        takePictureLauncher.launch(captureUri);
    }

    private void launchPhotoPicker() {
        ReferenceListCaptureUiState state =
                viewModel.getUiState().getValue();

        if (state != null
                && !state.canChooseImage()) {
            return;
        }

        PickVisualMediaRequest request =
                new PickVisualMediaRequest.Builder()
                        .setMediaType(
                                ActivityResultContracts
                                        .PickVisualMedia
                                        .ImageOnly.INSTANCE
                        )
                        .build();

        photoPickerLauncher.launch(request);
    }

    private void handlePictureResult(
            Boolean success
    ) {
        File completedCapture =
                pendingCaptureFile;

        pendingCaptureFile = null;

        if (!Boolean.TRUE.equals(success)
                || completedCapture == null
                || !completedCapture.isFile()
                || completedCapture.length() <= 0L) {
            deleteFile(completedCapture);
            return;
        }

        replaceActiveCapturedFile(
                completedCapture
        );

        Uri uri =
                FileProvider.getUriForFile(
                        this,
                        getPackageName()
                                + ".fileprovider",
                        completedCapture
                );

        viewModel.selectImage(
                uri.toString(),
                DocumentImageSource.CAMERA
        );
    }

    private void handleSelectedImage(
            Uri selectedUri
    ) {
        if (selectedUri == null) {
            return;
        }

        deleteActiveCapturedFile();

        if (!imageLoader.canOpen(selectedUri.toString())) {
            viewModel.selectImage(
                    selectedUri.toString(),
                    DocumentImageSource.PHOTO_PICKER
            );
            viewModel.processSelectedImage();
            return;
        }

        viewModel.selectImage(
                selectedUri.toString(),
                DocumentImageSource.PHOTO_PICKER
        );
    }

    private void changeImage() {
        deleteActiveCapturedFile();
        renderedImageUri = null;
        binding.imagePreview.setImageDrawable(null);
        viewModel.clearImage();
    }

    private void render(
            ReferenceListCaptureUiState state
    ) {
        boolean processing =
                state.isProcessing();

        binding.takePhotoButton.setEnabled(
                state.canChooseImage()
        );
        binding.selectImageButton.setEnabled(
                state.canChooseImage()
        );
        binding.cancelButton.setEnabled(
                !processing
        );

        binding.reviewReferencesButton
                .setEnabled(
                        state.shouldShowRecognizedText()
                );

        binding.previewCard.setVisibility(
                state.shouldShowPreview()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.imageSourceText.setVisibility(
                state.shouldShowPreview()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.processImageButton.setVisibility(
                state.getStatus()
                        == ReferenceListCaptureUiState
                        .Status.IMAGE_SELECTED
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.processImageButton.setEnabled(
                state.canProcessImage()
        );

        binding.processingProgress.setVisibility(
                processing
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.processingText.setVisibility(
                processing
                        ? View.VISIBLE
                        : View.GONE
        );

        renderPreview(state);
        renderError(state);
        renderRecognizedText(state);
    }

    private void renderPreview(
            ReferenceListCaptureUiState state
    ) {
        if (!state.shouldShowPreview()) {
            renderedImageUri = null;
            binding.imagePreview.setImageDrawable(null);
            return;
        }

        binding.imageSourceText.setText(
                state.getImageSource()
                        == DocumentImageSource.CAMERA
                        ? R.string
                          .reference_list_camera_source
                        : R.string
                          .reference_list_picker_source
        );

        String imageUri = state.getImageUri();

        if (imageUri == null
                || imageUri.equals(renderedImageUri)) {
            return;
        }

        renderedImageUri = imageUri;

        loadScaledPreview(
                Uri.parse(imageUri)
        );
    }

    private void loadScaledPreview(
            Uri imageUri
    ) {
        binding.imagePreview.setImageDrawable(null);

        previewExecutor.execute(
                () -> {
                    Bitmap preview =
                            imageLoader.loadPreview(
                                    imageUri.toString(),
                                    PREVIEW_MAX_SIZE
                            );

                    runOnUiThread(
                            () -> {
                                if (isFinishing()
                                        || isDestroyed()) {
                                    if (preview != null) {
                                        preview.recycle();
                                    }
                                    return;
                                }

                                ReferenceListCaptureUiState state =
                                        viewModel
                                                .getUiState()
                                                .getValue();

                                if (state == null
                                        || state.getImageUri() == null
                                        || !state.getImageUri()
                                        .equals(
                                                imageUri.toString()
                                        )) {
                                    if (preview != null) {
                                        preview.recycle();
                                    }
                                    return;
                                }

                                if (preview == null) {
                                    binding.imagePreview
                                            .setImageResource(
                                                    R.drawable
                                                            .ic_inventory_empty
                                            );
                                    return;
                                }

                                binding.imagePreview
                                        .setImageBitmap(preview);
                            }
                    );
                }
        );
    }

    private void renderError(
            ReferenceListCaptureUiState state
    ) {
        boolean hasError =
                state.getStatus()
                        == ReferenceListCaptureUiState
                        .Status.NO_TEXT_FOUND
                        || state.getStatus()
                        == ReferenceListCaptureUiState
                        .Status.IMAGE_ERROR
                        || state.getStatus()
                        == ReferenceListCaptureUiState
                        .Status.RECOGNITION_ERROR;

        binding.errorCard.setVisibility(
                hasError
                        ? View.VISIBLE
                        : View.GONE
        );

        if (!hasError) {
            lastAnnouncedErrorStatus = null;
            return;
        }

        int messageResource;

        switch (state.getStatus()) {
            case NO_TEXT_FOUND:
                messageResource =
                        R.string
                                .reference_list_no_text_error;
                break;

            case IMAGE_ERROR:
                messageResource =
                        R.string
                                .reference_list_image_open_error;
                break;

            case RECOGNITION_ERROR:
                messageResource =
                        R.string
                                .reference_list_recognition_error;
                break;

            default:
                throw new IllegalStateException(
                        "Unexpected error state: "
                                + state.getStatus()
                );
        }

        binding.errorText.setText(messageResource);

        binding.retryButton.setVisibility(
                state.canRetryRecognition()
                        ? View.VISIBLE
                        : View.GONE
        );

        if (lastAnnouncedErrorStatus
                != state.getStatus()) {
            binding.errorCard
                    .announceForAccessibility(
                            getString(messageResource)
                    );

            lastAnnouncedErrorStatus =
                    state.getStatus();
        }
    }

    private void renderRecognizedText(
            ReferenceListCaptureUiState state
    ) {
        boolean visible =
                state.shouldShowRecognizedText();

        binding.recognizedTextCard.setVisibility(
                visible
                        ? View.VISIBLE
                        : View.GONE
        );

        if (!visible) {
            binding.recognizedLinesText
                    .setText(null);

            binding.rawRecognizedLinesText
                    .setText(null);

            binding.rawRecognizedLinesText
                    .setVisibility(View.GONE);

            return;
        }

        List<String> reconstructedLines =
                state.getRecognizedDocument()
                        .getReconstructedLines();

        List<String> rawLines =
                state.getRecognizedDocument()
                        .getRawLines();

        binding.recognizedTextSummary.setText(
                getResources().getQuantityString(
                        R.plurals
                                .reference_list_reconstructed_lines,
                        reconstructedLines.size(),
                        reconstructedLines.size()
                )
        );

        binding.recognizedLinesText.setText(
                joinLines(reconstructedLines)
        );

        boolean rawTextExpanded =
                state.isRawTextExpanded();

        binding.rawRecognizedLinesText.setVisibility(
                rawTextExpanded
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.rawRecognizedLinesText.setText(
                rawTextExpanded
                        ? joinLines(rawLines)
                        : null
        );

        binding.toggleRawTextButton.setText(
                rawTextExpanded
                        ? R.string
                          .reference_list_hide_raw_text_action
                        : R.string
                          .reference_list_show_raw_text_action
        );

        binding.recognizedTextCard
                .announceForAccessibility(
                        getResources().getQuantityString(
                                R.plurals
                                        .reference_list_lines_recognized,
                                reconstructedLines.size(),
                                reconstructedLines.size()
                        )
                );
    }

    private String joinLines(
            List<String> lines
    ) {
        StringBuilder result =
                new StringBuilder();

        if (lines == null) {
            return "";
        }

        for (String line : lines) {
            if (line == null
                    || line.trim().isEmpty()) {
                continue;
            }

            if (result.length() > 0) {
                result.append('\n');
            }

            result.append(line.trim());
        }

        return result.toString();
    }

    private File createCaptureFile() {
        File directory =
                new File(
                        getCacheDir(),
                        "reference_lists"
                );

        if (!directory.exists()
                && !directory.mkdirs()) {
            return null;
        }

        File file =
                new File(
                        directory,
                        "reference-list-"
                                + UUID.randomUUID()
                                + ".jpg"
                );

        try (
                FileOutputStream ignored =
                        new FileOutputStream(file)
        ) {
            return file;
        } catch (Exception exception) {
            deleteFile(file);
            return null;
        }
    }

    private void showTemporaryImageCreationError() {
        new com.google.android.material.dialog
                .MaterialAlertDialogBuilder(this)
                .setTitle(
                        R.string
                                .reference_list_capture_error_title
                )
                .setMessage(
                        R.string
                                .reference_list_capture_file_error
                )
                .setPositiveButton(
                        android.R.string.ok,
                        null
                )
                .show();
    }

    private void replaceActiveCapturedFile(
            File newFile
    ) {
        if (activeCapturedFile != null
                && !activeCapturedFile.equals(newFile)) {
            deleteFile(activeCapturedFile);
        }

        activeCapturedFile = newFile;
    }

    private void deletePendingCapture() {
        deleteFile(pendingCaptureFile);
        pendingCaptureFile = null;
    }

    private void deleteActiveCapturedFile() {
        deleteFile(activeCapturedFile);
        activeCapturedFile = null;
    }

    private void deleteFile(
            File file
    ) {
        if (file == null
                || !file.exists()) {
            return;
        }

        // Failure is harmless; cache cleanup can remove it later.
        file.delete();
    }

    private void restorePendingCapture(
            Bundle savedInstanceState
    ) {
        if (savedInstanceState == null) {
            return;
        }

        String pendingPath =
                savedInstanceState.getString(
                        STATE_PENDING_CAPTURE_PATH
                );

        if (pendingPath != null) {
            pendingCaptureFile =
                    new File(pendingPath);
        }
    }

    private void openReferenceReview() {
        ReferenceListCaptureUiState state =
                viewModel
                        .getUiState()
                        .getValue();

        if (state == null
                || !state.shouldShowRecognizedText()
                || state.getRecognizedDocument()
                == null) {
            return;
        }

        List<String> recognizedLines =
                state.getRecognizedDocument()
                        .getReconstructedLines();

        startActivity(
                ReferenceListReviewActivity
                        .createIntent(
                                this,
                                recognizedLines
                        )
        );
    }

    @Override
    protected void onSaveInstanceState(
            Bundle outState
    ) {
        super.onSaveInstanceState(outState);

        if (pendingCaptureFile != null) {
            outState.putString(
                    STATE_PENDING_CAPTURE_PATH,
                    pendingCaptureFile
                            .getAbsolutePath()
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        previewExecutor.shutdownNow();

        if (isFinishing()) {
            deletePendingCapture();
            deleteActiveCapturedFile();
        }

        binding = null;
    }
}
