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

public class GameBoardTest {

    private GameBoard gameBoard;

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Constructors")
    void testConstructors() {
        final var clazz = GameBoard.class;
        final var ctors = ReflectionUtils.getPublicConstructors(clazz);

        assertThat(ctors.length, Matchers.greaterThanOrEqualTo(1));

        assertDoesNotThrow(() -> clazz.getConstructor(int.class, int.class, Cell[][].class));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Methods")
    void testPublicMethods() {
        final var clazz = GameBoard.class;
        final var publicMethods = ReflectionUtils.getPublicInstanceMethods(clazz);

        assertThat(publicMethods.length, Matchers.greaterThanOrEqualTo(14));

        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getRow", int.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getCol", int.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getCell", int.class, int.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getCell", Position.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getEntityCell", int.class, int.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getEntityCell", int.class, int.class));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getNumRows"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getNumCols"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getPlayer"));
        assertDoesNotThrow(() -> clazz.getDeclaredMethod("getNumGems"));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Sanity Test - Public Fields")
    void testPublicFields() {
        final var clazz = GameBoard.class;
        final var publicFields = ReflectionUtils.getPublicInstanceFields(clazz);

        assertThat(publicFields.length, Matchers.greaterThanOrEqualTo(0));
    }

    // P*.
    // ...
    // ...
    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - Valid")
    void testBasicGameBoardCreation() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][1]).setEntity(new Gem());

        assertDoesNotThrow(() -> gameBoard = new GameBoard(rows, cols, cells));
        assertTrue(((EntityCell) gameBoard.getCell(0, 0)).getEntity() instanceof Player);
        assertTrue(((EntityCell) gameBoard.getCell(0, 1)).getEntity() instanceof Gem);
    }

    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - Bad Row")
    void testGameBoardCreationBadRowCount() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(2, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][1]).setEntity(new Gem());

        assertThrows(IllegalArgumentException.class, () -> gameBoard = new GameBoard(rows, cols, cells));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - Bad Column")
    void testGameBoardCreationBadColCount() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, 2, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][1]).setEntity(new Gem());

        assertThrows(IllegalArgumentException.class, () -> gameBoard = new GameBoard(rows, cols, cells));
    }

    // .*.
    // ...
    // ...
    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - Missing Player")
    void testGameBoardCreationMissingPlayer() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][1]).setEntity(new Gem());

        assertThrows(IllegalArgumentException.class, () -> gameBoard = new GameBoard(rows, cols, cells));
    }

    // P..
    // ...
    // ...
    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - No Gems")
    void testGameBoardCreationMissingGem() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());

        assertThrows(IllegalArgumentException.class, () -> gameBoard = new GameBoard(rows, cols, cells));
    }

    // PW.
    // W*.
    // ...
    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - Unreachable Gem in Corner")
    void testGameBoardCreationUnreachableGemCorner() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, (pos) -> {
            if (pos.equals(new Position(1, 0)) || pos.equals(new Position(0, 1))) {
                return new Wall(pos);
            } else {
                return new EntityCell(pos);
            }
        });

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[1][1]).setEntity(new Gem());

        assertThrows(IllegalArgumentException.class, () -> gameBoard = new GameBoard(rows, cols, cells));
    }

    // P....
    // WWWW.
    // .....
    // .WWWW
    // ...W*
    @Test
    @Tag("sanity")
    @DisplayName("Instance Creation - Unreachable Gem in Linear Path")
    void testGameBoardCreationUnreachableGemLinear() {
        final var rows = 5;
        final var cols = 5;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[4][4]).setEntity(new Gem());

        for (int c = 0; c < cols - 1; ++c) {
            cells[1][c] = new Wall(new Position(1, c));
        }
        for (int c = 1; c < cols; ++c) {
            cells[3][c] = new Wall(new Position(3, c));
        }
        cells[4][3] = new Wall(new Position(4, 3));

        assertThrows(IllegalArgumentException.class, () -> gameBoard = new GameBoard(rows, cols, cells));
    }

    // *P*
    // *.X
    // *LX
    @Test
    @Tag("sanity")
    @DisplayName("Get Row")
    void testGetRow() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][1]).setEntity(new Player());
        for (int i = 1; i < rows; ++i) {
            ((EntityCell) cells[i][2]).setEntity(new Mine());
        }
        for (int i = 0; i < rows; ++i) {
            ((EntityCell) cells[i][0]).setEntity(new Gem());
        }
        ((EntityCell) cells[2][1]).setEntity(new ExtraLife());
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        final var row1 = gameBoard.getRow(0);
        assertEquals(((EntityCell) cells[0][0]).getEntity(), ((EntityCell) row1[0]).getEntity());
        assertEquals(((EntityCell) cells[0][1]).getEntity(), ((EntityCell) row1[1]).getEntity());
        assertEquals(((EntityCell) cells[0][2]).getEntity(), ((EntityCell) row1[2]).getEntity());

        final var row2 = gameBoard.getRow(1);
        assertEquals(((EntityCell) cells[1][0]).getEntity(), ((EntityCell) row2[0]).getEntity());
        assertEquals(((EntityCell) cells[1][1]).getEntity(), ((EntityCell) row2[1]).getEntity());
        assertEquals(((EntityCell) cells[1][2]).getEntity(), ((EntityCell) row2[2]).getEntity());

        final var row3 = gameBoard.getRow(2);
        assertEquals(((EntityCell) cells[2][0]).getEntity(), ((EntityCell) row3[0]).getEntity());
        assertEquals(((EntityCell) cells[2][1]).getEntity(), ((EntityCell) row3[1]).getEntity());
        assertEquals(((EntityCell) cells[2][2]).getEntity(), ((EntityCell) row3[2]).getEntity());
    }

    // *P*
    // *.X
    // *LX
    @Test
    @Tag("sanity")
    @DisplayName("Get Column")
    void testGetCol() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][1]).setEntity(new Player());
        for (int i = 1; i < rows; ++i) {
            ((EntityCell) cells[i][2]).setEntity(new Mine());
        }
        for (int i = 0; i < rows; ++i) {
            ((EntityCell) cells[i][0]).setEntity(new Gem());
        }
        ((EntityCell) cells[2][1]).setEntity(new ExtraLife());
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        final var col1 = gameBoard.getCol(0);
        assertEquals(((EntityCell) cells[0][0]).getEntity(), ((EntityCell) col1[0]).getEntity());
        assertEquals(((EntityCell) cells[1][0]).getEntity(), ((EntityCell) col1[1]).getEntity());
        assertEquals(((EntityCell) cells[2][0]).getEntity(), ((EntityCell) col1[2]).getEntity());

        final var col2 = gameBoard.getCol(1);
        assertEquals(((EntityCell) cells[0][1]).getEntity(), ((EntityCell) col2[0]).getEntity());
        assertEquals(((EntityCell) cells[1][1]).getEntity(), ((EntityCell) col2[1]).getEntity());
        assertEquals(((EntityCell) cells[2][1]).getEntity(), ((EntityCell) col2[2]).getEntity());

        final var col3 = gameBoard.getCol(2);
        assertEquals(((EntityCell) cells[0][2]).getEntity(), ((EntityCell) col3[0]).getEntity());
        assertEquals(((EntityCell) cells[1][2]).getEntity(), ((EntityCell) col3[1]).getEntity());
        assertEquals(((EntityCell) cells[2][2]).getEntity(), ((EntityCell) col3[2]).getEntity());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Cell - int-overload")
    void testGetCellWithInts() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                assertEquals(cells[r][c], gameBoard.getCell(r, c), "Mismatch for r=" + r + " c=" + c);
            }
        }
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Cell - Position-overload")
    void testGetCellWithPos() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                final var pos = new Position(r, c);
                assertEquals(cells[r][c], gameBoard.getCell(pos), "Mismatch for pos=" + pos);
            }
        }
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Entity Cell - Valid using int-overload")
    void testGetEntityCellWithIntsValid() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        final var entityCell = assertDoesNotThrow(() -> gameBoard.getEntityCell(0, 0));
        assertEquals(gameBoard.getCell(0, 0), entityCell);
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Entity Cell - Invalid using int-overload")
    void testGetEntityCellWithIntsInvalid() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        cells[0][1] = new Wall(new Position(0, 1));
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        assertThrows(IllegalArgumentException.class, () -> gameBoard.getEntityCell(0, 1));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Entity Cell - Valid using Position-overload")
    void testGetEntityCellWithPosValid() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        final var entityCell = assertDoesNotThrow(() -> gameBoard.getEntityCell(new Position(0, 0)));
        assertEquals(gameBoard.getCell(0, 0), entityCell);
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Entity Cell - Invalid using Position-overload")
    void testGetEntityCellWithPosInvalid() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        cells[0][1] = new Wall(new Position(0, 1));
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        assertThrows(IllegalArgumentException.class, () -> gameBoard.getEntityCell(new Position(0, 1)));
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Number of Rows")
    void testGetNumRows() {
        final var rows = 3;
        final var cols = 4;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][3]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        assertEquals(rows, gameBoard.getNumRows());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Number of Columns")
    void testGetNumCols() {
        final var rows = 4;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        assertEquals(cols, gameBoard.getNumCols());
    }

    @Test
    @Tag("sanity")
    @DisplayName("Get Player Instance")
    void testGetPlayer() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        final var player = new Player();
        ((EntityCell) cells[0][0]).setEntity(player);
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        assertEquals(player, gameBoard.getPlayer());
    }

    // P..
    // ...
    // ...
    @Test
    @Tag("sanity")
    @DisplayName("Get Number of Gems - None")
    void testGetNumGemsNone() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);
        ((EntityCell) gameBoard.getCell(0, 2)).setEntity(null);

        assertEquals(0, gameBoard.getNumGems());
    }

    // P.*
    // ...
    // ...
    @Test
    @Tag("sanity")
    @DisplayName("Get Number of Gems - One")
    void testGetNumGemsOne() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[0][2]).setEntity(new Gem());

        gameBoard = new GameBoard(rows, cols, cells);

        assertEquals(1, gameBoard.getNumGems());
    }

    // P**
    // *.*
    // ***
    @Test
    @Tag("sanity")
    @DisplayName("Get Number of Gems - Filled")
    void testGetNumGemsAll() {
        final var rows = 3;
        final var cols = 3;
        final var cells = GameBoardUtils.createEmptyCellArray(rows, cols, EntityCell::new);

        for (final var row : cells) {
            for (final var c : row) {
                ((EntityCell) c).setEntity(new Gem());
            }
        }
        ((EntityCell) cells[0][0]).setEntity(new Player());
        ((EntityCell) cells[1][1]).setEntity(null);

        gameBoard = new GameBoard(rows, cols, cells);

        assertEquals(7, gameBoard.getNumGems());
    }

    @AfterEach
    void tearDown() {
        gameBoard = null;
    }
}
