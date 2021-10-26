package hk.ust.cse.comp3021.pa3.model;

/**
 * Represents a single element on the {@link GameBoard}.
 */
public interface BoardElement {

    /**
     * @return A Unicode character representing this game element on the game board.
     */
    char toUnicodeChar();

    /**
     * @return An ASCII character representing this game element on the game board.
     */
    char toASCIIChar();

    /**
     * @return Path to the image that represents the element in GUI.
     */
    String toImage();
}
