package hk.ust.cse.comp3021.pa3.controller;

import hk.ust.cse.comp3021.pa3.model.*;
import hk.ust.cse.comp3021.pa3.util.GameBoardUtils;
import hk.ust.cse.comp3021.pa3.util.ReflectionUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

// Important: Do NOT assert any GameBoard state or MoveResult in this test class!
//            All GameBoard state assertions should belong in GameBoardControllerTest.
//            This class should test GameState fields (excl. GameBoard) ONLY!
//
// All tests in this class are effectively copied from GameBoardController, with all assertions replaced with GameState
// assertions instead.
// If you add a test case to GameBoardController, also add 2 variants to this class: One for unlimited lives, and one
// for "limited" lives. More test cases may be needed for "interesting" state transitions, e.g. a player dying.
public class GameControllerTest {

    private GameBoard gameBoard = null;
    private GameState gameState = null;
    private GameController controller = null;

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Constructors")
    void testConstructors() {
        final var clazz = GameController.class;
        final var ctors = ReflectionUtils.getPublicConstructors(clazz);

        assertThat(ctors.length, Matchers.greaterThanOrEqualTo(1));

        assertDoesNotThrow(() -> clazz.getConstructor(GameState[].class));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Methods")
    void testPublicMethods() {
        final var clazz = GameController.class;
        final var publicMethods = ReflectionUtils.getPublicInstanceMethods(clazz);

        assertThat(publicMethods.length, Matchers.greaterThanOrEqualTo(9));

        assertDoesNotThrow(() -> clazz.getMethod("processMove", Direction.class));
        assertDoesNotThrow(() -> clazz.getMethod("processUndo"));
        assertDoesNotThrow(() -> clazz.getMethod("getGameState"));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Fields")
    void testPublicFields() {
        final var clazz = GameController.class;
        final var publicFields = ReflectionUtils.getPublicInstanceFields(clazz);

        assertThat(publicFields.length, Matchers.greaterThanOrEqualTo(0));
    }

    // For UP and LEFT:
    // P.*
    // ...
    // ...
    //
    // For DOWN and RIGHT:
    // ..*
    // ...
    // ..P
    @ParameterizedTest
    @Tag("sanity")
    @EnumSource(value = Direction.class)
    @DisplayName("Make Move - Move to Adjacent Border, Unlimited Lives")
    void testMakeMoveToBorderUnlimitedLives(final Direction direction) {
        final Position expectedPos;
        if (direction == Direction.UP || direction == Direction.LEFT) {
            expectedPos = new Position(0, 0);
        } else {
            expectedPos = new Position(2, 2);
        }

        gameBoard = GameBoardUtils.createGameBoard(3, 3, (pos) -> {
            if (pos.equals(expectedPos)) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 2))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives());
        assumeTrue(gameState.getNumLives() == Integer.MAX_VALUE);
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(direction);

        assumeTrue(moveResult instanceof MoveResult.Invalid);

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(0, gameState.getNumMoves());
        assertEquals(Integer.MAX_VALUE, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());
        assertTrue(gameState.getMoveStack().isEmpty());
    }

    // For UP and LEFT:
    // P.*
    // ...
    // ...
    //
    // For DOWN and RIGHT:
    // ..*
    // ...
    // ..P
    @ParameterizedTest
    @Tag("sanity")
    @EnumSource(value = Direction.class)
    @DisplayName("Make Move - Move to Adjacent Border, Limited Lives")
    void testMakeMoveToBorderLimitedLives(final Direction direction) {
        final Position expectedPos;
        if (direction == Direction.UP || direction == Direction.LEFT) {
            expectedPos = new Position(0, 0);
        } else {
            expectedPos = new Position(2, 2);
        }

        gameBoard = GameBoardUtils.createGameBoard(3, 3, (pos) -> {
            if (pos.equals(expectedPos)) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 2))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeFalse(gameState.hasUnlimitedLives());
        assumeTrue(gameState.getNumLives() == 1);
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(direction);

        assumeTrue(moveResult instanceof MoveResult.Invalid);

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(0, gameState.getNumMoves());
        assertEquals(1, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());
        assertTrue(gameState.getMoveStack().isEmpty());
    }

    // Each parameterized test will test one of four positions for W
    // *W.
    // WPW
    // .W.
    @ParameterizedTest
    @Tag("sanity")
    @EnumSource(value = Direction.class)
    @DisplayName("Make Move - Move to Adjacent Wall, Unlimited Lives")
    void testMakeMoveToAdjacentWallUnlimitedLives(final Direction direction) {
        final var wallPos = new Position(1, 1).offsetBy(direction.getOffset());

        gameBoard = GameBoardUtils.createGameBoard(3, 3, (pos) -> {
            if (pos.equals(new Position(1, 1))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(wallPos)) {
                return new Wall(pos);
            } else if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives());
        assumeTrue(gameState.getNumLives() == Integer.MAX_VALUE);
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(direction);

        assumeTrue(moveResult instanceof MoveResult.Invalid);

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(0, gameState.getNumMoves());
        assertEquals(Integer.MAX_VALUE, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());
        assertTrue(gameState.getMoveStack().isEmpty());
    }

    // Each parameterized test will test one of four positions for W
    // *W.
    // WPW
    // .W.
    @ParameterizedTest
    @Tag("sanity")
    @EnumSource(value = Direction.class)
    @DisplayName("Make Move - Move to Adjacent Wall, Limited Lives")
    void testMakeMoveToAdjacentWallLimitedLives(final Direction direction) {
        final var wallPos = new Position(1, 1).offsetBy(direction.getOffset());

        gameBoard = GameBoardUtils.createGameBoard(3, 3, (pos) -> {
            if (pos.equals(new Position(1, 1))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(wallPos)) {
                return new Wall(pos);
            } else if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeFalse(gameState.hasUnlimitedLives());
        assumeTrue(gameState.getNumLives() == 1);
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(direction);

        assumeTrue(moveResult instanceof MoveResult.Invalid);

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(0, gameState.getNumMoves());
        assertEquals(1, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());
        assertTrue(gameState.getMoveStack().isEmpty());
    }

    // P.W*
    // ....
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Make Move - Move to Wall")
    void testMakeValidMoveToWall(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 4, (pos) -> {
            if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 2))) {
                return new Wall(pos);
            } else if (pos.equals(new Position(0, 3))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 1));
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(Direction.RIGHT);

        assumeTrue(moveResult instanceof MoveResult.Valid.Alive);

        // Mutation Assertions
        assertEquals(moveResult, gameState.getMoveStack().peek());
        assertEquals(1, gameState.getNumMoves());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(1, gameState.getNumGems());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 1, gameState.getNumLives());
    }

    // P.#.
    // ...*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Make Move - Move to Stop Cell")
    void testMakeValidMoveToStopCell(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 4, (pos) -> {
            if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 2))) {
                return new StopCell(pos);
            } else if (pos.equals(new Position(1, 3))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 1));
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(Direction.RIGHT);

        assertTrue(moveResult instanceof MoveResult.Valid.Alive);

        // Mutation Assertions
        assertEquals(moveResult, gameState.getMoveStack().peek());
        assertEquals(1, gameState.getNumMoves());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(1, gameState.getNumGems());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 1, gameState.getNumLives());
    }

    // P**#
    // ...*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Make Move - Move passing Gems")
    void testMakeValidMovePassingGems(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 4, (pos) -> {
            if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 1)) || pos.equals(new Position(0, 2))) {
                return new EntityCell(pos, new Gem());
            } else if (pos.equals(new Position(0, 3))) {
                return new StopCell(pos);
            } else if (pos.equals(new Position(1, 3))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 1));
        assumeTrue(gameState.getNumGems() == 3);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(Direction.RIGHT);

        assumeTrue(moveResult instanceof MoveResult.Valid.Alive);

        // Mutation Assertions
        assertEquals(moveResult, gameState.getMoveStack().peek());
        assertEquals(1, gameState.getNumMoves());
        assertEquals(1, gameState.getNumGems());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 1, gameState.getNumLives());
    }

    // PLL#
    // ...*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Make Move - Move passing ExtraLife")
    void testMakeValidMovePassingExtraLives(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 4, (pos) -> {
            if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 1)) || pos.equals(new Position(0, 2))) {
                return new EntityCell(pos, new ExtraLife());
            } else if (pos.equals(new Position(0, 3))) {
                return new StopCell(pos);
            } else if (pos.equals(new Position(1, 3))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 1));
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(Direction.RIGHT);

        assumeTrue(moveResult instanceof MoveResult.Valid.Alive);

        // Mutation Assertions
        assertEquals(moveResult, gameState.getMoveStack().peek());
        assertEquals(1, gameState.getNumMoves());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 3, gameState.getNumLives());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(1, gameState.getNumGems());
    }

    // P.L.*.#
    // ......*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Make Move - Move passing different entities")
    void testMakeValidMovePassingEntities(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 7, (pos) -> {
            if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 2))) {
                return new EntityCell(pos, new ExtraLife());
            } else if (pos.equals(new Position(0, 4))) {
                return new EntityCell(pos, new Gem());
            } else if (pos.equals(new Position(0, 6))) {
                return new StopCell(pos);
            } else if (pos.equals(new Position(1, 6))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 1));
        assumeTrue(gameState.getNumGems() == 2);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(Direction.RIGHT);

        assumeTrue(moveResult instanceof MoveResult.Valid.Alive);

        // Mutation Assertions
        assertEquals(moveResult, gameState.getMoveStack().peek());
        assertEquals(1, gameState.getNumMoves());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 2, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumDeaths());
    }

    // P.M
    // ..*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Make Move - Move hitting Mine")
    void testMakeValidMoveToMine(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 3, (pos) -> {
            if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 2))) {
                return new EntityCell(pos, new Mine());
            } else if (pos.equals(new Position(1, 2))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 3);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 3));
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(Direction.RIGHT);

        assumeTrue(moveResult instanceof MoveResult.Valid.Dead);

        // Mutation Assertions
        assertEquals(1, gameState.getNumMoves());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 2, gameState.getNumLives());
        assertEquals(1, gameState.getNumDeaths());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertTrue(gameState.getMoveStack().isEmpty());
        assertEquals(1, gameState.getNumGems());
    }

    // P.M
    // ..*
    @Test
    @Tag("sanity")
    @DisplayName("Make Move - Move hitting Mine and Losing")
    void testMakeValidMoveToMineAndLosing() {
        gameBoard = GameBoardUtils.createGameBoard(2, 3, (pos) -> {
            if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 2))) {
                return new EntityCell(pos, new Mine());
            } else if (pos.equals(new Position(1, 2))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeFalse(gameState.hasUnlimitedLives());
        assumeTrue(gameState.getNumLives() == 1);
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(Direction.RIGHT);

        assumeTrue(moveResult instanceof MoveResult.Valid.KickedOut);

        // Mutation Assertions
        assertEquals(1, gameState.getNumMoves());
        assertEquals(0, gameState.getNumLives());
        assertEquals(1, gameState.getNumDeaths());
        assertTrue(gameState.hasLost());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertTrue(gameState.getMoveStack().isEmpty());
        assertEquals(1, gameState.getNumGems());
    }

    // PL*M
    // ..#*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Make Move - Move hitting Mine while picking up entities")
    void testMakeValidMoveToMineCrossingOtherPickUps(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 4, (pos) -> {
            if (pos.equals(new Position(0, 0))) {
                return new EntityCell(pos, new Player());
            } else if (pos.equals(new Position(0, 1))) {
                return new EntityCell(pos, new ExtraLife());
            } else if (pos.equals(new Position(0, 2))) {
                return new EntityCell(pos, new Gem());
            } else if (pos.equals(new Position(0, 3))) {
                return new EntityCell(pos, new Mine());
            } else if (pos.equals(new Position(1, 2))) {
                return new StopCell(pos);
            } else if (pos.equals(new Position(1, 3))) {
                return new EntityCell(pos, new Mine());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 3);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 3));
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(Direction.RIGHT);

        assumeTrue(moveResult instanceof MoveResult.Valid.Dead);

        // Mutation Assertions
        assertEquals(1, gameState.getNumMoves());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 2, gameState.getNumLives());
        assertEquals(1, gameState.getNumDeaths());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertTrue(gameState.getMoveStack().isEmpty());
        assertEquals(1, gameState.getNumGems());
    }

    // P*M
    // .#.
    @Test
    @Tag("sanity")
    @DisplayName("Make Move - Move hitting Mine while picking up last gem")
    void testDyingGettingLastGemOverridesWinCondition() {
        gameBoard = GameBoardUtils.createGameBoard(2, 3, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else if (pos.equals(new Position(0, 2))) {
                entity = new Mine();
            } else if (pos.equals(new Position(1, 1))) {
                return new StopCell(pos);
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });

        gameState = new GameState(gameBoard, 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeFalse(gameState.hasUnlimitedLives());
        assumeTrue(gameState.getNumLives() == 1);
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var moveResult = controller.processMove(Direction.RIGHT);

        assumeTrue(moveResult instanceof MoveResult.Valid.KickedOut);

        // Mutation Assertions
        assertEquals(1, gameState.getNumMoves());
        assertEquals(0, gameState.getNumLives());
        assertEquals(1, gameState.getNumDeaths());
        assertTrue(gameState.hasLost());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertTrue(gameState.getMoveStack().isEmpty());
        assertEquals(1, gameState.getNumGems());
    }

    // P*M
    // .#.
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Undo Move - No moves")
    void testUndoFromEmptyStack(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(1, 3, (pos) -> {
            final Entity entity;
            if (pos.equals(new Position(0, 0))) {
                entity = new Player();
            } else if (pos.equals(new Position(0, 1))) {
                entity = new Gem();
            } else if (pos.equals(new Position(0, 2))) {
                entity = new Mine();
            } else if (pos.equals(new Position(1, 1))) {
                return new StopCell(pos);
            } else {
                entity = null;
            }

            return new EntityCell(pos, entity);
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 1);
        controller = new GameController(gameState);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 0);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 1));
        assumeTrue(gameState.getNumGems() == 1);
        assumeTrue(gameState.getMoveStack().isEmpty());

        final var result = assertDoesNotThrow(() -> controller.processUndo());
        assertFalse(result);

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(0, gameState.getNumMoves());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 1, gameState.getNumLives());
        assertEquals(0, gameState.getNumDeaths());
        assertEquals(1, gameState.getNumGems());
        assertTrue(gameState.getMoveStack().isEmpty());

    }

    // Undoes the following move:
    // P.# -> ..P
    // ..*    ..*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Undo Move - Single Move")
    void testUndoMoveTrivial(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 3, (pos) -> {
            if (pos.equals(new Position(0, 2))) {
                return new StopCell(pos, new Player());
            } else if (pos.equals(new Position(1, 2))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });

        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 1);
        controller = new GameController(gameState);

        gameState.incrementNumMoves();
        final var moveToUndo = new MoveResult.Valid.Alive(
                new Position(0, 2),
                new Position(0, 0),
                Collections.emptyList(),
                Collections.emptyList()
        );
        gameState.getMoveStack().push(moveToUndo);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 1);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 1));
        assumeTrue(gameState.getNumGems() == 1);
        assumeFalse(gameState.getMoveStack().isEmpty());

        assertTrue(controller.processUndo());

        // Mutation Assertions
        assertTrue(gameState.getMoveStack().isEmpty());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 1, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());
        assertEquals(1, gameState.getNumMoves());
        assertEquals(0, gameState.getNumDeaths());
    }

    // Undoes the following move:
    // P*# -> ..P
    // ..*    ..*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Undo Move - Restores Gems")
    void testUndoMoveRestoresGems(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 3, (pos) -> {
            if (pos.equals(new Position(0, 2))) {
                return new StopCell(pos, new Player());
            } else if (pos.equals(new Position(1, 2))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });

        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 1);
        controller = new GameController(gameState);

        gameState.incrementNumMoves();
        final var moveToUndo = new MoveResult.Valid.Alive(
                new Position(0, 2),
                new Position(0, 0),
                Collections.singletonList(new Position(0, 1)),
                Collections.emptyList()
        );
        gameState.getMoveStack().push(moveToUndo);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 1);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 1));
        assumeTrue(gameState.getNumGems() == 1);
        assumeFalse(gameState.getMoveStack().isEmpty());

        assertTrue(controller.processUndo());

        // Mutation Assertions
        assertEquals(2, gameState.getNumGems());
        assertTrue(gameState.getMoveStack().isEmpty());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 1, gameState.getNumLives());
        assertEquals(1, gameState.getNumMoves());
        assertEquals(0, gameState.getNumDeaths());
    }

    // Undoes the following move:
    // PL# -> ..P
    // ..*    ..*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Undo Move - Restores ExtraLife")
    void testUndoMoveRestoresExtraLife(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 3, (pos) -> {
            if (pos.equals(new Position(0, 2))) {
                return new StopCell(pos, new Player());
            } else if (pos.equals(new Position(1, 2))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });

        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 2);
        controller = new GameController(gameState);

        gameState.incrementNumMoves();
        final var moveToUndo = new MoveResult.Valid.Alive(
                new Position(0, 2),
                new Position(0, 0),
                Collections.emptyList(),
                Collections.singletonList(new Position(0, 1))

        );
        gameState.getMoveStack().push(moveToUndo);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 1);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 2));
        assumeTrue(gameState.getNumGems() == 1);
        assumeFalse(gameState.getMoveStack().isEmpty());

        assertTrue(controller.processUndo());

        // Mutation Assertions
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 1, gameState.getNumLives());
        assertTrue(gameState.getMoveStack().isEmpty());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(1, gameState.getNumGems());
        assertEquals(1, gameState.getNumMoves());
        assertEquals(0, gameState.getNumDeaths());
    }

    // Undoes the following move:
    // P*L# -> ...P
    // ...*    ...*
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Undo Move - Restores all entity pickups")
    void testUndoMoveRestoresEntities(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(2, 4, (pos) -> {
            if (pos.equals(new Position(0, 3))) {
                return new StopCell(pos, new Player());
            } else if (pos.equals(new Position(1, 3))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 2);
        controller = new GameController(gameState);

        gameState.incrementNumMoves();
        final var moveToUndo = new MoveResult.Valid.Alive(
                new Position(0, 3),
                new Position(0, 0),
                Collections.singletonList(new Position(0, 1)),
                Collections.singletonList(new Position(0, 2))

        );
        gameState.getMoveStack().push(moveToUndo);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 1);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 2));
        assumeTrue(gameState.getNumGems() == 1);
        assumeFalse(gameState.getMoveStack().isEmpty());

        assertTrue(controller.processUndo());

        // Mutation Assertions
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 1, gameState.getNumLives());
        assertEquals(2, gameState.getNumGems());
        assertTrue(gameState.getMoveStack().isEmpty());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(1, gameState.getNumMoves());
        assertEquals(0, gameState.getNumDeaths());
    }

    // Undoes the last move of this sequence:
    // P*.    .*.    .*.    .*P
    // ... -> ... -> ... -> ...
    // ...    P..    ..P    ...
    @ParameterizedTest
    @Tag("sanity")
    @ValueSource(booleans = {true, false})
    @DisplayName("Undo Move - Undoes only one move")
    void testUndoMoveUndoesOne(final boolean hasUnlimitedLives) {
        gameBoard = GameBoardUtils.createGameBoard(3, 3, (pos) -> {
            if (pos.equals(new Position(0, 2))) {
                return new StopCell(pos, new Player());
            } else if (pos.equals(new Position(0, 1))) {
                return new EntityCell(pos, new Gem());
            } else {
                return new EntityCell(pos);
            }
        });
        gameState = new GameState(gameBoard, hasUnlimitedLives ? GameState.UNLIMITED_LIVES : 1);
        controller = new GameController(gameState);

        // Push Move-1
        gameState.incrementNumMoves();
        final var move1 = new MoveResult.Valid.Alive(
                new Position(2, 0),
                new Position(0, 0),
                Collections.emptyList(),
                Collections.emptyList()

        );
        gameState.getMoveStack().push(move1);

        // Push Move-2
        gameState.incrementNumMoves();
        final var move2 = new MoveResult.Valid.Alive(
                new Position(2, 2),
                new Position(2, 0),
                Collections.emptyList(),
                Collections.emptyList()

        );
        gameState.getMoveStack().push(move2);

        // Push Move-3
        gameState.incrementNumMoves();
        final var move3 = new MoveResult.Valid.Alive(
                new Position(0, 2),
                new Position(2, 2),
                Collections.emptyList(),
                Collections.emptyList()

        );
        gameState.getMoveStack().push(move3);

        assumeFalse(gameState.noGemsLeft());
        assumeFalse(gameState.hasLost());
        assumeTrue(gameState.getNumDeaths() == 0);
        assumeTrue(gameState.getNumMoves() == 3);
        assumeTrue(gameState.hasUnlimitedLives() == hasUnlimitedLives);
        assumeTrue(gameState.getNumLives() == (hasUnlimitedLives ? Integer.MAX_VALUE : 1));
        assumeTrue(gameState.getNumGems() == 1);
        assumeFalse(gameState.getMoveStack().isEmpty());
        assumeTrue(gameState.getMoveStack().peek().equals(move3));

        assertTrue(controller.processUndo());

        // Mutation Assertions
        assertEquals(move2, gameState.getMoveStack().peek());

        // Non-Mutation Assertions
        assertFalse(gameState.noGemsLeft());
        assertFalse(gameState.hasLost());
        assertEquals(hasUnlimitedLives ? Integer.MAX_VALUE : 1, gameState.getNumLives());
        assertEquals(1, gameState.getNumGems());
        assertEquals(3, gameState.getNumMoves());
        assertEquals(0, gameState.getNumDeaths());
    }

    @AfterEach
    void tearDown() {
        controller = null;
        gameState = null;
        gameBoard = null;
    }
}
