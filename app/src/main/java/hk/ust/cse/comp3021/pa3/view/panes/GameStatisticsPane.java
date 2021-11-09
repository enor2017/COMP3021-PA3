package hk.ust.cse.comp3021.pa3.view.panes;

import hk.ust.cse.comp3021.pa3.model.GameState;
import hk.ust.cse.comp3021.pa3.view.GameUIComponent;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link javafx.scene.layout.Pane} for displaying the current statistics of a player.
 * That is to say, each player has a {@link GameStatisticsPane} in the GUI.
 */
public class GameStatisticsPane extends GridPane implements GameUIComponent {

    /**
     * The {@link GameState} instance that the player owning this instance is associated with.
     * Recall that each {@link hk.ust.cse.comp3021.pa3.model.Player} instance is associated with a {@link GameState} instance.
     */
    private final GameState gameState;

    private final Label numMovesLabel = new Label();
    private final Label numUndoesLabel = new Label();
    private final Label numDeathsLabel = new Label();
    private final Label numLivesLabel = new Label();
    private final Label scoreLabel = new Label();

    /**
     * Creates an instance.
     *
     * @param gameState The game state that the player owning this instance is associated with.
     */
    GameStatisticsPane(@NotNull GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeComponents() {
        this.setPadding(new Insets(2));

        this.add(scoreLabel, 0, 0);
        this.add(numMovesLabel, 1, 0);
        this.add(numLivesLabel, 0, 1);
        this.add(numDeathsLabel, 1, 1);
        this.add(numUndoesLabel, 0, 2);

        for (int i = 0; i < 2; i++) {
            var col = new ColumnConstraints();
            col.setPercentWidth(100.0f / 2.0f);
            this.getColumnConstraints().add(col);
        }
    }

    /**
     * Updates the statistics display with latest {@link GameState}.
     */
    public void updateStatistics() {
        setNumMoves(gameState.getNumMoves());
        setNumUndoes(gameState.getMoveStack().getPopCount());
        setNumDeaths(gameState.getNumDeaths());
        setNumLives(gameState.getNumLives(), gameState.hasUnlimitedLives());
        setScore(gameState.getScore());
    }

    private void setNumMoves(int value) {
        this.numMovesLabel.setText(String.format("Move: %d", value));
    }

    private void setNumUndoes(int value) {
        this.numUndoesLabel.setText(String.format("Undoes: %d", value));
    }

    private void setNumDeaths(int value) {
        this.numDeathsLabel.setText(String.format("Deaths: %d", value));
    }

    private void setNumLives(int value, boolean hasUnlimitedLives) {
        this.numLivesLabel.setText(String.format("Lives: %s", hasUnlimitedLives ? "Unlimited" : String.valueOf(value)));
    }

    private void setScore(int value) {
        this.scoreLabel.setText(String.format("Score: %d", value));
    }

}
