package hk.ust.cse.comp3021.pa3;

import hk.ust.cse.comp3021.pa3.controller.GameController;
import hk.ust.cse.comp3021.pa3.model.*;
import hk.ust.cse.comp3021.pa3.util.GameStateSerializer;
import hk.ust.cse.comp3021.pa3.util.Robot;
import hk.ust.cse.comp3021.pa3.util.TimeIntervalGenerator;
import hk.ust.cse.comp3021.pa3.view.UIServices;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class StudentTest {
    private GameBoard gameBoard = null;
    private GameState gameState = null;
    private GameController controller = null;
    private GameState[] gameStates;



    @ParameterizedTest
    @Tag("student")
    @ValueSource(booleans = {true, false})
    @DisplayName("testMultithreading")
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
        for (var gameState : gameStates) {
            var randomDelegate = new Robot(gameStates[0], Robot.Strategy.Random);
            var smartDelegate = new Robot(gameStates[0], Robot.Strategy.Smart);
            randomDelegate.startDelegation(e -> controller.processMove(e, gameState.getPlayer().getId()));
        }

        try {
            Thread.sleep(fewerMove ? 30: 1000);
        } catch (InterruptedException e) {
            System.out.println("Failed to sleep.");
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
        for (var gameState : gameStates) {
            var randomDelegate = new Robot(gameStates[0], Robot.Strategy.Random);
            var smartDelegate = new Robot(gameStates[0], Robot.Strategy.Smart);
            randomDelegate.startDelegation(e -> controller.processMove(e, gameState.getPlayer().getId()));
        }

        try {
            Thread.sleep(fewerMove ? 30: 1000);
        } catch (InterruptedException e) {
            System.out.println("Failed to sleep.");
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

}
