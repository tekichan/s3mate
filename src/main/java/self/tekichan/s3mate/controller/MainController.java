package self.tekichan.s3mate.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import self.tekichan.s3mate.AppMode;
import self.tekichan.s3mate.demo.DemoMetadataLoader;
import self.tekichan.s3mate.s3.MetadataItem;
import self.tekichan.s3mate.s3.S3Path;
import self.tekichan.s3mate.s3.S3PathParser;
import self.tekichan.s3mate.s3.S3Service;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI Controller for JavaFX main layout
 */
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private TextField pathField;
    @FXML private TableView<MetadataItem> metadataTable;
    @FXML private TableColumn<MetadataItem, String> fieldCol;
    @FXML private TableColumn<MetadataItem, String> valueCol;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    private final S3Service s3 = new S3Service();

    /**
     * Initialise the UI components
     */
    @FXML
    void initialize() {
        metadataTable.getSelectionModel().setCellSelectionEnabled(true);
        metadataTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        metadataTable.setOnKeyPressed(e -> {
            if (new KeyCodeCombination(
                    KeyCode.C, KeyCombination.SHORTCUT_DOWN).match(e)) {
                onCopySelectedCell();
            }
        });

        fieldCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().field()));
        valueCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().value()));
    }

    /**
     * When View Metadata button is pressed
     */
    @FXML
    void onViewMetadata() {
        try {
            clearMetadata();

            // Demo routine
            if (AppMode.DEMO) {
                metadataTable.setItems(
                        FXCollections.observableArrayList(
                                DemoMetadataLoader.load()
                        )
                );
                pathField.setText("s3://example-assets/documents/2024/report-summary.pdf");
                setStatus("Demo mode – sample metadata", false);
                return;
            }

            S3Path path = S3PathParser.parse(pathField.getText());
            var meta = s3.metadata(path);

            List<MetadataItem> items = new ArrayList<>();

            addIfPresent(items, "Bucket", path.bucket());
            addIfPresent(items, "Key", path.key());
            addIfPresent(items, "Size", human(meta.contentLength()));
            addIfPresent(items, "Content Type", meta.contentType());
            addIfPresent(items, "Last Modified", meta.lastModified());

            addIfPresent(items, "ETag", meta.eTag());
            addIfPresent(items, "Storage Class", meta.storageClassAsString());
            addIfPresent(items, "Encryption", meta.serverSideEncryptionAsString());
            addIfPresent(items, "Version ID", meta.versionId());
            addIfPresent(items, "Checksum", meta.checksumSHA256());

            meta.metadata().forEach((k, v) ->
                    items.add(new MetadataItem("x-amz-meta-" + k, v))
            );

            metadataTable.setItems(FXCollections.observableArrayList(items));
            setStatus("Metadata loaded", false);
        } catch (Exception e) {
            logger.error("Failed at viewing Metadata", e);
            setStatus(errorMessage(e), true);
        }
    }

    private void addIfPresent(List<MetadataItem> items,
                              String key,
                              Object value) {
        if (value == null) return;
        String s = value.toString().trim();
        if (s.isEmpty()) return;
        items.add(new MetadataItem(key, s));
    }

    /**
     * When Download button is pressed
     */
    @FXML
    void onDownload() {
        try {
            clearMetadata();

            // Demo routine
            if (AppMode.DEMO) {
                demoProgress();
                return;
            }

            S3Path path = S3PathParser.parse(pathField.getText());

            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(getKeyFilename(path.key()));
            chooser.setInitialDirectory(
                    new File(System.getProperty("user.home"), "Downloads")
            );

            File target = chooser.showSaveDialog(null);
            if (target == null) return;

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    s3.download(
                            path,
                            target.toPath(),
                            (read, total) -> {
                                if (total > 0) {
                                    updateProgress(read, total);
                                }
                            }
                    );
                    return null;
                }
            };

            bindProgress(task, "Downloaded successfully");
            setStatus("Downloading...", false);

            new Thread(task, "s3-download-task").start();

        } catch (Exception e) {
            logger.error("Download failed", e);
            setStatus(errorMessage(e), true);
        }
    }

    private String getKeyFilename(String s3Key) {
        return s3Key.contains("/")
                ? s3Key.substring(s3Key.lastIndexOf('/') + 1)
                : s3Key;
    }

    /**
     * When Upload button is pressed
      */
    @FXML
    void onUpload() {
        try {
            clearMetadata();

            // Demo routine
            if (AppMode.DEMO) {
                demoProgress();
                return;
            }

            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(null);
            if (file == null) return;

            S3Path path = S3PathParser.parse(pathField.getText());

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    s3.upload(
                            path,
                            file.toPath(),
                            (sent, total) -> {
                                if (total > 0) {
                                    updateProgress(sent, total);
                                }
                            }
                    );
                    return null;
                }
            };

            bindProgress(task, "Uploaded successfully");
            setStatus("Uploading...", false);

            new Thread(task, "s3-upload-task").start();

        } catch (Exception e) {
            logger.error("Upload failed", e);
            setStatus(errorMessage(e), true);
        }
    }

    /**
     * When Copy button of a selected table cell is pressed
     */
    @FXML
    private void onCopySelectedCell() {
        var selection = metadataTable.getSelectionModel().getSelectedCells();
        if (selection.isEmpty()) {
            return;
        }

        TablePosition<?, ?> pos = selection.get(0);
        Object value = pos.getTableColumn()
                .getCellObservableValue(pos.getRow())
                .getValue();

        if (value != null) {
            ClipboardContent content = new ClipboardContent();
            content.putString(value.toString());
            Clipboard.getSystemClipboard().setContent(content);

            statusLabel.setText("Copied to clipboard");
        }
    }

    private void bindProgress(Task<?> task, String successMessage) {
        progressBar.setVisible(true);
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            setStatus(successMessage, false);
        });

        task.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            setStatus(task.getException().getMessage(), true);
        });
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().setAll(error ? "status-error" : "status-ok");
    }

    private void clearMetadata() {
        metadataTable.getItems().clear();
    }

    private void demoProgress() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                for (int i = 0; i <= 100; i += 5) {
                    updateProgress(i, 100);
                    Thread.sleep(60);
                }
                return null;
            }
        };
        bindProgress(task, "Completed (demo)");
        new Thread(task).start();
    }

    private static String human(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }

    private static String errorMessage(Throwable e) {
        if (e instanceof SdkException) {
            return "Failed to access S3. Check credentials or permissions.";
        }
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            return e.getMessage();
        }
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            return e.getCause().getMessage();
        }
        return e.getClass().getSimpleName();
    }
}
