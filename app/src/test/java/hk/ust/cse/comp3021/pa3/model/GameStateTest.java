package hk.ust.cse.comp3021.pa3.model;

import hk.ust.cse.comp3021.pa3.util.GameBoardUtils;
import hk.ust.cse.comp3021.pa3.util.ReflectionUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class GameStateTest {

    private GameBoard gameBoard = null;
    private GameState gameState = null;

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Constructors")
    void testConstructors() {
        final var clazz = GameState.class;
        final var ctors = ReflectionUtils.getPublicConstructors(clazz);

        assertThat(ctors.length, Matchers.greaterThanOrEqualTo(4));

        assertDoesNotThrow(() -> clazz.getConstructor(GameBoard.class));
        assertDoesNotThrow(() -> clazz.getConstructor(GameBoard.class, int.class));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Methods")
    void testPublicMethods() {
        final var clazz = GameState.class;
        final var publicMethods = ReflectionUtils.getPublicInstanceMethods(clazz);

        assertThat(publicMethods.length, Matchers.greaterThanOrEqualTo(19));

        assertDoesNotThrow(() -> clazz.getDeclaredMethod("noGemsLeft"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("hasLost"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("increaseNumLives", int.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("decreaseNumLives", int.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("decrementNumLives"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("incrementNumMoves"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("incrementNumDeaths"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getNumDeaths"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getNumMoves"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("hasUnlimitedLives"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getNumLives"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getNumGems"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getScore"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getGameBoardController"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getGameBoard"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getMoveStack"));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Fields")
    void testPublicFields() {
        final var clazz = GameState.class;
        final var publicFields = ReflectionUtils.getPublicInstanceFields(clazz);

        assertThat(publicFields.length, Matchers.greaterThanOrEqualTo(0));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - Unlimited Lives using One-Arg constructor")
    void testCreationUnlimitedLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(0, gameState.getNumMoves());
        assertTrue(gameState.hasUnlimitedLives());
        assertEquals(Integer.MAX_VALUE, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());
        assertEquals(4, gameState.getScore());
        assertSame(gameBoard, gameState.getGameBoard());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - Unlimited Lives using Two-Arg constructor")
    void testNegativeNumLivesCreationIsUnlimitedLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard, GameState.UNLIMITED_LIVES);

        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(0, gameState.getNumMoves());
        assertTrue(gameState.hasUnlimitedLives());
        assertEquals(Integer.MAX_VALUE, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());
        assertEquals(4, gameState.getScore());
        assertSame(gameBoard, gameState.getGameBoard());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - Limited Lives")
    void testCreationLimitedLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard, 3);

        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(0, gameState.getNumMoves());
        assertFalse(gameState.hasUnlimitedLives());
        assertEquals(3, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());
        assertEquals(4, gameState.getScore());
        assertSame(gameBoard, gameState.getGameBoard());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Test Win Condition")
    void testWinsWhenNoGems() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        gameBoard.getEntityCell(0, 1).setEntity(null);

        assertEquals(0, gameState.getNumGems());
        assertTrue(gameState.noGemsLeft());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Test Lose Condition")
    void testLosesWhenNoLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard, 0);

        assertEquals(0, gameState.getNumLives());
        assertTrue(gameState.hasLost());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Lives Increase - Unlimited Lives")
    void testIncreaseNumLivesNoOpWhenUnlimitedLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assumeTrue(gameState.hasUnlimitedLives());

        final var newNumLives = gameState.increaseNumLives(1);
        assertEquals(Integer.MAX_VALUE, newNumLives);
        assertEquals(Integer.MAX_VALUE, gameState.getNumLives());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Lives Increase - Limited Lives")
    void testIncreaseNumLivesWhenNotUnlimitedLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard, 3);

        assumeFalse(gameState.hasUnlimitedLives());

        final var newNumLives = gameState.increaseNumLives(1);
        assertEquals(4, newNumLives);
        assertEquals(4, gameState.getNumLives());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Lives Decrease - Unlimited Lives")
    void testDecreaseNumLivesNoOpWhenUnlimitedLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assumeTrue(gameState.hasUnlimitedLives());

        final var newNumLives = gameState.decreaseNumLives(1);
        assertEquals(Integer.MAX_VALUE, newNumLives);
        assertEquals(Integer.MAX_VALUE, gameState.getNumLives());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Lives Decrease - Limited Lives")
    void testDecreaseNumLivesWhenNotUnlimitedLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard, 3);

        assumeFalse(gameState.hasUnlimitedLives());

        final var newNumLives = gameState.decreaseNumLives(1);
        assertEquals(2, newNumLives);
        assertEquals(2, gameState.getNumLives());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Lives Decrement - Unlimited Lives")
    void testDecrementNumLivesEqualToDecreaseByOneWithUnlimitedLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assumeTrue(gameState.hasUnlimitedLives());

        final var newNumLives = gameState.decrementNumLives();
        assertEquals(Integer.MAX_VALUE, newNumLives);
        assertEquals(Integer.MAX_VALUE, gameState.getNumLives());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Lives Decrement - Limited Lives")
    void testDecrementNumLivesEqualToDecreaseByOneWithLimitedLives() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard, 3);

        assumeFalse(gameState.hasUnlimitedLives());

        final var newNumLives = gameState.decrementNumLives();
        assertEquals(2, newNumLives);
        assertEquals(2, gameState.getNumLives());
        assertEquals(4, gameState.getScore());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Increment Number of Moves")
    void testIncrementNumMoves() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        final var newNumMoves = gameState.incrementNumMoves();
        assertEquals(1, newNumMoves);
        assertEquals(1, gameState.getNumMoves());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Increment Number of Deaths")
    void testIncrementNumDeaths() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        final var newNumDeaths = gameState.incrementNumDeaths();
        assertEquals(1, newNumDeaths);
        assertEquals(1, gameState.getNumDeaths());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Score - Initial Always Zero")
    void testInitialScoreAlwaysZero() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assertEquals(4, gameState.getScore());

        gameBoard = GameBoardUtils.createGameBoard(10, 10, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assertEquals(100, gameState.getScore());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Score - Score Increases When Gem Picked Up")
    void testScoreScalesWhenGemPickedUp() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else if (pos.equals(new Position(1, 0))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assumeTrue(gameState.getScore() == 4);

        gameBoard.getEntityCell(new Position(1, 0)).setEntity(null);
        gameState.increaseNumGotGems(1);

        assertEquals(14, gameState.getScore());

        gameBoard = GameBoardUtils.createGameBoard(10, 10, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else if (pos.equals(new Position(0, 2))) {
                entity = new Gem();
            } else if (pos.equals(new Position(0, 3))) {
                entity = new Gem();
            } else if (pos.equals(new Position(0, 4))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assumeTrue(gameState.getScore() == 100);

        gameBoard.getEntityCell(new Position(0, 2)).setEntity(null);
        gameState.increaseNumGotGems(1);

        assertEquals(110, gameState.getScore());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Score - Moves Deducts Points")
    void testScoreMovesDeductsPoints() {
        gameBoard = GameBoardUtils.createGameBoard(2, 2, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else if (pos.equals(new Position(1, 0))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assumeTrue(gameState.getScore() == 4);

        gameState.incrementNumMoves();

        assumeTrue(gameState.getNumMoves() == 1);

        assertEquals(3, gameState.getScore());

        gameState.incrementNumMoves();

        assumeTrue(gameState.getNumMoves() == 2);

        assertEquals(2, gameState.getScore());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Score - Death Deducts Points")
    void testScoreDeathsDeductsPoints() {
        gameBoard = GameBoardUtils.createGameBoard(10, 10, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else if (pos.equals(new Position(1, 0))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assumeTrue(gameState.getScore() == 100);

        gameState.incrementNumDeaths();

        assumeTrue(gameState.getNumDeaths() == 1);

        assertEquals(96, gameState.getScore());

        gameState.incrementNumDeaths();

        assumeTrue(gameState.getNumDeaths() == 2);

        assertEquals(92, gameState.getScore());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Score - Death Deducts Points")
    void testScoreUndoDeductsPoints() {
        gameBoard = GameBoardUtils.createGameBoard(10, 10, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else if (pos.equals(new Position(0, 2))) {
                entity = new Gem();
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard);

        assumeTrue(gameState.getScore() == 100);

        final var moveStack = gameState.getMoveStack();

        moveStack.push(new MoveResult.Valid.Alive(new Position(1, 0), new Position(2, 0)));
        moveStack.pop();

        assumeTrue(gameState.getMoveStack().isEmpty());

        assertEquals(98, gameState.getScore());

        moveStack.push(new MoveResult.Valid.Alive(new Position(1, 0), new Position(2, 0)));
        moveStack.pop();

        assertEquals(96, gameState.getScore());
    }

    @AfterEach
    void tearDown() {
        gameState = null;
        gameBoard = null;
    }
}
