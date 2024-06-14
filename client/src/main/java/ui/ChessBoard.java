package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class ChessBoard {

    private static final int BOARD_SIZE = 8;
    private static final String EMPTY = "   ";
    private static String currentChar = EMPTY;
    private static String currentColor = null;

    private static chess.ChessBoard board; //

    public static void drawInitialBoard(String perspective) {
        board = new chess.ChessBoard(); // Initialize the chessboard
        board.resetBoard(); //

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        boolean isWhite = "WHITE".equalsIgnoreCase(perspective);
        drawBoard(out, isWhite);
        drawBoard(out, !isWhite);
    }

    private static void drawBoard(PrintStream out, boolean whitePerspective) {
        out.print(ERASE_SCREEN);
        out.println(whitePerspective ? "White Perspective:" : "Black Perspective:");
        drawHeaders(out);

        String[] rowHeaders = {"1", "2", "3", "4", "5", "6", "7", "8"};
        int startRow = whitePerspective ? 1 : BOARD_SIZE;
        int endRow = whitePerspective ? BOARD_SIZE : 1;
        int step = whitePerspective ? 1 : -1;

        for (int row = startRow; whitePerspective ? row <= endRow : row >= endRow; row += step) {
            // Determine the color of the background for the row header
            boolean isRowHeaderWhite = (row % 2 != 0);

            // Print the row header with the opposite color of the first square
            out.print((isRowHeaderWhite ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK) + SET_TEXT_COLOR_GREEN + rowHeaders[row - 1] + RESET_TEXT_COLOR + " ");
            for (int col = 1; col <= BOARD_SIZE; col++) {
                setCurrentCharAndColor(row, col);
                if ((row + col) % 2 == 0) {
                    out.print(SET_BG_COLOR_WHITE + currentColor + currentChar + RESET_TEXT_COLOR);
                } else {
                    out.print(SET_BG_COLOR_BLACK + currentColor + currentChar + RESET_TEXT_COLOR);
                }
            }
            // Print the row header again at the end of the row with the same color as the start
            out.print(" " + (isRowHeaderWhite ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK) + SET_TEXT_COLOR_GREEN + rowHeaders[row - 1] + RESET_TEXT_COLOR);
            out.println();
        }
        drawHeaders(out);
        out.print(SET_BG_COLOR_BLACK); // Ensure to reset the background to black after finishing the board
    }

    private static void drawHeaders(PrintStream out) {
        setBlack(out);
        String[] headers = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
        int totalWidth = BOARD_SIZE * 3 + 2; // Each square is 3 characters wide, plus padding
        int headerWidth = headers.length * 3;
        int padding = (totalWidth - headerWidth) / 2;

        // Print padding to center align headers
        out.print(SET_BG_COLOR_BLACK + " ".repeat(padding));
        for (String header : headers) {
            out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_GREEN + header + RESET_TEXT_COLOR);
        }
        out.println(RESET_TEXT_COLOR);
    }

    private static void setCurrentCharAndColor(int row, int col) {
        ChessPiece piece = getPiece(row, col);
        if (piece == null) {
            currentChar = EMPTY;
            currentColor = SET_TEXT_COLOR_BLACK;
        } else {
            switch (piece.getPieceType()) {
                case KING:
                    currentChar = " K ";
                    break;
                case QUEEN:
                    currentChar = " Q ";
                    break;
                case PAWN:
                    currentChar = " P ";
                    break;
                case ROOK:
                    currentChar = " R ";
                    break;
                case KNIGHT:
                    currentChar = "KN ";
                    break;
                case BISHOP:
                    currentChar = " B ";
                    break;
            }
            currentColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
        }
    }

    private static ChessPiece getPiece(int row, int col) {
        return board.getPiece(new ChessPosition(row, col));
    }

    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }
}
