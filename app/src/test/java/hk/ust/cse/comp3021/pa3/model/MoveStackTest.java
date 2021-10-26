package hk.ust.cse.comp3021.pa3.model;

import hk.ust.cse.comp3021.pa3.util.ReflectionUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

public class MoveStackTest {

    private MoveStack moveStack;

    @BeforeEach
    void setUp() {
        moveStack = new MoveStack();
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Constructors")
    void testConstructors() {
        final var clazz = MoveStack.class;
        final var ctors = ReflectionUtils.getPublicConstructors(clazz);

        assertEquals(1, ctors.length);

        assertDoesNotThrow((ThrowingSupplier<Constructor<MoveStack>>) clazz::getConstructor);
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Methods")
    void testPublicMethods() {
        final var clazz = MoveStack.class;
        final var publicMethods = ReflectionUtils.getPublicInstanceMethods(clazz);

        assertEquals(5, publicMethods.length);

        assertDoesNotThrow(() -> clazz.getDeclaredMethod("push", MoveResult.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("isEmpty"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("pop"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getPopCount"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("peek"));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Fields")
    void testPublicFields() {
        final var clazz = MoveStack.class;
        final var publicFields = ReflectionUtils.getPublicInstanceFields(clazz);

        assertEquals(0, publicFields.length);
    }

    @Test
    @Tag("sanity")
    @DisplayName("Initial State")
    void testInitialState() {
        assertEquals(0, moveStack.getPopCount());
        assertTrue(moveStack.isEmpty());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Push Move")
    void testPush() {
        final var move = new MoveResult.Valid.Alive(
                new Position(1, 0),
                new Position(0, 0)
        );

        moveStack.push(move);

        assertFalse(moveStack.isEmpty());
        assertEquals(moveStack.peek(), move);
    }

    @Test
    @Tag("sanity")
    @DisplayName("Pop Move")
    void testPop() {
        final var move = new MoveResult.Valid.Alive(
                new Position(1, 0),
                new Position(0, 0)
        );

        moveStack.push(move);

        final var poppedElem = assertDoesNotThrow(() -> moveStack.pop());

        assertTrue(moveStack.isEmpty());
        assertEquals(move, poppedElem);
        assertEquals(1, moveStack.getPopCount());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Push-Pop Moves in LIFO Order")
    void testPushPopInLIFOOrder() {
        final var move1 = new MoveResult.Valid.Alive(
                new Position(1, 0),
                new Position(0, 0)
        );
        final var move2 = new MoveResult.Valid.Alive(
                new Position(2, 0),
                new Position(1, 0)
        );
        final var move3 = new MoveResult.Valid.Alive(
                new Position(3, 0),
                new Position(2, 0)
        );

        moveStack.push(move1);
        moveStack.push(move2);
        moveStack.push(move3);

        assertEquals(move3, moveStack.peek());

        assertEquals(move3, moveStack.pop());
        assertEquals(1, moveStack.getPopCount());

        assertEquals(move2, moveStack.pop());
        assertEquals(2, moveStack.getPopCount());

        assertEquals(move1, moveStack.pop());
        assertEquals(3, moveStack.getPopCount());

        assertTrue(moveStack.isEmpty());
    }

    @AfterEach
    void tearDown() {
        moveStack = null;
    }
}
