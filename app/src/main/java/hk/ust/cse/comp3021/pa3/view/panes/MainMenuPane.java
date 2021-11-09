package hk.ust.cse.comp3021.pa3.view.panes;

import hk.ust.cse.comp3021.pa3.InertiaFxGame;
import hk.ust.cse.comp3021.pa3.view.GameUIComponent;
import hk.ust.cse.comp3021.pa3.view.UIServices;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * A {@link javafx.scene.layout.Pane} representing the main menu of the game.
 */
public class MainMenuPane extends VBox implements GameUIComponent {

    private final InertiaFxGame game;

    private final Label gameTitle = new Label("Inertia Game");

    private final Button startGameButton = new Button("Load Game");

    /**
     * Creates a new instances of {@link MainMenuPane}.
     *
     * @param game The {@link InertiaFxGame}.
     */
    public MainMenuPane(InertiaFxGame game) {
        this.game = game;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeComponents() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(10);
        this.gameTitle.getStyleClass().add("game-title");
        this.startGameButton.getStyleClass().add("main-menu-button");
        this.getChildren().addAll(
                gameTitle,
                startGameButton
        );
        this.startGameButton.setOnAction(this::onStartButtonClick);
    }

    /**
     * Event handler for the start game button.
     * Remember to check the total number of players or game states since in PA3 we only consider at most 2 players.
     *
     * @param e The {@link ActionEvent} for the button click.
     */
    private void onStartButtonClick(ActionEvent e) {
        var gameStates = UIServices.loadGame(game);
        if (gameStates != null) {
            if (gameStates.length > 2) {
                throw new IllegalArgumentException("only support at most 2 players");
            }
            game.showGamePane(gameStates);
        }
    }
}

