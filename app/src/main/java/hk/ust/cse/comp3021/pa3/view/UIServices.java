package hk.ust.cse.comp3021.pa3.view;

import hk.ust.cse.comp3021.pa3.InertiaFxGame;
import hk.ust.cse.comp3021.pa3.model.GameState;
import hk.ust.cse.comp3021.pa3.model.Player;
import hk.ust.cse.comp3021.pa3.util.GameStateSerializer;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for performing GUI interactions
 */
public class UIServices {

    private static final String FILE_CHOOSER_TITLE = "Load Game";

    /**
     * Creates a {@link FileChooser} for choosing a game file.
     *
     * @return The {@link FileChooser}.
     */
    @NotNull
    public static FileChooser createGameLoadFileChooser() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(FILE_CHOOSER_TITLE);
        var workingDir = getWorkingDirectory();
        fileChooser.setInitialDirectory(workingDir.toFile());
        return fileChooser;
    }

    /**
     * Gets the working directory of the current program
     *
     * @return A {@link Path} representing the current working directory.
     */
    @NotNull
    public static Path getWorkingDirectory() {
        return Paths.get("").toAbsolutePath();
    }

    /**
     * Prompts the user to choose a file and load it to the game.
     * Shows an {@link Alert} then exception occurred.
     *
     * @param game The {@link InertiaFxGame} instance.
     * @return the {@link GameState} or {@literal null} if exception occurred.
     */
    @Nullable
    public static GameState[] loadGame(@NotNull InertiaFxGame game) {
        var fileChooser = UIServices.createGameLoadFileChooser();
        var selectedFile = fileChooser.showOpenDialog(game.getPrimaryStage());
        GameState[] gameStates = null;
        if (selectedFile != null) {
            try {
                gameStates = GameStateSerializer.loadFrom(selectedFile.toPath());
            } catch (Exception ex) {
                showLoadGameErrorDialog();
            }
        } else {
            showFileNotSelectedDialog();
        }
        return gameStates;
    }

    private static final String LOAD_GAME_ERROR_ALERT_TITLE = "Can not load game";

    private static final String LOAD_GAME_ERROR_ALERT_CONTENT_TEXT
            = "Error occurred when loading the game from selected file.";

    /**
     * Shows an {@link Alert} telling that there is an error when trying to open the game file.
     */
    public static void showLoadGameErrorDialog() {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(LOAD_GAME_ERROR_ALERT_TITLE);
        alert.setContentText(LOAD_GAME_ERROR_ALERT_CONTENT_TEXT);
        alert.showAndWait();
    }

    private static final String LOAD_GAME_CANCEL_ALERT_TITLE = "Operation Cancelled";

    private static final String LOAD_GAME_CANCEL_ALERT_CONTENT_TEXT = "You didn't select a file.";

    /**
     * Shows an {@link Alert} telling that the user has cancelled loading a game file.
     */
    public static void showFileNotSelectedDialog() {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(LOAD_GAME_CANCEL_ALERT_TITLE);
        alert.setContentText(LOAD_GAME_CANCEL_ALERT_CONTENT_TEXT);
        alert.showAndWait();
    }

    public static final String WIN_ALERT_TITLE = "Congratulations";

    private static final String WIN_ALERT_CONTENT_TEXT = "You won the game!";

    /**
     * Shows an {@link Alert} telling that the user has won the game.
     */
    public static void showWinDialog(Player winner) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(WIN_ALERT_TITLE);
        alert.setContentText(String.format("Player %d: %s", winner.getId(), WIN_ALERT_CONTENT_TEXT));
        alert.showAndWait();
    }

    public static final String LOSE_ALERT_TITLE = "Oops";

    private static final String LOSE_ALERT_CONTENT_TEXT = "You lose the game.";

    /**
     * Shows an {@link Alert} telling that the user has lost the game.
     */
    public static void showLoseDialog(Player loser) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LOSE_ALERT_TITLE);
        alert.setContentText(String.format("Player %d: %s", loser.getId(), LOSE_ALERT_CONTENT_TEXT));
        alert.showAndWait();
    }
}
