package hk.ust.cse.comp3021.pa3.util;

import hk.ust.cse.comp3021.pa3.model.Direction;
import hk.ust.cse.comp3021.pa3.model.GameState;
import hk.ust.cse.comp3021.pa3.model.MoveResult;
import javafx.application.Platform;
import javafx.scene.paint.Stop;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
                    // need to let JavaFX Thread run this method.
                    Platform.runLater(() -> makeMoveRandomly(processor));
//                    makeMoveRandomly(processor);
                } else {
                    Platform.runLater(() -> makeMoveSmartly(processor));
//                    makeMoveSmartly(processor);
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
            int i = 0;
            while (th.getState() != Thread.State.TERMINATED && i++ <= 1000) {
                System.out.println(th + " still running!, i = " + i);
            }
            System.out.println("out!");
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
     * TODO modify this method if you need to do thread synchronization.
     *
     * @param processor The processor to make movements.
     */
    private void makeMoveRandomly(MoveProcessor processor) {
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
    }

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

    }

}
