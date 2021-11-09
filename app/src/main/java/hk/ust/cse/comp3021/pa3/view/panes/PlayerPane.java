package hk.ust.cse.comp3021.pa3.view.panes;

import hk.ust.cse.comp3021.pa3.controller.GameController;
import hk.ust.cse.comp3021.pa3.model.GameState;
import hk.ust.cse.comp3021.pa3.model.Player;
import hk.ust.cse.comp3021.pa3.util.Robot;
import hk.ust.cse.comp3021.pa3.view.GameUIComponent;
import hk.ust.cse.comp3021.pa3.view.events.MoveEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;


/**
 * Represents a pane corresponding to a player in the GUI.
 * It contains the {@link GameControlPane} and {@link GameStatisticsPane} instances corresponding to the player.
 */
public class PlayerPane extends VBox implements GameUIComponent {
    /**
     * The id of the player.
     */
    private final Label playerID;

    /**
     * A toggle button that controls whether to delegate the player control to a {@link Robot} instance.
     */
    private final ToggleButton robotButton;
    private final GameControlPane controlPane;
    private final GameStatisticsPane statisticsPane;
    private final Label playerStatus;

    private EventHandler<MoveEvent> moveHandler;

    /**
     * Creates an instance.
     *
     * @param gameController The game controller that is shared among all {@link PlayerPane} instance.
     * @param gameState      The game state that belongs to the player corresponding to this instance.
     * @param allowUndo      Whether the player is allowed to undo movements.
     */
    PlayerPane(GameController gameController, GameState gameState, boolean allowUndo) {
        super();
        this.playerID = new Label(String.format("Player %d", gameState.getPlayer().getId()));
        this.playerStatus = new Label("Status: Alive");
        this.controlPane = new GameControlPane(gameController, gameState.getPlayer(), allowUndo);
        this.robotButton = new ToggleButton("Robot Disabled");
        this.statisticsPane = new GameStatisticsPane(gameState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeComponents() {
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(playerID, playerStatus, controlPane, robotButton, statisticsPane);
        controlPane.initializeComponents();
        controlPane.setOnMove(this::gameMoveHandler);
        robotButton.setOnAction(this::robotButtonAction);
        statisticsPane.initializeComponents();
        statisticsPane.updateStatistics();
    }

    /**
     * The event handler for the {@link PlayerPane#robotButton}.
     *
     * @param e event
     */
    private void robotButtonAction(Event e) {
        if (robotButton.isSelected()) {
            robotButton.setText("Robot Enabled");
            controlPane.delegateControl(new Robot(getGameState()));
        } else {
            controlPane.revokeControl();
            robotButton.setText("Robot Disabled");
        }
    }

    /**
     * Mark the player as lost.
     * Disable the {@link PlayerPane#controlPane} and {@link PlayerPane#robotButton}.
     */
    public void kickOut() {
        // disable move buttons
        controlPane.disable();
        this.robotButton.setDisable(true);
        playerStatus.setText("Status: Lost");
    }

    private void gameMoveHandler(MoveEvent e) {
        statisticsPane.updateStatistics();
        if (moveHandler != null) {
            moveHandler.handle(e);
        }
    }

    public void setOnMove(EventHandler<MoveEvent> handler) {
        moveHandler = handler;
    }

    /**
     * Get the player that corresponds to this instance.
     *
     * @return the player.
     */
    public Player getPlayer() {
        return controlPane.getPlayer();
    }

    /**
     * Get the game state that is associated with the player who corresponds to this instance.
     *
     * @return the game state.
     */
    public GameState getGameState() {
        return controlPane.getGameState();
    }

    /**
     * Stop the delegation to the {@link Robot} instance if there is any.
     */
    public void stopRobot() {
        this.robotButton.setSelected(false);
        this.robotButton.setText("Robot Disabled");
        this.controlPane.revokeControl();
    }
}
