package hk.ust.cse.comp3021.pa3.view.panes;

import hk.ust.cse.comp3021.pa3.model.GameState;
import hk.ust.cse.comp3021.pa3.view.GameUIComponent;
import hk.ust.cse.comp3021.pa3.view.controls.GameCell;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;

/**
 * A {@link javafx.scene.layout.Pane} for displaying the status of {@link hk.ust.cse.comp3021.pa3.model.GameBoard}.
 */
public class GameBoardPane extends GridPane implements GameUIComponent {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeComponents() {
        this.setAlignment(Pos.CENTER);
        this.setCenterShape(true);
    }

    /**
     * Updates the game board display with latest {@link GameState}.
     *
     * @param gameStates The latest {@link GameState} instances of all players.
     */
    public void showGameState(GameState... gameStates) {
        if (gameStates.length < 1) {
            throw new IllegalArgumentException();
        }
        this.getChildren().clear();
        // since all gameStates of all players refer to the same gameBoard,
        // we can simply use the first one.
        var gameBoard = gameStates[0].getGameBoard();
        for (int x = 0; x < gameBoard.getNumRows(); x++) {
            for (int y = 0; y < gameBoard.getNumCols(); y++) {
                var cellControl = new GameCell(gameBoard.getCell(x, y));
                this.add(cellControl, y, x);
            }
        }
    }

}
