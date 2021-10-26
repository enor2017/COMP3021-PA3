package hk.ust.cse.comp3021.pa3.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The player entity on a game board.
 *
 * <p>
 * There should be at most one player entity on a game board.
 * </p>
 */
public final class Player extends Entity {
    private static int count = 0;
    private static final String[] PLAYER_IMAGES = new String[]{
            "/images/player.png",
            "/images/player2.png",
    };

    /**
     * The id of the player instance.
     * Each player should have unique id.
     */
    final int id;

    /**
     * The game state that this player is associated with.
     */
    @Nullable
    GameState gameState;

    final String image;

    /**
     * Creates an instance of {@link Player}, initially not present on any {@link EntityCell}.
     */
    public Player() {
        this(null, null);
    }

    public Player(@Nullable EntityCell owner, @Nullable GameState state) {
        super(owner);
        this.gameState = state;
        id = count++;
        image = PLAYER_IMAGES[id % PLAYER_IMAGES.length];
    }

    /**
     * Creates an instance of {@link Player}.
     *
     * @param owner The initial {@link EntityCell} the player belongs to.
     */
    public Player(@NotNull final EntityCell owner) {
        this(owner, null);
    }

    /**
     * Get the id of the instance.
     *
     * @return id.
     */
    public int getId() {
        return id;
    }

    /**
     * Get the game state that the player is associated with.
     *
     * @return game state
     */
    public @Nullable GameState getGameState() {
        return gameState;
    }

    /**
     * Set the game state associated with the player.
     *
     * @param gameState the game state to associate.
     */
    public void setGameState(@Nullable GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public char toUnicodeChar() {
        return '\u25EF';
    }

    @Override
    public char toASCIIChar() {
        return '@';
    }

    @Override
    public String toImage() {
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
