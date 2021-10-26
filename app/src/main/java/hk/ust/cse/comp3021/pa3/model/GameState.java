package hk.ust.cse.comp3021.pa3.model;

import hk.ust.cse.comp3021.pa3.controller.GameBoardController;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Class for tracking the state of multiple game components.
 * The definition of {@link GameState} has been slightly changed in PA3.
 * Each instance is corresponding to a specific player.
 * That is to say, for each {@link Player}, there exists a {@link GameState} recording the state of the player.
 */
public class GameState {

    /**
     * Number representing unlimited number of lives for a player.
     */
    public static final int UNLIMITED_LIVES = -1;

    /**
     * The game board managed by this instance.
     */
    @NotNull
    private final GameBoard gameBoard;

    /**
     * The {@link Player} instance that this {@link GameState} corresponds to.
     */
    private final Player player;

    /**
     * {@link MoveStack} instance of all moves performed by the player.
     */
    @NotNull
    private final MoveStack moveStack = new MoveStack();

    /**
     * The number of deaths of the player.
     */
    private int numDeaths = 0;

    /**
     * The number of moves performed by the player (excluding invalid moves).
     */
    private int numMoves = 0;

    /**
     * The number of lives the player has.
     */
    private int numLives;

    /**
     * The number of gems that have been got by the player.
     */
    private int numGotGems = 0;

    /**
     * The number of gems initially on the game board when this instance was created.
     */
    private final int initialNumOfGems;

    /**
     * Creates an instance.
     *
     * <p>
     * The player will have an unlimited number of lives by default.
     * </p>
     *
     * @param gameBoard The game board to be managed by this instance.
     */
    public GameState(@NotNull final GameBoard gameBoard) {
        this(gameBoard, UNLIMITED_LIVES);
    }

    /**
     * Creates an instance.
     * This constructor is meant to maintain backward compatibility with PA1 and PA2.
     * Since no player is provided in this constructor, we use the {@link GameBoard#getPlayer()} to get a player
     * when there is only a single player.
     *
     * <p>
     * Note: Do NOT throw an exception when {@code numLives} is zero. This will be used in our testing.
     * </p>
     *
     * @param gameBoard The game board to be managed by this instance.
     * @param numLives  Number of lives the player has. If the value is negative, treat as if the player has an
     *                  unlimited number of lives.
     */
    public GameState(@NotNull final GameBoard gameBoard, final int numLives) {
        this(gameBoard, gameBoard.getPlayer(), numLives);
    }

    /**
     * Creates an instance
     *
     * @param gameBoard The game board to be managed by this instance.
     * @param player    The {@link Player} that this instance corresponds to.
     */
    public GameState(@NotNull final GameBoard gameBoard, Player player) {
        this(gameBoard, player, UNLIMITED_LIVES);
    }

    /**
     * Creates an instance.
     *
     * @param gameBoard The game board to be managed by this instance.
     * @param player    The {@link Player} that this instance corresponds to.
     * @param numLives  Number of lives the player has. If the value is negative, treat as if the player has an
     *                  unlimited number of lives.
     */
    public GameState(@NotNull final GameBoard gameBoard, Player player, final int numLives) {
        this.gameBoard = Objects.requireNonNull(gameBoard);
        // make sure the player is in the game board
        if (!this.gameBoard.hasPlayer(player)) {
            throw new IllegalArgumentException();
        }
        this.player = Objects.requireNonNull(player);
        this.numLives = numLives;
        player.setGameState(this);

        this.initialNumOfGems = this.gameBoard.getNumGems();
    }

    /**
     * Checks whether the game has been won.
     *
     * <p>
     * The game is won when there are no gems left in the game board.
     * </p>
     *
     * @return Whether the player has won the game.
     */
    public boolean noGemsLeft() {
        return getNumGems() == 0;
    }

    /**
     * Checks whether the game has been lost.
     *
     * <p>
     * The game is lost when the player has no lives remaining. For games which the player has unlimited lives, this
     * condition should never trigger.
     * </p>
     *
     * @return Whether the player has lost the game.
     */
    public boolean hasLost() {
        return getNumLives() == 0;
    }

    /**
     * Increases the player's number of lives by the specified amount.
     *
     * @param delta The number of lives to give the player.
     * @return The new number of lives of the player. If the player has unlimited number of lives, returns
     * {@link Integer#MAX_VALUE}.
     */
    public int increaseNumLives(final int delta) {
        if (hasUnlimitedLives()) {
            return getNumLives();
        }

        numLives += delta;
        return numLives;
    }

    /**
     * Decreases the player's number of lives by the specified amount.
     *
     * @param delta The number of lives to take from the player.
     * @return The new number of lives of the player. If the player has unlimited number of lives, returns
     * {@link Integer#MAX_VALUE}.
     */
    public int decreaseNumLives(final int delta) {
        if (hasUnlimitedLives()) {
            return getNumLives();
        }
        if (getNumLives() - delta < 0) {
            throw new RuntimeException();
        }

        numLives -= delta;
        return numLives;
    }

    /**
     * Decrements the player's number of lives by one.
     *
     * @return The new number of lives of the player. If the player has unlimited number of lives, returns
     * {@link Integer#MAX_VALUE}.
     */
    public int decrementNumLives() {
        return decreaseNumLives(1);
    }

    /**
     * Increments the number of moves taken by the player.
     *
     * @return The new number of moves taken by the player.
     */
    public int incrementNumMoves() {
        return (++numMoves);
    }

    public int increaseNumGotGems(final int delta) {
        assert delta >= 0;
        numGotGems += delta;
        return numGotGems;
    }

    /**
     * Increments the number of deaths of the player.
     *
     * @return The new number of deaths of the player.
     */
    public int incrementNumDeaths() {
        return (++numDeaths);
    }

    /**
     * @return The current number of deaths of the player.
     */
    public int getNumDeaths() {
        return numDeaths;
    }

    /**
     * @return The current number of moves taken by the player.
     */
    public int getNumMoves() {
        return numMoves;
    }

    public int getNumGotGems() {
        return numGotGems;
    }

    /**
     * @return Whether the player has unlimited lives.
     */
    public boolean hasUnlimitedLives() {
        return numLives < 0;
    }

    /**
     * @return The number of lives a player has. If the player has an unlimited number of lives, returns
     * {@link Integer#MAX_VALUE}.
     */
    public int getNumLives() {
        return hasUnlimitedLives() ? Integer.MAX_VALUE : numLives;
    }

    /**
     * @return The number of gems that is still present on the game board.
     */
    public int getNumGems() {
        return getGameBoard().getNumGems();
    }

    /**
     * <p>
     * At any point of the game, the score should be computed using the following formula:
     * </p>
     * <ul>
     * <li>The initial score of any game board is {@code gameBoardSize}.</li>
     * <li>Each gem will be worth 10 points.</li>
     * <li>Each valid move deducts one point.</li>
     * <li>Each undo deducts two points.</li>
     * <li>Each death deducts four points (on top of the one point deducted by a valid move).</li>
     * </ul>
     *
     * @return The current score of this game.
     */
    public int getScore() {
        final var gameboardSize = getGameBoard().getNumRows() * getGameBoard().getNumCols();
        final var gemAddition = getNumGotGems() * 10;
        final var moveDeduction = getNumMoves();
        final var undoDeduction = getMoveStack().getPopCount() * 2;
        final var deathDeduction = getNumDeaths() * 4;

        return gameboardSize + gemAddition - moveDeduction - undoDeduction - deathDeduction;
    }

    /**
     * @return A controller of the managed game board for mutation.
     */
    public GameBoardController getGameBoardController() {
        return new GameBoardController(getGameBoard());
    }


    /**
     * @return The instance of the managed {@link GameBoard}.
     */
    @NotNull
    public GameBoard getGameBoard() {
        return gameBoard;
    }

    /**
     * @return The instance of the managed {@link MoveStack}.
     */
    @NotNull
    public MoveStack getMoveStack() {
        return moveStack;
    }

    /**
     * Get the {@link Player} that this instance corresponds to.
     *
     * @return the {@link Player} instance.
     */
    public Player getPlayer() {
        return player;
    }
}
