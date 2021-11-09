package hk.ust.cse.comp3021.pa3.view.events;

import hk.ust.cse.comp3021.pa3.model.MoveResult;
import javafx.event.Event;
import javafx.event.EventType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Event} representing a move action in {@link hk.ust.cse.comp3021.pa3.view.panes.GameControlPane}.
 */
public class MoveEvent extends Event {

    /**
     * The result of the move.
     */
    private final MoveResult moveResult;

    /**
     * The id of the player who makes this movement.
     */
    private final Integer playerID;

    /**
     * Creates a new instance of {@link MoveEvent}.
     *
     * @param moveResult The corresponding {@link MoveResult}.
     */
    public MoveEvent(@NotNull MoveResult moveResult, int playerId) {
        super(EventType.ROOT);
        this.moveResult = moveResult;
        this.playerID = playerId;
    }

    /**
     * Gets the {@link MoveResult} of this move.
     *
     * @return The {@link MoveResult}
     */
    @NotNull
    public MoveResult getMoveResult() {
        return moveResult;
    }

    /**
     * Get the id of the player that makes this move.
     *
     * @return player id.
     */
    public Integer getPlayerID() {
        return playerID;
    }
}
