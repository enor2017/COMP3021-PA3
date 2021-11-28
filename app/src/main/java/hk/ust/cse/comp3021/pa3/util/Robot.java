package hk.ust.cse.comp3021.pa3.util;

import hk.ust.cse.comp3021.pa3.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * The Robot is an automated worker that can delegate the movement control of a player.
 * <p>
 * It implements the {@link MoveDelegate} interface and
 * is used by {@link hk.ust.cse.comp3021.pa3.view.panes.GameControlPane#delegateControl(MoveDelegate)}.
 */
public class Robot implements MoveDelegate {
    public enum Strategy {
        Random, Smart
    }

    /**
     * All robots should share one lock, to makeMove
     */
    private static Lock lock = new ReentrantLock();

    /**
     * A generator to get the time interval before the robot makes the next move.
     */
    public static Generator<Long> timeIntervalGenerator = TimeIntervalGenerator.everySecond();

    /**
     * e.printStackTrace();
     * The game state of thee.printStackTrace(); player that the robot delegates.
     */
    private final GameState gameState;

    /**
     * The strategy of this instance of robot.
     */
    private final Strategy strategy;

    public Robot(GameState gameState) {
        this(gameState, Strategy.Random);
    }

    public Robot(GameState gameState, Strategy strategy) {
        this.strategy = strategy;
        this.gameState = gameState;
    }

    /**
     * An ArrayList to store all current threads of this robot
     */
    private final ArrayList<Thread> threads = new ArrayList<>();
    /**
     * An ArrayList to store all tasks corresponding to above threads
     */
    private final ArrayList<StoppableTask> tasks = new ArrayList<>();

    private class StoppableTask implements Runnable{
        /**
         * Use a flag to determine whether the thread should continue or not
         */
        private final AtomicBoolean running = new AtomicBoolean(true);

        private MoveProcessor processor;

        /**
         * Set the processor of current task
         */
        public void setProcessor(MoveProcessor processor) {
            this.processor = processor;
        }

        /**
         * Terminate the thread, by setting flag to false
         */
        public void endThread() {
            running.set(false);
        }
        @Override
        public void run() {
            while (running.get()) {
                try {
                    Thread.sleep(timeIntervalGenerator.next());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (strategy == Strategy.Random) {
                    makeMoveRandomly(processor);
                } else {
                    makeMoveSmartly(processor);
                }
            }
        }
    }

    /**
     * DONE Start the delegation in a new thread.
     * The delegation should run in a separate thread.
     * This method should return immediately when the thread is started.
     * <p>
     * In the delegation of the control of the player,
     * the time interval between moves should be obtained from {@link Robot#timeIntervalGenerator}.
     * That is to say, the new thread should:
     * <ol>
     *   <li>Stop all existing threads by calling {@link Robot#stopDelegation()}</li>
     *   <li>Wait for some time (obtained from {@link TimeIntervalGenerator#next()}</li>
     *   <li>Make a move, call {@link Robot#makeMoveRandomly(MoveProcessor)} or
     *   {@link Robot#makeMoveSmartly(MoveProcessor)} according to {@link Robot#strategy}</li>
     *   <li>goto 2</li>
     * </ol>
     * The thread should be able to exit when {@link Robot#stopDelegation()} is called.
     * <p>
     *
     * @param processor The processor to make movements.
     */
    @Override
    public void startDelegation(@NotNull MoveProcessor processor) {
        var task = new StoppableTask();
        task.setProcessor(processor);
        var thread = new Thread(task);
        stopDelegation();
        thread.start();
        threads.add(thread);
        tasks.add(task);
    }

    /**
     * DONE Stop the delegations, i.e., stop the thread of this instance.
     * When this method returns, the thread must have exited already.
     */
    @Override
    public void stopDelegation() {
        for (var t : tasks) {
            t.endThread();
        }
        for (var th : threads) {
            while (th.getState() != Thread.State.TERMINATED) {}
//            System.out.println("out!");
        }
        tasks.clear();
        threads.clear();
    }

    private MoveResult tryMove(Direction direction) {
        var player = gameState.getPlayer();
        if (player.getOwner() == null) {
            return null;
        }
        var r = gameState.getGameBoardController().tryMove(player.getOwner().getPosition(), direction, player.getId());
        return r;
    }

    /**
     * The robot moves randomly but rationally,
     * which means the robot will not move to a direction that will make the player die if there are other choices,
     * but for other non-dying directions, the robot just randomly chooses one.
     * If there is no choice but only have one dying direction to move, the robot will still choose it.
     * If there is no valid direction, i.e. can neither die nor move, the robot do not perform a move.
     * <p>
     * DONE modify this method if you need to do thread synchronization.
     *
     * @param processor The processor to make movements.
     */
    private void makeMoveRandomly(MoveProcessor processor) {
        lock.lock();
        var directions = new ArrayList<>(Arrays.asList(Direction.values()));
        Collections.shuffle(directions);
        Direction aliveDirection = null;
        Direction deadDirection = null;
        for (var direction :
                directions) {
            var result = tryMove(direction);
            if (result instanceof MoveResult.Valid.Alive) {
                aliveDirection = direction;
            } else if (result instanceof MoveResult.Valid.Dead) {
                deadDirection = direction;
            }
        }
        if (aliveDirection != null) {
            processor.move(aliveDirection);
        } else if (deadDirection != null) {
            processor.move(deadDirection);
        }
        lock.unlock();
    }


/*    private int evaluateState(GameState state) {
        if (state.hasLost()) {
            return -100;
        } *//*else if (state.noGemsLeft()) {
            return Integer.MAX_VALUE;
        }*//* else {
            return state.getScore();
        }
    }

    private GameState[] getGameStates() {
        List <GameState> states = new ArrayList<>();
        var players = gameState.getGameBoard().getPlayers();
        players.forEach(p -> states.add(p.getGameState()));
        return states.toArray(new GameState[0]);
    }

    private int switchPlayer(int agentIndex) {
        var players = gameState.getGameBoard().getPlayers();
        for (var player : players) {
            if (player.getId() != agentIndex) {
                return player.getId();
            }
        }
        return -1;
    }

    private double expValue(GameState[] states, int depth, int maxDepth, int agentIndex) {
        double expVal = 0;
        int numValidMoves = 0;
        var directions = new ArrayList<>(Arrays.asList(Direction.values()));
        for (var dire : directions) {
            var player = states[agentIndex % 2].getPlayer();
            if (player.getOwner() == null) {
                throw new IllegalArgumentException("player's owner is null in expValue()");
            }
            var moveResult = states[agentIndex % 2].getGameBoardController()
                    .tryMove(player.getOwner().getPosition(), dire, player.getId());
            // be sure the move is valid and alive
            if (!(moveResult instanceof final MoveResult.Valid.Alive aliveState)) {
                continue;
            }
            numValidMoves++;    // increase num valid moves

            states[agentIndex % 2].getGameBoardController().makeMove(dire, agentIndex);
            // update max value
//            System.out.println("exp chooses: " + dire);

            states[agentIndex % 2].increaseNumGotGems(aliveState.collectedGems.size()); // add collected gem nums

            double thisVal = getValue(states, depth + 1, maxDepth, switchPlayer(agentIndex));
            expVal += thisVal;
//            System.out.println("in exp value, depth = " + depth + ", dire = " + dire + ", val = " + thisVal);

            // undo the move
            var gameBoard = states[agentIndex % 2].getGameBoard();
            gameBoard.getEntityCell(aliveState.origPosition).setEntity(states[agentIndex % 2].getPlayer());
            states[agentIndex % 2].increaseNumGotGems(-aliveState.collectedGems.size());
//            System.out.println("decreasing agent: " + agentIndex + " gem by " + aliveState.collectedGems.size());


            for (@NotNull final var gemPos : aliveState.collectedGems) {
                gameBoard.getEntityCell(gemPos).setEntity(new Gem());
            }
            for (@NotNull final var extraLifePos : aliveState.collectedExtraLives) {
                gameBoard.getEntityCell(extraLifePos).setEntity(new ExtraLife());
            }
        }
        if (numValidMoves == 0) {
//            System.out.println("depth: " + depth + ", value: -inf, player: " + agentIndex);
            return -100;
        } else {
//            System.out.println("depth: " + depth + ", value: " + expVal / numValidMoves + ", player: " + agentIndex);
            return expVal / numValidMoves;
        }
    }

    private double maxValue(GameState[] states, int depth, int maxDepth, int agentIndex) {
        double maxVal = -Integer.MAX_VALUE;
        var directions = new ArrayList<>(Arrays.asList(Direction.values()));
        for (var dire : directions) {
            var player = states[agentIndex % 2].getPlayer();
            if (player.getOwner() == null) {
                throw new IllegalArgumentException("player's owner is null in maxValue()");
            }
            var moveResult = states[agentIndex % 2].getGameBoardController()
                    .tryMove(player.getOwner().getPosition(), dire, player.getId());
            // be sure the move is valid and alive
            if (!(moveResult instanceof final MoveResult.Valid.Alive aliveState)) {
                continue;
            }

            states[agentIndex % 2].getGameBoardController().makeMove(dire,agentIndex);
            // update max value
//            System.out.println("max chooses: " + dire);

            states[agentIndex % 2].increaseNumGotGems(aliveState.collectedGems.size()); // add collected gem nums

            double thisVal = getValue(states, depth + 1, maxDepth, switchPlayer(agentIndex));
            maxVal = Double.max(maxVal, thisVal);
//            System.out.println("in max value, depth = " + depth + ", dire = " + dire + ", val = " + thisVal);

            // undo the move
            var gameBoard = states[agentIndex % 2].getGameBoard();
            gameBoard.getEntityCell(aliveState.origPosition).setEntity(states[agentIndex % 2].getPlayer());
//            System.out.println("decreasing agent: " + agentIndex + " gem by " + aliveState.collectedGems.size());
            states[agentIndex % 2].increaseNumGotGems(-aliveState.collectedGems.size());

            for (@NotNull final var gemPos : aliveState.collectedGems) {
                gameBoard.getEntityCell(gemPos).setEntity(new Gem());
            }
            for (@NotNull final var extraLifePos : aliveState.collectedExtraLives) {
                gameBoard.getEntityCell(extraLifePos).setEntity(new ExtraLife());
            }
        }
//        System.out.println("depth: " + depth + ", value: " + maxVal + ", player: " + agentIndex);
        return maxVal == -Integer.MAX_VALUE ? -100 : maxVal;
    }

    private double getValue(GameState[] states, int depth, int maxDepth, int agentIndex) {
        *//*System.out.println("==========");
        var board = states[0].getGameBoard();
        int numRow = board.getNumRows();
        int numCol = board.getNumCols();
        for (int i = 0; i < numRow; ++i) {
            for (int j = 0; j < numCol; ++j) {
                System.out.print(board.getCell(i, j).toASCIIChar() + " ");
            }
            System.out.println();
        }
        System.out.println("==========");*//*
        if (depth == maxDepth || states[agentIndex % 2].hasLost() || states[agentIndex % 2].noGemsLeft()) {
            return evaluateState(gameState);
        }

        if (agentIndex == gameState.getPlayer().getId()) {
            return maxValue(states, depth, maxDepth, agentIndex);
        } else {
            return expValue(states, depth, maxDepth, agentIndex);
        }
    }


    private void makeMoveSmartly(MoveProcessor processor) {
        lock.lock();
        var directions = new ArrayList<>(Arrays.asList(Direction.values()));
        List<Direction> bestDirections = new ArrayList<>();
        double maxVal = -Integer.MAX_VALUE;
        for (var dire : directions) {
            var player = gameState.getPlayer();
            if (player.getOwner() == null) {
                throw new IllegalArgumentException("player's owner is null in makeMoveSmartly()");
            }
            var moveResult = gameState.getGameBoardController()
                    .tryMove(player.getOwner().getPosition(), dire, player.getId());
            if (!(moveResult instanceof MoveResult.Valid.Alive aliveState)) {
                continue;
            }

            gameState.getGameBoardController().makeMove(dire, player.getId());

//            System.out.println("max choose: " + dire);

            gameState.increaseNumGotGems(aliveState.collectedGems.size()); // add collected gem nums

            double thisValue = getValue(getGameStates(), 0, 5, switchPlayer(gameState.getPlayer().getId()));
//            System.out.println("root: depth: " + 0 + ", direction: " + dire + ", value: " + thisValue);
            if (thisValue > maxVal) {
                maxVal = thisValue;
                bestDirections.clear();
                bestDirections.add(dire);
            } else if (thisValue == maxVal) {
                bestDirections.add(dire);
            }

            // undo the move
            var gameBoard = gameState.getGameBoard();
            gameBoard.getEntityCell(aliveState.origPosition).setEntity(gameState.getPlayer());
            gameState.increaseNumGotGems(-aliveState.collectedGems.size());
//            System.out.println("decreasing agent: " + player.getId() + " gem by " + aliveState.collectedGems.size());


            for (@NotNull final var gemPos : aliveState.collectedGems) {
                gameBoard.getEntityCell(gemPos).setEntity(new Gem());
            }
            for (@NotNull final var extraLifePos : aliveState.collectedExtraLives) {
                gameBoard.getEntityCell(extraLifePos).setEntity(new ExtraLife());
            }
        }
        Collections.shuffle(bestDirections);
//        System.out.println("best direction: " + bestDirections.get(0) + "\n\n\n\n");
        processor.move(bestDirections.get(0));
        lock.unlock();
    }*/

    /**
     * TODO implement this method
     * The robot moves with a smarter strategy compared to random.
     * This strategy is expected to beat random strategy in most of the time.
     * That is to say we will let random robot and smart robot compete with each other and repeat many (>10) times
     * (10 seconds timeout for each run).
     * You will get the grade if the robot with your implementation can win in more than half of the total runs
     * (e.g., at least 6 when total is 10).
     * <p>
     *
     * @param processor The processor to make movements.
     */
    private void makeMoveSmartly(MoveProcessor processor) {
        lock.lock();

        lock.unlock();
    }

}
