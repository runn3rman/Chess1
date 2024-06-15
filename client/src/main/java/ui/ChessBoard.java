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

    private static chess.ChessBoard board;

    public static void drawInitialBoard(String perspective) {
        board = new chess.ChessBoard(); // Initialize the chessboard
        board.resetBoard();

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        boolean isWhite = "WHITE".equalsIgnoreCase(perspective);
        drawBoard(out, isWhite);
        drawBoard(out, !isWhite);
    }

    private static void drawBoard(PrintStream out, boolean whitePerspective) {
        out.print(ERASE_SCREEN);
        out.println(whitePerspective ? "White Perspective:" : "Black Perspective:");
        drawHeaders(out, whitePerspective);

        String[] rowHeaders = whitePerspective ? new String[]{"8", "7", "6", "5", "4", "3", "2", "1"} : new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};

        int startRow = whitePerspective ? 8 : 1;
        int endRow = whitePerspective ? 1 : 8;
        int step = whitePerspective ? -1 : 1;

        for (int row = startRow; whitePerspective ? row >= endRow : row <= endRow; row += step) {
            // Print the row header with a grey background
            out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + rowHeaders[whitePerspective ? 8 - row : row - 1] + " " + RESET_TEXT_COLOR + RESET_BG_COLOR + " ");
            for (int col = 1; col <= BOARD_SIZE; col++) {
                setCurrentCharAndColor(row, col, whitePerspective);
                boolean isWhiteSquare = (row + col) % 2 != 0;
                if (isWhiteSquare) {
                    out.print(SET_BG_COLOR_WHITE + currentColor + currentChar + RESET_TEXT_COLOR);
                } else {
                    out.print(SET_BG_COLOR_BLACK + currentColor + currentChar + RESET_TEXT_COLOR);
                }
            }
            // Print the row header again at the end of the row with a grey background
            out.print(" " + SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + rowHeaders[whitePerspective ? 8 - row : row - 1] + " " + RESET_TEXT_COLOR + RESET_BG_COLOR);
            out.println();
        }
        drawHeaders(out, whitePerspective);
        out.print(RESET_BG_COLOR); // Ensure to reset the background after finishing the board
    }

    private static void drawHeaders(PrintStream out, boolean whitePerspective) {
        String[] headers = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
        out.print(SET_BG_COLOR_LIGHT_GREY + "   " + RESET_BG_COLOR); // Left corner
        for (String header : headers) {
            out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + header + RESET_TEXT_COLOR + RESET_BG_COLOR);
        }
        out.print(SET_BG_COLOR_LIGHT_GREY + "   " + RESET_BG_COLOR); // Right corner
        out.println();
    }

    private static void setCurrentCharAndColor(int row, int col, boolean whitePerspective) {
        int boardRow = whitePerspective ? 9 - row : row;
        int boardCol = whitePerspective ? col : 9 - col;
        ChessPiece piece = getPiece(boardRow, boardCol);
        if (piece == null) {
            currentChar = EMPTY;
            currentColor = SET_TEXT_COLOR_BLACK;
        } else {
            switch (piece.getPieceType()) {
                case KING:
                    currentChar = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
                    break;
                case QUEEN:
                    currentChar = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
                    break;
                case PAWN:
                    currentChar = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
                    break;
                case ROOK:
                    currentChar = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
                    break;
                case KNIGHT:
                    currentChar = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
                    break;
                case BISHOP:
                    currentChar = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
                    break;
            }
            // Set the text color to ensure visibility on both white and black squares
            boolean isWhiteSquare = (row + col) % 2 != 0;
            if (isWhiteSquare && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                currentColor = SET_TEXT_COLOR_BLACK; // White piece on white square, use black color
            } else if (!isWhiteSquare && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                currentColor = SET_TEXT_COLOR_WHITE; // Black piece on black square, use white color
            } else {
                currentColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
            }
        }
    }

    private static ChessPiece getPiece(int row, int col) {
        return board.getPiece(new ChessPosition(row, col));
    }
}
