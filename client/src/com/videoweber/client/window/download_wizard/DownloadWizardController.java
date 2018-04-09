package com.videoweber.client.window.download_wizard;

import com.videoweber.client.entity.ChannelEntity;
import com.videoweber.client.entity.SampleEntity;
import com.videoweber.client.repository.SampleRepository;
import com.videoweber.client.service.HibernateService;
import com.videoweber.client.service.HybridSampleStorageService;
import com.videoweber.client.window.range_constructor.RangeConstructorWindow;
import com.videoweber.client.window.range_edit.Range;
import com.videoweber.client.window.range_edit.RangeEditWindow;
import com.videoweber.lib.app.service.window_service.WindowController;
import com.videoweber.lib.app.service.window_service.WindowService;
import com.videoweber.lib.gui.Gui;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

/**
 * FXML Controller class
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class DownloadWizardController extends WindowController {

    @FXML
    private TableView<ArrayList<Object>> channelsTableView;
    @FXML
    private TableView<ArrayList<String>> rangesTableView;
    @FXML
    private TableView<ArrayList<String>> channelsFinalTableView;
    @FXML
    private TableView<ArrayList<String>> rangesFinalTableView;
    @FXML
    private Label donwloadSizeLabel;
    @FXML
    private TabPane tabPane;
    @FXML
    private ProgressBar downloadProgressBar;
    @FXML
    private Button startDownloadButton;
    @FXML
    private Button stopDownloadButton;
    @FXML
    private Button prevDownloadButton;

    private static enum State {
        PREPARE, DOWNLOAD
    };
    private State state = State.PREPARE;

    private Thread downloadThread = null;

    private final Set<Range> ranges = new HashSet<>();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
    private final BiFunction<Range, Range, String> editRangeHandler = (Range oldRange, Range newRange) -> {
        for (Range range : ranges) {
            if (range == oldRange) {
                continue;
            }
            Date rb = range.getBegin();
            Date re = range.getEnd();
            Date nrb = newRange.getBegin();
            Date nre = newRange.getEnd();
            if (nrb.before(rb) && nre.after(rb)
                    || nrb.before(re) && nre.after(re)
                    || !nrb.before(rb) && !nre.after(re)) {
                return String.format(
                        "Интервал пересекается с одним из существующих интервалов (%s - %s).",
                        SIMPLE_DATE_FORMAT.format(range.getBegin()),
                        SIMPLE_DATE_FORMAT.format(range.getEnd())
                );
            }
        }
        ranges.add(newRange);
        if (oldRange != null) {
            ranges.remove(oldRange);
        }
        refresh();
        return null;
    };

    private final Consumer<Set<Range>> constructorRangesHandler = (Set<Range> ranges) -> {
        this.ranges.addAll(ranges);
        refresh();
    };

    @Override
    public void onWindowInitialized() {
        for (int i = 0; i < channelsTableView.getColumns().size(); i++) {
            final int colIndex = i;
            if (colIndex == 0) {
                TableColumn<ArrayList<Object>, Boolean> col = (TableColumn<ArrayList<Object>, Boolean>) channelsTableView.getColumns().get(colIndex);
                col.setCellValueFactory(colData -> {
                    SimpleBooleanProperty simpleBooleanProperty = new SimpleBooleanProperty((Boolean) colData.getValue().get(colIndex));
                    simpleBooleanProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        colData.getValue().set(colIndex, newValue);
                        refresh();
                    });
                    return simpleBooleanProperty;
                });
                col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
            } else {
                TableColumn<ArrayList<Object>, String> col = (TableColumn<ArrayList<Object>, String>) channelsTableView.getColumns().get(colIndex);
                col.setCellValueFactory(colData -> {
                    List<Object> rowValues = colData.getValue();
                    String cellValue;
                    if (colIndex < rowValues.size()) {
                        cellValue = (String) rowValues.get(colIndex);
                    } else {
                        cellValue = "";
                    }
                    return new ReadOnlyStringWrapper(cellValue);
                });
            }
        }
        for (int i = 0; i < rangesTableView.getColumns().size(); i++) {
            TableColumn<ArrayList<String>, String> col = (TableColumn<ArrayList<String>, String>) rangesTableView.getColumns().get(i);
            final int colIndex = i;
            col.setCellValueFactory(colData -> {
                List<String> rowValues = colData.getValue();
                String cellValue;
                if (colIndex < rowValues.size()) {
                    cellValue = rowValues.get(colIndex);
                } else {
                    cellValue = "";
                }
                return new ReadOnlyStringWrapper(cellValue);
            });
        }

        for (int i = 0; i < channelsFinalTableView.getColumns().size(); i++) {
            TableColumn<ArrayList<String>, String> col = (TableColumn<ArrayList<String>, String>) channelsFinalTableView.getColumns().get(i);
            final int colIndex = i;
            col.setCellValueFactory(colData -> {
                List<String> rowValues = colData.getValue();
                String cellValue;
                if (colIndex < rowValues.size()) {
                    cellValue = rowValues.get(colIndex);
                } else {
                    cellValue = "";
                }
                return new ReadOnlyStringWrapper(cellValue);
            });
        }
        for (int i = 0; i < rangesFinalTableView.getColumns().size(); i++) {
            TableColumn<ArrayList<String>, String> col = (TableColumn<ArrayList<String>, String>) rangesFinalTableView.getColumns().get(i);
            final int colIndex = i;
            col.setCellValueFactory(colData -> {
                List<String> rowValues = colData.getValue();
                String cellValue;
                if (colIndex < rowValues.size()) {
                    cellValue = rowValues.get(colIndex);
                } else {
                    cellValue = "";
                }
                return new ReadOnlyStringWrapper(cellValue);
            });
        }
        refresh();
    }

    private List<Range> getSortedRanges() {
        return ranges
                .stream()
                .sorted((Range o1, Range o2) -> o1.getBegin().compareTo(o2.getBegin()))
                .collect(Collectors.toList());
    }

    private Set<UUID> getCheckedChannels() {
        return channelsTableView
                .getItems()
                .stream()
                .filter((ArrayList<Object> row) -> (Boolean) row.get(0))
                .map((ArrayList<Object> row) -> UUID.fromString((String) row.get(3)))
                .collect(Collectors.toSet());
    }

    private void refresh() {
        Set<UUID> checkedChannels = getCheckedChannels();
        channelsTableView.getItems().clear();
        getService(HibernateService.class)
                .getRepository(ChannelEntity.class)
                .findAll()
                .forEach((ChannelEntity channelEntity) -> {
                    ArrayList<Object> row = new ArrayList<>();
                    row.add(checkedChannels.contains(channelEntity.getUuid()));
                    row.add(channelEntity.getTitle());
                    row.add(channelEntity.getPath());
                    row.add(channelEntity.getUuid().toString());
                    channelsTableView.getItems().add(row);
                });

        rangesTableView.getItems().clear();
        List<Range> sortedRanges = getSortedRanges();
        sortedRanges.forEach((Range range) -> {
            ArrayList<String> row = new ArrayList<>();
            row.add(SIMPLE_DATE_FORMAT.format(range.getBegin()));
            row.add(SIMPLE_DATE_FORMAT.format(range.getEnd()));
            rangesTableView.getItems().add(row);
        });

        channelsFinalTableView.getItems().clear();
        getService(HibernateService.class)
                .getRepository(ChannelEntity.class)
                .findAll()
                .stream()
                .filter(channelEntity -> checkedChannels.contains(channelEntity.getUuid()))
                .forEach((ChannelEntity channelEntity) -> {
                    ArrayList<String> row = new ArrayList<>();
                    row.add(channelEntity.getTitle());
                    row.add(channelEntity.getPath());
                    row.add(channelEntity.getUuid().toString());
                    channelsFinalTableView.getItems().add(row);
                });

        rangesFinalTableView.getItems().clear();
        sortedRanges.forEach((Range range) -> {
            ArrayList<String> row = new ArrayList<>();
            row.add(SIMPLE_DATE_FORMAT.format(range.getBegin()));
            row.add(SIMPLE_DATE_FORMAT.format(range.getEnd()));
            rangesFinalTableView.getItems().add(row);
        });

        donwloadSizeLabel.setText(String.valueOf(Math.round(100. * getDownloadSize() / 1024 / 1024) / 100.));

        if (state == State.PREPARE) {
            downloadProgressBar.progressProperty().set(0);
            tabPane.getTabs().forEach((tab) -> {
                tab.disableProperty().set(false);
            });
            stopDownloadButton.disableProperty().set(true);
            startDownloadButton.disableProperty().set(false);
            prevDownloadButton.disableProperty().set(false);
        } else {
            IntStream.range(0, tabPane.getTabs().size() - 1)
                    .forEach((index) -> {
                        tabPane.getTabs().get(index).disableProperty().set(true);
                    });
            stopDownloadButton.disableProperty().set(false);
            startDownloadButton.disableProperty().set(true);
            prevDownloadButton.disableProperty().set(true);
        }
    }

    private long getDownloadSize() {
        return getCheckedChannels()
                .stream()
                .map((UUID uuid) -> {
                    return getService(HibernateService.class)
                            .getRepository(ChannelEntity.class)
                            .get(uuid);
                })
                .mapToLong((ChannelEntity channelEntity) -> {
                    return getSortedRanges()
                            .stream()
                            .mapToLong((Range range) -> {
                                return ((SampleRepository) getService(HibernateService.class)
                                        .getRepository(SampleEntity.class))
                                        .getDownloadSizeByRange(channelEntity, range.getBegin(), range.getEnd());
                            })
                            .sum();
                })
                .sum();
    }

    public void deleteRangeAction() {
        if (rangesTableView.getSelectionModel().getSelectedItem() == null) {
            Alert alert = Gui.createAlert(Alert.AlertType.WARNING);
            alert.setHeaderText("Не выбран элемент для удаления");
            alert.show();
            return;
        }
        ranges.remove(
                getSortedRanges().get(
                        rangesTableView.getSelectionModel().getSelectedIndex()
                )
        );
        refresh();
    }

    public void openEditRangeAction() {
        if (rangesTableView.getSelectionModel().getSelectedItem() == null) {
            Alert alert = Gui.createAlert(Alert.AlertType.WARNING);
            alert.setHeaderText("Не выбран элемент для редактирования");
            alert.show();
            return;
        }
        Range rangeToEdit = getSortedRanges().get(
                rangesTableView.getSelectionModel().getSelectedIndex()
        );
        getService(WindowService.class)
                .openWindow(
                        RangeEditWindow.class,
                        new Object[]{editRangeHandler, rangeToEdit}
                );
    }

    public void openCreateRangeAction() {
        getService(WindowService.class)
                .openWindow(
                        RangeEditWindow.class,
                        new Object[]{editRangeHandler}
                );
    }

    public void checkAllChannelsAction() {
        channelsTableView.getItems().forEach((ArrayList<Object> row) -> {
            row.set(0, true);
        });
        refresh();
    }

    public void uncheckAllChannelsAction() {
        channelsTableView.getItems().clear();
        refresh();
    }

    public void clearRangesAction() {
        ranges.clear();
        refresh();
    }

    public void openRangeConstructorAction() {
        if (!ranges.isEmpty()) {
            Gui.createAlert(
                    Alert.AlertType.WARNING,
                    "Очистите список интервалов перед запуском конструктора"
            ).show();
            return;
        }
        getService(WindowService.class)
                .openWindow(
                        RangeConstructorWindow.class,
                        new Object[]{constructorRangesHandler}
                );
    }

    public void nextAction() {
        switch (tabPane.getSelectionModel().getSelectedIndex()) {
            case 0:
                tabPane.getSelectionModel().select(1);
                break;
            case 1:
                tabPane.getSelectionModel().select(2);
                break;
        }
    }

    public void prevAction() {
        switch (tabPane.getSelectionModel().getSelectedIndex()) {
            case 2:
                tabPane.getSelectionModel().select(1);
                break;
            case 1:
                tabPane.getSelectionModel().select(0);
                break;
        }

    }

    public void startDownloadingAction() {
        state = State.DOWNLOAD;
        downloadProgressBar.progressProperty().set(0);
        final Set<UUID> uuids = getCheckedChannels();
        final List<Range> rangesToDownload = getSortedRanges();
        downloadThread = new Thread(() -> {
            List<SampleEntity> samples = uuids
                    .stream()
                    .map((UUID uuid) -> {
                        return getService(HibernateService.class)
                                .getRepository(ChannelEntity.class)
                                .get(uuid);
                    })
                    .flatMap((ChannelEntity channelEntity) -> {
                        return rangesToDownload
                                .stream()
                                .flatMap((Range range) -> {
                                    return ((SampleRepository) getService(HibernateService.class)
                                            .getRepository(SampleEntity.class))
                                            .findUnloadedByRange(channelEntity, range.getBegin(), range.getEnd())
                                            .stream();
                                });
                    })
                    .collect(Collectors.toList());
            int samplesCount = samples.size();
            AtomicInteger samplesLoaded = new AtomicInteger(0);
            int executorsNum = 5;
            CountDownLatch countDownLatch = new CountDownLatch(executorsNum);
            ExecutorService executorService = Executors.newFixedThreadPool(executorsNum);
            for (int i = 0; i < 5; i++) {
                executorService.execute(() -> {
                    SampleEntity sampleToLoad;
                    while (!Thread.interrupted()) {
                        synchronized (samples) {
                            if (samples.isEmpty()) {
                                break;
                            }
                            sampleToLoad = samples.get(0);
                            samples.remove(0);
                        }
                        getService(HybridSampleStorageService.class)
                                .getSampleFile(sampleToLoad);
                        synchronized (samples) {
                            Platform.runLater(() -> {
                                if (executorService.isShutdown()) {
                                    return;
                                }
                                downloadProgressBar.progressProperty().set(1. * samplesLoaded.incrementAndGet() / samplesCount);
                                refresh();
                            });
                        }
                    }
                    countDownLatch.countDown();
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                executorService.shutdownNow();
                return;
            }
            Platform.runLater(() -> {
                Gui.createAlert(
                        Alert.AlertType.INFORMATION,
                        "Загрузка успешно завершена"
                ).show();
                stopDownloadingAction();
            });
        });
        refresh();
        downloadThread.start();
    }

    public void stopDownloadingAction() {
        state = State.PREPARE;
        if (downloadThread != null) {
            downloadThread.interrupt();
            downloadThread = null;
        }
        refresh();
    }

}
