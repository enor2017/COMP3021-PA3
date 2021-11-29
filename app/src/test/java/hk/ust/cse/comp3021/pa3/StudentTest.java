package hk.ust.cse.comp3021.pa3;

import hk.ust.cse.comp3021.pa3.controller.GameController;
import hk.ust.cse.comp3021.pa3.model.*;
import hk.ust.cse.comp3021.pa3.util.GameStateSerializer;
import hk.ust.cse.comp3021.pa3.util.Robot;
import hk.ust.cse.comp3021.pa3.util.TimeIntervalGenerator;
import hk.ust.cse.comp3021.pa3.view.UIServices;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class StudentTest {
    private GameController controller = null;
    private GameState[] gameStates;

    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testWinner-simple")
    public void testWinnerSimple(final boolean unlimitedLive) throws FileNotFoundException {
        Path puzzle = null;
        if (unlimitedLive) {
            puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/051-extra-life-unlimitedLife.multiplayer.game");
        } else {
            puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/05-extra-life.multiplayer.game");
        }
        gameStates = GameStateSerializer.loadFrom(puzzle);
        controller = new GameController(gameStates);

        var winners = controller.getWinners();
        assertNull(winners);
        
        int id1 = -1, id2 = -1;
        for (var state : gameStates) {
            if (id1 == -1) {
                id1 = state.getPlayer().getId();
            } else {
                id2 = state.getPlayer().getId();
            }
        }

        // move to this
//        S.S.L
//        0MGM.
//        W1SG.
//        GMGM.
//        ..S.L
        controller.processMove(Direction.DOWN, id1);
        controller.processMove(Direction.LEFT, id2);
        // let player 0 die
        controller.processMove(Direction.RIGHT, id1);
        assertTrue(unlimitedLive != gameStates[0].hasLost());
        assertFalse(gameStates[1].hasLost());
        winners = controller.getWinners();
        assertNull(winners);    // game not over
        // let player 1 die
        controller.processMove(Direction.DOWN, id2);
        assertTrue(unlimitedLive != gameStates[0].hasLost());
        assertTrue(unlimitedLive != gameStates[1].hasLost());
        winners = controller.getWinners();
        if (unlimitedLive) {
            assertNull(winners);    // game not over
        } else {
            assertNotNull(winners, "All players dead, game should be ended.");
            assertEquals(0, winners.length, "There should be no winner since both are dead.");
            return;
        }

        // let unlimited life finish the game
        controller.processMove(Direction.RIGHT, id2);
        controller.processMove(Direction.RIGHT, id2);
        controller.processMove(Direction.LEFT, id2);
        controller.processMove(Direction.UP, id2);
        controller.processMove(Direction.DOWN, id2);
        controller.processMove(Direction.DOWN, id2);
        controller.processMove(Direction.DOWN, id2);
        controller.processMove(Direction.LEFT, id2);
        // now
//        S.S.L
//        0M.M.
//        W.S..
//        GM.M.
//        1.S.L
        winners = controller.getWinners();
        assertNull(winners);    // game not over
        // collect last gem
        controller.processMove(Direction.UP, id2);
        winners = controller.getWinners();
        assertNotNull(winners, "Game ends since all gems collected");
        assertEquals(1, winners.length);
        assertEquals(id2, winners[0].getId());
    }

    @Test
    @Tag("student")
    @DisplayName("testWinner-Tie")
    public void testWinnerTie() throws FileNotFoundException {
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/10-test-tie.multiplayer.game");
        gameStates = GameStateSerializer.loadFrom(puzzle);
        controller = new GameController(gameStates);

        var winners = controller.getWinners();
        assertNull(winners);

        // let both player collect gem
        int id1 = -1, id2 = -1;
        for (var state : gameStates) {
            if (id1 == -1) {
                id1 = state.getPlayer().getId();
            } else {
                id2 = state.getPlayer().getId();
            }
        }
        controller.processMove(Direction.DOWN, id1);
        winners = controller.getWinners();
        assertNull(winners);    // game not over
        controller.processMove(Direction.DOWN, id2);
        winners = controller.getWinners();
        assertNotNull(winners, "Game should end since all gems collected.");
        assertEquals(2, winners.length, "Both players should win.");
    }


    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testMultithreading-smallMap")
    public void testMultithreading(final boolean fewerMove) throws FileNotFoundException {
//        System.out.println(UIServices.getWorkingDirectory() + "/../puzzles/05-extra-life.multiplayer.game");
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/05-extra-life.multiplayer.game");
        gameStates = GameStateSerializer.loadFrom(puzzle);
        controller = new GameController(gameStates);

        // get original gems num
        var originalGemNum = gameStates[0].getGameBoard().getNumGems();

        // set time to very fast
        Robot.timeIntervalGenerator = TimeIntervalGenerator.expectedMilliseconds(5);

        // start robot delegation
        List<Robot> robots = new ArrayList<>();
        for (var gameState : gameStates) {
            var randomDelegate = new Robot(gameState, Robot.Strategy.Random);
            robots.add(randomDelegate);
            randomDelegate.startDelegation(e -> controller.processMove(e, gameState.getPlayer().getId()));
        }

        try {
            Thread.sleep(fewerMove ? 30: 1000);
        } catch (InterruptedException e) {
            System.out.println("Failed to sleep.");
        }

        for (var r : robots) {
            r.stopDelegation();
        }

//        for (var gameState : gameStates) {
//            System.out.println("player pos: " + gameState.getPlayer().getOwner().getPosition());
//        }

        // check if players move
        for (var gameState : gameStates) {
            assertNotEquals(0, gameState.getNumMoves(), "The player didn't move.");
        }


        // check gems num
        var finalGems = 0;
        for (var gameState : gameStates) {
            finalGems += gameState.getNumGotGems();
        }

//        assertNotEquals(0, finalGems, "How can players got 0 gems in total?");
        assertEquals(originalGemNum, finalGems + gameStates[0].getNumGems(),
                "should have " + originalGemNum + "gems, but after moving, " + finalGems + " gems" +
                        "were collected by players in total, and " + gameStates[0].getNumGems() + " gems left on board.");

        // check extra life
        // this puzzle only have 2 extra life, meaning that
        // (total_num_death) <= (original_num_life) + (total_extra_life) = 4
        // (total_num_life_left) <= (original_num_life) + (total_extra_life) = 4
        var totalNumDeath = 0;
        var totalNumLifeLeft = 0;
        for (var gameState : gameStates) {
            totalNumDeath += gameState.getNumDeaths();
            totalNumLifeLeft += gameState.getNumLives();
        }
        assertTrue(totalNumDeath <= 4, "total num death more than 4");
        assertTrue(totalNumLifeLeft <= 4, "total num life after moving more than 4");

        // check winner and loser
        var winners = controller.getWinners();
        if (winners == null) {
            assertNotEquals(0, gameStates[0].getNumGems());
        } else {
            if (winners.length == 0) {
                assertTrue(gameStates[0].hasLost() && gameStates[1].hasLost());
            } else if (winners.length == 2) {
                assertTrue(!gameStates[0].hasLost() && !gameStates[1].hasLost() && gameStates[0].getScore() == gameStates[1].getScore());
            } else if (winners[0] == gameStates[0].getPlayer()) {
                assertTrue(gameStates[1].hasLost() || gameStates[0].getScore() > gameStates[1].getScore());
            } else {
                assertTrue(gameStates[0].hasLost() || gameStates[0].getScore() < gameStates[1].getScore());
            }
        }
    }

    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testMultithreading-threePlayers")
    public void testMultithreadingThreePlayers(final boolean fewerMove) throws FileNotFoundException {
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/11-test-three-players.multiplayer.game");
        gameStates = GameStateSerializer.loadFrom(puzzle);
        controller = new GameController(gameStates);

        // get original gems num
        var originalGemNum = gameStates[0].getGameBoard().getNumGems();

        // set time to very fast
        Robot.timeIntervalGenerator = TimeIntervalGenerator.expectedMilliseconds(5);

        // start robot delegation
        List<Robot> robots = new ArrayList<>();
        for (var gameState : gameStates) {
            var randomDelegate = new Robot(gameState, Robot.Strategy.Random);
            robots.add(randomDelegate);
            randomDelegate.startDelegation(e -> controller.processMove(e, gameState.getPlayer().getId()));
        }

        try {
            Thread.sleep(fewerMove ? 30: 1000);
        } catch (InterruptedException e) {
            System.out.println("Failed to sleep.");
        }

        for (var robot : robots) {
            robot.stopDelegation();
        }

//        for (var gameState : gameStates) {
//            System.out.println("player pos: " + gameState.getPlayer().getOwner().getPosition());
//        }

        // check if players move
        for (var gameState : gameStates) {
            assertNotEquals(0, gameState.getNumMoves(), "The player didn't move.");
        }


        // check gems num
        var finalGems = 0;
        for (var gameState : gameStates) {
            finalGems += gameState.getNumGotGems();
        }

//        assertNotEquals(0, finalGems, "How can players got 0 gems in total?");
        assertEquals(originalGemNum, finalGems + gameStates[0].getNumGems(),
                "should have " + originalGemNum + "gems, but after moving, " + finalGems + " gems" +
                        "were collected by players in total, and " + gameStates[0].getNumGems() + " gems left on board.");

        // check extra life
        // this puzzle only have 1 extra life, meaning that
        // (total_num_death) <= (original_num_life) + (total_extra_life) = 4
        // (total_num_life_left) <= (original_num_life) + (total_extra_life) = 4
        var totalNumDeath = 0;
        var totalNumLifeLeft = 0;
        for (var gameState : gameStates) {
            totalNumDeath += gameState.getNumDeaths();
            totalNumLifeLeft += gameState.getNumLives();
        }
        assertTrue(totalNumDeath <= 4, "total num death more than 4");
        assertTrue(totalNumLifeLeft <= 4, "total num life after moving more than 4");

        // check winner and loser
        var winners = controller.getWinners();
        if (winners == null) {
            assertNotEquals(0, gameStates[0].getNumGems());
        } else {
            int numDeath = 0;
            for (var gameState : gameStates) {
                if (gameState.hasLost()) {
                    numDeath++;
                }
            }
            if (numDeath == 3) {
                assertEquals(0, winners.length);
            } else if (numDeath == 2) {
                assertEquals(1, winners.length);
                assertFalse(winners[0].getGameState().hasLost(), "The winner you got is dead.");
            } else if (numDeath == 0 || numDeath == 1) {
                int maxScore = -Integer.MAX_VALUE;
                for (var state : gameStates) {
                    if (!state.hasLost() && state.getScore() > maxScore) {
                        maxScore = state.getScore();
                    }
                }
                Set<Player> actualWinner = new HashSet<>();
                for (var state : gameStates) {
                    if (!state.hasLost() && state.getScore() == maxScore) {
                        actualWinner.add(state.getPlayer());
                    }
                }

                for (var state : gameStates) {
                    System.out.println("Lost : " + state.hasLost() + ", id: " + state.getPlayer().getId() + ", score: " + state.getScore());
                }
                for (var w : actualWinner) {
                    System.out.println("actual winner: " + w.getId());
                }
                for (var w : winners) {
                    System.out.println("winner: " + w.getId());
                    assertTrue(actualWinner.contains(w));
                }
            }
        }
    }


    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testMultithreading-largeMap")
    public void testMultithreadingLarge(final boolean fewerMove) throws FileNotFoundException {
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/07-test-multithread.game");
        gameStates = GameStateSerializer.loadFrom(puzzle);
        controller = new GameController(gameStates);

        // get original gems num
        var originalGemNum = gameStates[0].getGameBoard().getNumGems();

        // set time to very fast
        Robot.timeIntervalGenerator = TimeIntervalGenerator.expectedMilliseconds(5);

        // start robot delegation
        List<Robot> robots = new ArrayList<>();
        for (var gameState : gameStates) {
            var randomDelegate = new Robot(gameState, Robot.Strategy.Random);
            robots.add(randomDelegate);
            randomDelegate.startDelegation(e -> controller.processMove(e, gameState.getPlayer().getId()));
        }

        try {
            Thread.sleep(fewerMove ? 30: 1000);
        } catch (InterruptedException e) {
            System.out.println("Failed to sleep.");
        }

        for (var r : robots) {
            r.stopDelegation();
        }


        // check if players move
        for (var gameState : gameStates) {
            assertNotEquals(0, gameState.getNumMoves(), "The player didn't move.");
        }


        // check gems num
        var finalGems = 0;
        for (var gameState : gameStates) {
            finalGems += gameState.getNumGotGems();
        }

        assertEquals(originalGemNum, finalGems + gameStates[0].getNumGems(),
                "should have " + originalGemNum + "gems, but after moving, " + finalGems + " gems" +
                        "were collected by players in total, and " + gameStates[0].getNumGems() + " gems left on board.");

        // check extra life
        // this puzzle only have 3 extra life, meaning that
        // (total_num_death) <= (original_num_life) + (total_extra_life) = 5
        // (total_num_life_left) <= (original_num_life) + (total_extra_life) = 5
        var totalNumDeath = 0;
        var totalNumLifeLeft = 0;
        for (var gameState : gameStates) {
            totalNumDeath += gameState.getNumDeaths();
            totalNumLifeLeft += gameState.getNumLives();
        }
        assertTrue(totalNumDeath <= 5, "total num death more than 5");
        assertTrue(totalNumLifeLeft <= 5, "total num life after moving more than 5");

        // check winner and loser
        var winners = controller.getWinners();
        if (winners == null) {
            assertNotEquals(0, gameStates[0].getNumGems());
        } else {
            if (winners.length == 0) {
                assertTrue(gameStates[0].hasLost() && gameStates[1].hasLost());
            } else if (winners.length == 2) {
                assertTrue(!gameStates[0].hasLost() && !gameStates[1].hasLost() && gameStates[0].getScore() == gameStates[1].getScore());
            } else if (winners[0] == gameStates[0].getPlayer()) {
                assertTrue(gameStates[1].hasLost() || gameStates[0].getScore() > gameStates[1].getScore());
            } else {
                assertTrue(gameStates[0].hasLost() || gameStates[0].getScore() < gameStates[1].getScore());
            }
        }
    }

    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testMultithreading-largeMapWithSmartRobot")
    public void testMultithreadingLargeSmart(final boolean fewerMove) throws FileNotFoundException {
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/07-test-multithread.game");
        gameStates = GameStateSerializer.loadFrom(puzzle);
        controller = new GameController(gameStates);

        // get original gems num
        var originalGemNum = gameStates[0].getGameBoard().getNumGems();

        // set time to very fast
        Robot.timeIntervalGenerator = TimeIntervalGenerator.expectedMilliseconds(4);

        // start robot delegation
        var randomDelegate = new Robot(gameStates[0], Robot.Strategy.Random);
        var smartDelegate = new Robot(gameStates[1], Robot.Strategy.Smart);
        randomDelegate.startDelegation(e -> controller.processMove(e, gameStates[0].getPlayer().getId()));
        smartDelegate.startDelegation(e -> controller.processMove(e, gameStates[1].getPlayer().getId()));


        try {
            Thread.sleep(fewerMove ? 30: 1000);
        } catch (InterruptedException e) {
            System.out.println("Failed to sleep.");
        }

        randomDelegate.stopDelegation();
        smartDelegate.stopDelegation();


        // check if players move
        for (var gameState : gameStates) {
            assertNotEquals(0, gameState.getNumMoves(), "The player didn't move.");
        }


        // check gems num
        var finalGems = 0;
        for (var gameState : gameStates) {
            finalGems += gameState.getNumGotGems();
        }

        assertEquals(originalGemNum, finalGems + gameStates[0].getNumGems(),
                "should have " + originalGemNum + "gems, but after moving, " + finalGems + " gems" +
                        "were collected by players in total, and " + gameStates[0].getNumGems() + " gems left on board.");

        // check extra life
        // this puzzle only have 3 extra life, meaning that
        // (total_num_death) <= (original_num_life) + (total_extra_life) = 5
        // (total_num_life_left) <= (original_num_life) + (total_extra_life) = 5
        var totalNumDeath = 0;
        var totalNumLifeLeft = 0;
        for (var gameState : gameStates) {
            totalNumDeath += gameState.getNumDeaths();
            totalNumLifeLeft += gameState.getNumLives();
        }
        assertTrue(totalNumDeath <= 5, "total num death more than 5");
        assertTrue(totalNumLifeLeft <= 5, "total num life after moving more than 5");

        // check winner and loser
        var winners = controller.getWinners();
        if (winners == null) {
            assertNotEquals(0, gameStates[0].getNumGems());
        } else {
            if (winners.length == 0) {
                assertTrue(gameStates[0].hasLost() && gameStates[1].hasLost());
            } else if (winners.length == 2) {
                assertTrue(!gameStates[0].hasLost() && !gameStates[1].hasLost() && gameStates[0].getScore() == gameStates[1].getScore());
            } else if (winners[0] == gameStates[0].getPlayer()) {
                assertTrue(gameStates[1].hasLost() || gameStates[0].getScore() > gameStates[1].getScore());
            } else {
                assertTrue(gameStates[0].hasLost() || gameStates[0].getScore() < gameStates[1].getScore());
            }
        }
    }

    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testMultithreading-manyGemsMapWithSmartRobot")
    public void testMultithreadingManyGemSmart(final boolean fewerMove) throws FileNotFoundException {
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/08-test-robot.game");
        gameStates = GameStateSerializer.loadFrom(puzzle);
        controller = new GameController(gameStates);

        // get original gems num
        var originalGemNum = gameStates[0].getGameBoard().getNumGems();

        // set time to very fast
        Robot.timeIntervalGenerator = TimeIntervalGenerator.expectedMilliseconds(4);

        // start robot delegation
        var randomDelegate = new Robot(gameStates[0], Robot.Strategy.Random);
        var smartDelegate = new Robot(gameStates[1], Robot.Strategy.Smart);
        randomDelegate.startDelegation(e -> controller.processMove(e, gameStates[0].getPlayer().getId()));
        smartDelegate.startDelegation(e -> controller.processMove(e, gameStates[1].getPlayer().getId()));


        try {
            Thread.sleep(fewerMove ? 30: 1000);
        } catch (InterruptedException e) {
            System.out.println("Failed to sleep.");
        }

        randomDelegate.stopDelegation();
        smartDelegate.stopDelegation();

        // check if players move
        for (var gameState : gameStates) {
            assertNotEquals(0, gameState.getNumMoves(), "The player didn't move.");
        }


        // check gems num
        var finalGems = 0;
        for (var gameState : gameStates) {
            finalGems += gameState.getNumGotGems();
        }

        assertEquals(originalGemNum, finalGems + gameStates[0].getNumGems(),
                "should have " + originalGemNum + "gems, but after moving, " + finalGems + " gems" +
                        "were collected by players in total, and " + gameStates[0].getNumGems() + " gems left on board.");

        // check extra life
        // this puzzle only have 1 extra life, meaning that
        // (total_num_death) <= (original_num_life) + (total_extra_life) = 3
        // (total_num_life_left) <= (original_num_life) + (total_extra_life) = 3
        var totalNumDeath = 0;
        var totalNumLifeLeft = 0;
        for (var gameState : gameStates) {
            totalNumDeath += gameState.getNumDeaths();
            totalNumLifeLeft += gameState.getNumLives();
        }
        assertTrue(totalNumDeath <= 3, "total num death more than 3");
        assertTrue(totalNumLifeLeft <= 3, "total num life after moving more than 3");

        // check winner and loser
        var winners = controller.getWinners();
        if (winners == null) {
            assertNotEquals(0, gameStates[0].getNumGems());
        } else {
            if (winners.length == 0) {
                assertTrue(gameStates[0].hasLost() && gameStates[1].hasLost());
            } else if (winners.length == 2) {
                assertTrue(!gameStates[0].hasLost() && !gameStates[1].hasLost() && gameStates[0].getScore() == gameStates[1].getScore());
            } else if (winners[0] == gameStates[0].getPlayer()) {
                assertTrue(gameStates[1].hasLost() || gameStates[0].getScore() > gameStates[1].getScore());
            } else {
                assertTrue(gameStates[0].hasLost() || gameStates[0].getScore() < gameStates[1].getScore());
            }
        }
    }


    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testRobot-smallMap")
    @Timeout(10)
    public void testRobotSmallMap(final boolean playerZeroIsSmart) throws FileNotFoundException {
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/05-extra-life.multiplayer.game");
        // set time to normal
        Robot.timeIntervalGenerator = TimeIntervalGenerator.expectedMilliseconds(10);

        int winCount = 0;
        // run 10 times
        for (int $ = 0; $ < 10; ++$) {
            gameStates = GameStateSerializer.loadFrom(puzzle);
            controller = new GameController(gameStates);

            // start robot delegation
            var randomDelegate = new Robot(gameStates[playerZeroIsSmart ? 1 : 0], Robot.Strategy.Random);
            var smartDelegate = new Robot(gameStates[playerZeroIsSmart ? 0 : 1], Robot.Strategy.Smart);
            randomDelegate.startDelegation(e -> controller.processMove(e, gameStates[playerZeroIsSmart ? 1 : 0].getPlayer().getId()));
            smartDelegate.startDelegation(e -> controller.processMove(e, gameStates[playerZeroIsSmart ? 0 : 1].getPlayer().getId()));


            // check winner and loser
            while (true) {
                var winners = controller.getWinners();
                if (winners == null || winners.length == 0) {
                    continue;
                }
                randomDelegate.stopDelegation();
                smartDelegate.stopDelegation();
                if (winners.length == 2) {
                    // if drawn, run another time.
                    System.out.println("Warning: Draw!");
                    $--;
                } else if (winners[0] == gameStates[playerZeroIsSmart ? 0 : 1].getPlayer()) {
                    winCount++;
                }
                break;
            }
        }
        assertTrue(winCount >= 6, "only win " + winCount + " times out of 10.");
        System.out.println("Great! Win " + winCount + " out of 10.");
    }


    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testRobot-manyGemsMap")
    @Timeout(10)
    public void testRobotGemMap(final boolean playerZeroIsSmart) throws FileNotFoundException {
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/08-test-robot.game");
        // set time to normal
        Robot.timeIntervalGenerator = TimeIntervalGenerator.expectedMilliseconds(10);

        int winCount = 0;
        // run 10 times
        for (int $ = 0; $ < 10; ++$) {
            gameStates = GameStateSerializer.loadFrom(puzzle);
            controller = new GameController(gameStates);

            // start robot delegation
            var randomDelegate = new Robot(gameStates[playerZeroIsSmart ? 1 : 0], Robot.Strategy.Random);
            var smartDelegate = new Robot(gameStates[playerZeroIsSmart ? 0 : 1], Robot.Strategy.Smart);
            randomDelegate.startDelegation(e -> controller.processMove(e, gameStates[playerZeroIsSmart ? 1 : 0].getPlayer().getId()));
            smartDelegate.startDelegation(e -> controller.processMove(e, gameStates[playerZeroIsSmart ? 0 : 1].getPlayer().getId()));


            // check winner and loser
            while (true) {
                var winners = controller.getWinners();
                if (winners == null || winners.length == 0) {
                    continue;
                }
                randomDelegate.stopDelegation();
                smartDelegate.stopDelegation();
                if (winners.length == 2) {
                    // if drawn, run another time.
                    System.out.println("Warning: Draw!");
                    $--;
                } else if (winners[0] == gameStates[playerZeroIsSmart ? 0 : 1].getPlayer()) {
                    winCount++;
                }
                break;
            }
        }
        assertTrue(winCount >= 6, "only win " + winCount + " times out of 10.");
        System.out.println("Great! Win " + winCount + " out of 10.");
    }

    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true/*, false*/})  // false is impossible to win!
    @DisplayName("testRobot-manyGemsTrickyMap")
    @Timeout(10)
    public void testRobotGemTrickyMap(final boolean playerZeroIsSmart) throws FileNotFoundException {
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/09-test-robot-tricky.game");
        // set time to normal
        Robot.timeIntervalGenerator = TimeIntervalGenerator.expectedMilliseconds(10);

        int winCount = 0;
        // run 10 times
        for (int $ = 0; $ < 10; ++$) {
            gameStates = GameStateSerializer.loadFrom(puzzle);
            controller = new GameController(gameStates);

            // start robot delegation
            var randomDelegate = new Robot(gameStates[playerZeroIsSmart ? 1 : 0], Robot.Strategy.Random);
            var smartDelegate = new Robot(gameStates[playerZeroIsSmart ? 0 : 1], Robot.Strategy.Smart);
            randomDelegate.startDelegation(e -> controller.processMove(e, gameStates[playerZeroIsSmart ? 1 : 0].getPlayer().getId()));
            smartDelegate.startDelegation(e -> controller.processMove(e, gameStates[playerZeroIsSmart ? 0 : 1].getPlayer().getId()));


            // check winner and loser
            while (true) {
                var winners = controller.getWinners();
                if (winners == null || winners.length == 0) {
                    continue;
                }
                randomDelegate.stopDelegation();
                smartDelegate.stopDelegation();
                if (winners.length == 2) {
                    // if drawn, run another time.
                    System.out.println("Warning: Draw!");
                    $--;
                } else if (winners[0] == gameStates[playerZeroIsSmart ? 0 : 1].getPlayer()) {
                    winCount++;
                }
                break;
            }
        }
        assertTrue(winCount >= 6, "only win " + winCount + " times out of 10.");
        System.out.println("Great! Win " + winCount + " out of 10.");
    }



    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testRobot-largeMap")
    @Timeout(15)
    public void testRobotLargeMap(final boolean playerZeroIsSmart) throws FileNotFoundException {
        Path puzzle = Paths.get(UIServices.getWorkingDirectory() + "/../puzzles/07-test-multithread.game");
        // set time to normal
        Robot.timeIntervalGenerator = TimeIntervalGenerator.expectedMilliseconds(10);

        int winCount = 0;
        // run 10 times
        for (int $ = 0; $ < 10; ++$) {
            gameStates = GameStateSerializer.loadFrom(puzzle);
            controller = new GameController(gameStates);

            // start robot delegation
            var randomDelegate = new Robot(gameStates[playerZeroIsSmart ? 1 : 0], Robot.Strategy.Random);
            var smartDelegate = new Robot(gameStates[playerZeroIsSmart ? 0 : 1], Robot.Strategy.Smart);
            randomDelegate.startDelegation(e -> controller.processMove(e, gameStates[playerZeroIsSmart ? 1 : 0].getPlayer().getId()));
            smartDelegate.startDelegation(e -> controller.processMove(e, gameStates[playerZeroIsSmart ? 0 : 1].getPlayer().getId()));


            // check winner and loser
            while (true) {
                var winners = controller.getWinners();
                if (winners == null || winners.length == 0) {
                    continue;
                }
                randomDelegate.stopDelegation();
                smartDelegate.stopDelegation();
                if (winners.length == 2) {
                    // if drawn, run another time.
                    System.out.println("Warning: Draw!");
                    $--;
                } else if (winners[0] == gameStates[playerZeroIsSmart ? 0 : 1].getPlayer()) {
                    winCount++;
                }
                break;
            }
        }
        assertTrue(winCount >= 6, "only win " + winCount + " times out of 10.");
        System.out.println("Great! Win " + winCount + " out of 10.");
    }
}
