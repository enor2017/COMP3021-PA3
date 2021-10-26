package hk.ust.cse.comp3021.pa3.model;

import hk.ust.cse.comp3021.pa3.util.ReflectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StopCellTest {

    private Position position;
    private StopCell cell;

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Constructors")
    void testConstructors() {
        final var clazz = StopCell.class;
        final var ctors = ReflectionUtils.getPublicConstructors(clazz);

        assertEquals(2, ctors.length);

        assertDoesNotThrow(() -> clazz.getConstructor(Position.class));
        assertDoesNotThrow(() -> clazz.getConstructor(Position.class, Entity.class));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Methods")
    void testPublicMethods() {
        final var clazz = StopCell.class;
        final var publicMethods = ReflectionUtils.getPublicInstanceMethods(clazz);

        assertEquals(5, publicMethods.length);

        assertDoesNotThrow(() -> clazz.getDeclaredMethod("setEntity", Entity.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("setPlayer", Player.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("toUnicodeChar"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("toASCIIChar"));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Fields")
    void testPublicFields() {
        final var clazz = StopCell.class;
        final var publicFields = ReflectionUtils.getPublicInstanceFields(clazz);

        assertEquals(0, publicFields.length);
    }

    @Test
    @Tag("sanity")
    @DisplayName("Set Entity - Non-Player")
    void testSetEntityToNonPlayer() {
        position = new Position(0, 0);
        cell = new StopCell(position);

        assertThrows(IllegalArgumentException.class, () -> cell.setEntity(new Gem()));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Set Entity - Player")
    void testSetEntityToPlayer() {
        position = new Position(0, 0);
        cell = new StopCell(position);

        final var player = new Player();
        assertDoesNotThrow(() -> cell.setEntity(player));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Set Player - Not-Null to Null")
    void testSetPlayerFromNotNullToNull() {
        final var player = new Player();
        position = new Position(0, 0);
        cell = new StopCell(position, player);

        final var prev = cell.setPlayer(null);

        assertNull(cell.entity);
        assertSame(player, prev);
        assertNull(player.getOwner());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Set Player - Null to Not-Null")
    void testSetPlayerFromNullToNotNull() {
        position = new Position(0, 0);
        cell = new StopCell(position);

        final var player = new Player();
        final var prev = cell.setPlayer(player);

        assertSame(player, cell.entity);
        assertSame(cell, player.getOwner());
        assertNull(prev);
    }

    @Test
    @Tag("sanity")
    @DisplayName("Set Player - Null to Null")
    void testSetPlayerFromNullToNull() {
        position = new Position(0, 0);
        cell = new StopCell(position);

        final var prev = cell.setPlayer(null);

        assertNull(cell.entity);
        assertNull(prev);
    }

    @Test
    @Tag("sanity")
    @DisplayName("Set Player - Null to Not-Null")
    void testSetPlayerFromNotNullToNotNull() {
        final var origPlayer = new Player();
        position = new Position(0, 0);
        cell = new StopCell(position, origPlayer);

        final var newPlayer = new Player();
        final var prev = cell.setPlayer(newPlayer);

        assertSame(newPlayer, cell.entity);
        assertSame(cell, newPlayer.getOwner());
        assertNull(origPlayer.getOwner());
        assertSame(origPlayer, prev);
    }

    @AfterEach
    void tearDown() {
        cell = null;
        position = null;
    }
}
