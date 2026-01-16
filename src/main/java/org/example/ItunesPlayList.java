package org.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.entity.Playlist;
import org.example.entity.Song;
import org.example.repo.PlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Main JavaFX UI class for playlist and library management.
 *
 * <p>
 * This class is responsible for constructing and displaying the graphical
 * user interface, including playlist navigation, song tables, search
 * functionality, and context menus for playlist operations.
 * </p>
 *
 * <p>
 * The UI communicates with the persistence layer exclusively through
 * {@link PlaylistRepository}.
 * </p>
 */
public class ItunesPlayList {
    private static final Logger logger = LoggerFactory.getLogger(ItunesPlayList.class);
    private final PlaylistRepository pri;
    private Runnable onUpdateCallback;

    /**
     * Creates a new {@code ItunesPlayList}.
     *
     * @param playlistRepository repository used for playlist persistence operations
     */
    public ItunesPlayList(PlaylistRepository playlistRepository) {
        this.pri = playlistRepository;
    }

    /**
     * Registers a callback that is invoked when the UI state changes and
     * external components need to refresh.
     *
     * @param callback the callback to invoke on update
     */
    public void setOnUpdate(Runnable callback) {
        this.onUpdateCallback = callback;
    }

    /**
     * Triggers the registered update callback, if present.
     */
    private void refresh() {
        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }
    }

    // ---------------------------------------------------------------------
    // Data model
    // ---------------------------------------------------------------------

    /**
     * Observable list containing all playlists loaded from the database.
     */
    private final ObservableList<Playlist> allPlaylistList = FXCollections.observableArrayList();

    // ---------------------------------------------------------------------
    // UI components
    // ---------------------------------------------------------------------

    /**
     * Table displaying the songs of the selected playlist.
     */
    private final TableView<Song> songTable = new TableView<>();

    /**
     * List view displaying available playlists.
     */
    private final ListView<Playlist> sourceList = new ListView<>();

    /**
     * Text elements used in the LCD-style display at the top of the UI.
     */
    private Text lcdTitle = new Text("myTunes");
    private Text lcdArtist = new Text("Choose Library or playlist");

    /**
     * Builds and displays the complete application window.
     *
     * <p>
     * This method initializes all UI components, loads playlists asynchronously,
     * and wires event handlers for user interaction.
     * </p>
     */
    public void showLibrary() {
        Stage stage = new Stage();

        // Load playlists asynchronously to avoid blocking the JavaFX thread
        new Thread(() -> {
            try {
                List<Playlist> pls = pri.findAll();
                javafx.application.Platform.runLater(() -> {
                    allPlaylistList.setAll(pls);
                    if (!allPlaylistList.isEmpty()) {
                        sourceList.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception e) {
                logger.error("showLibrary: Failed to load playlists", e);
                javafx.application.Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR, "Failed to load playlists").showAndWait()
                );
            }
        }).start();

        BorderPane root = new BorderPane();

        // -----------------------------------------------------------------
        // Top section (controls, LCD display, search field)
        // -----------------------------------------------------------------
        HBox topPanel = new HBox(15);
        topPanel.getStyleClass().add("top-panel");
        topPanel.setPadding(new Insets(10, 15, 10, 15));
        topPanel.setAlignment(Pos.CENTER_LEFT);

        StackPane lcdDisplay = createLCDDisplay();
        HBox.setHgrow(lcdDisplay, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("itunes-search");

        // Filter songs whenever the search text changes
        searchField.textProperty()
            .addListener((obs, old, newVal) -> filterSongs(newVal));

        topPanel.getChildren().addAll(
            createRoundButton("⏮"),
            createRoundButton("▶"),
            createRoundButton("⏭"),
            lcdDisplay,
            searchField
        );

        // -----------------------------------------------------------------
        // Left section (playlist navigation)
        // -----------------------------------------------------------------
        sourceList.setItems(allPlaylistList); // Koppla data till listan
        sourceList.getStyleClass().add("source-list");
        sourceList.setPrefWidth(200);

        sourceList.setCellFactory(sl -> {
            ListCell<Playlist> cell = new ListCell<>() {
                @Override
                protected void updateItem(Playlist playlist, boolean empty) {
                    super.updateItem(playlist, empty);
                    if (empty || playlist == null) {
                        setText(null);
                        setContextMenu(null);
                    } else {
                        setText(playlist.getName());
                    }
                }
            };

            ContextMenu contextMenu = new ContextMenu();

            MenuItem renameItem = new MenuItem("Change name");
            renameItem.setOnAction(event -> {
                Playlist selected = cell.getItem();
                if (selected != null) {
                    sourceList.getSelectionModel().select(selected);
                    renameSelectedPlaylist();
                }

            });

            MenuItem deleteItem = new MenuItem("Remove");
            deleteItem.setOnAction(event -> {
                Playlist selected = cell.getItem();
                if (selected != null) {
                    sourceList.getSelectionModel().select(selected);
                    deleteSelectedPlaylist();
                }
            });

            contextMenu.getItems().addAll(renameItem, deleteItem);

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });

        // Update song table when a playlist is selected
        sourceList.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, newVal) -> {
                if (newVal != null) {
                    searchField.clear();
                    ObservableList<Song> songList
                        = FXCollections.observableArrayList(
                        newVal.getSongs().stream().toList()
                    );
                    songTable.setItems(songList);
                }
            });

        // -----------------------------------------------------------------
        // Center section (song table)
        // -----------------------------------------------------------------
        setupTable();

        // -----------------------------------------------------------------
        // Bottom section (playlist controls)
        // -----------------------------------------------------------------
        HBox bottomPanel = new HBox(10);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.getStyleClass().add("bottom-panel");

        Button btnAddList = new Button("+");
        btnAddList.getStyleClass().add("list-control-button");

        Button btnDeleteList = new Button("-");
        btnDeleteList.getStyleClass().add("list-control-button");

        Button btnMoveToPlaylist = new Button("Add song to playlist");
        Button btnRemoveSong = new Button("Remove song from playlist");

        btnAddList.setOnAction(e -> createNewPlaylist());
        btnDeleteList.setOnAction(e -> deleteSelectedPlaylist());
        btnRemoveSong.setOnAction(e -> removeSelectedSong());
        btnMoveToPlaylist.setOnAction(e -> addSelectedSong(btnMoveToPlaylist));

        bottomPanel.getChildren().addAll(
            btnAddList,
            btnDeleteList,
            new Separator(),
            btnMoveToPlaylist,
            btnRemoveSong);

        // -----------------------------------------------------------------
        // Final layout assembly
        // -----------------------------------------------------------------
        SplitPane splitPane = new SplitPane(sourceList, songTable);
        splitPane.setDividerPositions(0.25);

        root.setTop(topPanel);
        root.setCenter(splitPane);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 950, 600);
        var cssResource = getClass().getResource("/ipod_style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            logger.warn("Stylesheet /ipod_style.css not found");
        }

        stage.setScene(scene);
        stage.setTitle("myTunes");
        stage.show();
    }

    /**
     * Creates the LCD-style display used in the top panel.
     *
     * @return a {@link StackPane} containing the LCD display
     */
    private StackPane createLCDDisplay() {
        StackPane stack = new StackPane();
        Rectangle bg = new Rectangle(350, 45);
        bg.getStyleClass().add("lcd-background");

        VBox textStack = new VBox(2);
        textStack.setAlignment(Pos.CENTER);

        lcdTitle.getStyleClass().add("lcd-title");
        lcdArtist.getStyleClass().add("lcd-artist");

        textStack.getChildren().addAll(lcdTitle, lcdArtist);
        stack.getChildren().addAll(bg, textStack);

        return stack;
    }

    /**
     * Creates a standardized round button with the given icon text.
     *
     * @param icon the text or symbol to display on the button
     * @return a styled {@link Button}
     */
    private Button createRoundButton(String icon) {
        Button b = new Button(icon);
        b.getStyleClass().add("itunes-button");
        return b;
    }

    /**
     * Configures the song table columns, selection behavior, and context menus.
     */
    private void setupTable() {
        TableColumn<Song, String> titleCol = new TableColumn<>("Title");

        titleCol.setCellValueFactory(d -> {
            Song s = d.getValue();
            if (s.getName() != null) {
                return new SimpleStringProperty(s.getName());
            }
            return new SimpleStringProperty("Unknown title");
        });

        TableColumn<Song, String> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(d -> {
            Song s = d.getValue();
            if (s.getAlbum() != null && s.getAlbum().getArtist() != null && s.getAlbum().getArtist().getName() != null) {
                return new SimpleStringProperty(s.getAlbum().getArtist().getName());
            }
            return new SimpleStringProperty("Unknown artist");
        });

        TableColumn<Song, String> albumCol = new TableColumn<>("Album");
        albumCol.setCellValueFactory(d -> {
            Song s = d.getValue();
            if (s.getAlbum() != null && s.getAlbum().getName() != null) {
                return new SimpleStringProperty(s.getAlbum().getName());
            }
            return new SimpleStringProperty("Unknown album");
        });

        TableColumn<Song, String> timeCol = new TableColumn<>("Length");
        timeCol.setCellValueFactory(d -> {
            Song s = d.getValue();
            if (s.getFormattedLength() != null) {
                return new SimpleStringProperty(s.getFormattedLength());
            }
            return new SimpleStringProperty("Unknown length");
        });

        songTable.getColumns().setAll(titleCol, artistCol, albumCol, timeCol);
        songTable.getStyleClass().add("song-table");

        songTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Update LCD display when clicking on a row in a table
        songTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                lcdTitle.setText(newVal.getName());
                String artistName = "Unknown artist";
                if (newVal.getAlbum() != null && newVal.getAlbum().getArtist() != null && newVal.getAlbum().getArtist().getName() != null) {
                    artistName = newVal.getAlbum().getArtist().getName();
                }
                lcdArtist.setText(artistName);
            }
        });

        // Right click function, to add song to playlist and remove song from playlist
        songTable.setRowFactory(songTableView -> {
            TableRow<Song> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            row.setOnContextMenuRequested(e -> {
                if (!row.isEmpty()) {
                    songTableView.getSelectionModel().select(row.getIndex());
                }
            });

            Menu addSongSubMenu = new Menu("Add to playlist");
            MenuItem removeSongItem = new MenuItem("Remove from playlist");

            removeSongItem.setOnAction(e -> {
                removeSelectedSong();
            });

            // Update when showing ContextMenu
            contextMenu.setOnShowing(event -> {
                addSongSubMenu.getItems().clear();
                Song selectedSong = row.getItem();

                if (selectedSong != null && !allPlaylistList.isEmpty()) {
                    for (Playlist pl : allPlaylistList) {
                        if (pl.getId() != null && pl.getId().equals(1L)) continue;

                        MenuItem playListItem = new MenuItem(pl.getName());
                        playListItem.setOnAction(e -> {
                            try {
                                if (!pri.isSongInPlaylist(pl, selectedSong)) {
                                    pri.addSong(pl, selectedSong);
                                    pl.getSongs().add(selectedSong);
                                }
                            } catch (IllegalStateException ex) {
                                logger.error("setupTable: add song failed", ex);
                                new Alert(Alert.AlertType.ERROR, "Failed to add song: " + ex.getMessage()).showAndWait();
                            }
                        });
                        addSongSubMenu.getItems().add(playListItem);
                    }
                }

                if (addSongSubMenu.getItems().isEmpty()) {
                    MenuItem emptyItem = new MenuItem("No playlists available");
                    emptyItem.setDisable(true);
                    addSongSubMenu.getItems().add(emptyItem);
                }

                Playlist currentList = sourceList.getSelectionModel().getSelectedItem();
                removeSongItem.setVisible(currentList != null && currentList.getId() != null && !currentList.getId().equals(1L));
            });

            contextMenu.getItems().addAll(addSongSubMenu, new SeparatorMenuItem(), removeSongItem);

            row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(contextMenu);
                }
            });
            return row;
        });
    }

    /**
     * Filters the songs of the currently selected playlist
     * based on the provided search text.
     *
     * @param searchText the text used for filtering
     */
    private void filterSongs(String searchText) {
        Playlist selectedPlaylist = sourceList.getSelectionModel().getSelectedItem();
        if (selectedPlaylist == null) return;

        ObservableList<Song> masterData = FXCollections.observableArrayList(selectedPlaylist.getSongs());

        if (searchText == null || searchText.isEmpty()) {
            songTable.setItems(masterData);
            return;
        }

        FilteredList<Song> filteredData = new FilteredList<>(masterData, song -> {
            String filter = searchText.toLowerCase();
            boolean titleMatch = song.getName() != null && song.getName().toLowerCase().contains(filter);
            boolean artistMatch = song.getAlbum() != null &&
                song.getAlbum().getArtist() != null &&
                song.getAlbum().getArtist().getName() != null &&
                song.getAlbum().getArtist().getName().toLowerCase().contains(filter);
            boolean albumMatch = song.getAlbum() != null &&
                song.getAlbum().getName() != null &&
                song.getAlbum().getName().toLowerCase().contains(filter);
            return titleMatch || artistMatch || albumMatch;
        });

        songTable.setItems(filteredData);
    }

    /**
     * Displays a dialog allowing the user to create a new playlist.
     */
    private void createNewPlaylist() {
        TextInputDialog d = new TextInputDialog("New playlist");
        d.setTitle("Create new playlist");
        d.setHeaderText("Enter playlist name");
        d.setContentText("Name:");

        d.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Playlist pl = pri.createPlaylist(name);
                allPlaylistList.add(pl);
            }
            refresh();
        });
    }

    /**
     * Renames the currently selected playlist.
     *
     * <p>
     * System playlists such as the main library and favorites cannot be renamed.
     * </p>
     */
    private void renameSelectedPlaylist() {
        Playlist sel = sourceList.getSelectionModel().getSelectedItem();

        if (sel == null || sel.getId() == null || sel.getId().equals(1L) || sel.getId().equals(2L)) {
            return;
        }

        TextInputDialog d = new TextInputDialog("Rename");
        d.setTitle("Rename playlist");
        d.setHeaderText("Rename playlist");
        d.setContentText("New name:");

        d.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                try {
                    pri.renamePlaylist(sel, newName);
                    sel.setName(newName);
                    sourceList.refresh();
                } catch (IllegalStateException ex) {
                    logger.error("renameSelectedPlaylist: failed to rename ", ex);
                    new Alert(Alert.AlertType.ERROR, "Failed to rename: " + ex.getMessage()).showAndWait();
                }
            }
            refresh();
        });
    }

    /**
     * Deletes the currently selected playlist.
     *
     * <p>
     * System playlists (e.g. Library and Favorites) cannot be deleted.
     * </p>
     */
    private void deleteSelectedPlaylist() {
        Playlist sel = sourceList.getSelectionModel().getSelectedItem();
        if (sel != null && sel.getId() != null && !sel.getId().equals(1L) && !sel.getId().equals(2L)) {
            try {
                pri.deletePlaylist(sel);
                allPlaylistList.remove(sel);
                refresh();
            } catch (Exception ex) {
                logger.error("deleteSelectedPlaylist: failed to delete", ex);
                new Alert(Alert.AlertType.ERROR, "Failed to delete playlist: " + ex.getMessage()).showAndWait();
            }
        }
    }

    /**
     * Removes the selected song from the currently active playlist.
     *
     * <p>
     * Songs cannot be removed directly from the main library.
     * </p>
     */
    private void removeSelectedSong() {
        Song sel = songTable.getSelectionModel().getSelectedItem();
        Playlist list = sourceList.getSelectionModel().getSelectedItem();

        // You cannot remove song from Library
        if (sel != null && list != null && list.getId() != null && !list.getId().equals(1L)) {
            try {
                pri.removeSong(list, sel);
                list.getSongs().remove(sel);
                songTable.getItems().remove(sel);
                refresh();
            } catch (Exception ex) {
                logger.error("removeSelectedSong: failed to remove", ex);
                new Alert(Alert.AlertType.ERROR, "Failed to remove song: " + ex.getMessage()).showAndWait();
            }
        }
    }

    /**
     * Displays a context menu allowing the user to add the selected song
     * to another playlist.
     *
     * @param anchor the UI element used as the menu anchor
     */
    private void addSelectedSong(Button anchor) {
        Song sel = songTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        ContextMenu menu = new ContextMenu();
        for (Playlist pl : allPlaylistList) {
            if (pl.getId() != null && pl.getId().equals(1L))
                continue; // You cannot add song to Library

            MenuItem itm = new MenuItem(pl.getName());
            itm.setOnAction(e -> {
                if (!pri.isSongInPlaylist(pl, sel)) {
                    try {
                        pri.addSong(pl, sel);
                        pl.getSongs().add(sel);
                        refresh();
                    } catch (IllegalStateException ex) {
                        logger.error("addSelectedSong: failed to add song", ex);
                        new Alert(Alert.AlertType.ERROR, "Could not add song: " + ex.getMessage()).showAndWait();
                    }
                }
            });
            menu.getItems().add(itm);
        }

        var bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        menu.show(anchor, bounds.getMinX(), bounds.getMaxY());
    }
}
