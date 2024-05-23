package chess;
import java.util.ArrayList;
import java.util.Collection;

public abstract class ChessPieceMovement {
    protected ChessBoard board;
    protected ChessPosition position;

    // Constructor to initialize the board and position
    public ChessPieceMovement(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
    }

    // Abstract method to be implemented by each piece for calculating moves
    public abstract Collection<ChessMove> pieceMoves();

    // Method to traverse the board in specific directions
    protected void traverseBoard(Collection<ChessMove> moves, int[][] paths, boolean isLimited) {
        for (int[] path : paths) {
            int tempRow = position.getRow();
            int tempCol = position.getColumn();

            // Loop to move along a path until a block or edge
            do {
                tempRow += path[0];
                tempCol += path[1];
                ChessPosition newPos = new ChessPosition(tempRow, tempCol);

                // Break the loop if a move cannot be added
                if (!tryAddMove(moves, newPos)) {
                    break;
                }
            } while(!isLimited);
        }
    }

    // Try adding a move and check for blockages or board limits
    private boolean tryAddMove(Collection<ChessMove> moves, ChessPosition newPos) {
        // Checking for board boundaries
        if (newPos.getRow() <= 0 || newPos.getRow() > 8 || newPos.getColumn() <= 0 || newPos.getColumn() > 8) {
            return false;
        }

        ChessPiece pieceAtNewPos = board.getPiece(newPos);

        // Conditionally add a move based on the presence of another piece
        if (pieceAtNewPos != null) {
            // Add move if it's an opponent's piece
            if (pieceAtNewPos.getTeamColor() != this.board.getPiece(this.position).getTeamColor()) {
                moves.add(new ChessMove(this.position, newPos, null));
            }
            return false;
        }

        // Add move if the square is unoccupied
        moves.add(new ChessMove(this.position, newPos, null));
        return true;
    }
}

class RookMovement extends ChessPieceMovement {
    public RookMovement(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    @Override
    public Collection<ChessMove> pieceMoves() {
        Collection<ChessMove> rookPaths = new ArrayList<>();
        int[][] moveVectors = {{0, 1}, {1, 0}, {-1, 0}, {0, -1}}; // Horizontal and Vertical movement
        traverseBoard(rookPaths, moveVectors, false);
        return rookPaths;
    }
}

class BishopMovement extends ChessPieceMovement {
    public BishopMovement(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    @Override
    public Collection<ChessMove> pieceMoves() {
        Collection<ChessMove> bishopTrajectories = new ArrayList<>();
        int[][] diagonalVectors = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // Diagonal movement
        traverseBoard(bishopTrajectories, diagonalVectors, false);
        return bishopTrajectories;
    }
}

class QueenMovement extends ChessPieceMovement {
    public QueenMovement(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    @Override
    public Collection<ChessMove> pieceMoves() {
        Collection<ChessMove> queenRoutes = new ArrayList<>();
        int[][] combinedVectors = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {0, 1}, {1, 0}, {-1, 0}, {0, -1}}; // Combining Rook and Bishop movements
        traverseBoard(queenRoutes, combinedVectors,false);
        return queenRoutes;
    }
}
class PawnMovement extends ChessPieceMovement {
    public PawnMovement(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    @Override
    public Collection<ChessMove> pieceMoves() {
        Collection<ChessMove> pawnAdvances = new ArrayList<>();
        ChessPiece currentPawn = board.getPiece(position);
        int moveDirection = currentPawn.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
        int startingRow = currentPawn.getTeamColor() == ChessGame.TeamColor.WHITE ? 2 : 7;
        int promotionRow = currentPawn.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;

        // Normal forward move
        evaluatePawnForwardMove(pawnAdvances, position.getRow() + moveDirection, position.getColumn(), promotionRow);

        // Pawn's initial double move
        if (position.getRow() == startingRow && isPathFree(moveDirection)) {
            evaluatePawnForwardMove(pawnAdvances, position.getRow() + 2 * moveDirection, position.getColumn(), promotionRow);
        }

        // Diagonal captures
        evaluatePawnCaptureMove(pawnAdvances, position.getRow() + moveDirection, position.getColumn() + 1, promotionRow);
        evaluatePawnCaptureMove(pawnAdvances, position.getRow() + moveDirection, position.getColumn() - 1, promotionRow);

        return pawnAdvances;
    }

    private void evaluatePawnForwardMove(Collection<ChessMove> moves, int row, int col, int promotionRow) {
        if (row > 0 && row <= 8 && col > 0 && col <= 8 && board.getPiece(new ChessPosition(row, col)) == null) {
            if (row == promotionRow) {
                handlePawnPromotion(moves, row, col);
            } else {
                moves.add(new ChessMove(position, new ChessPosition(row, col), null)); // No promotion
            }
        }
    }

    private void handlePawnPromotion(Collection<ChessMove> moves, int row, int col) {
        for (ChessPiece.PieceType type : ChessPiece.PieceType.values()) {
            if (type != ChessPiece.PieceType.PAWN && type != ChessPiece.PieceType.KING) {
                moves.add(new ChessMove(position, new ChessPosition(row, col), type));
            }
        }
    }

    private void evaluatePawnCaptureMove(Collection<ChessMove> moves, int row, int col, int promotionRow) {
        if (row > 0 && row <= 8 && col > 0 && col <= 8) {
            ChessPiece target = board.getPiece(new ChessPosition(row, col));
            if (target != null && target.getTeamColor() != board.getPiece(position).getTeamColor()) {
                if (row == promotionRow) {
                    // Handle promotion during capture
                    for (ChessPiece.PieceType type : ChessPiece.PieceType.values()) {
                        if (type != ChessPiece.PieceType.PAWN && type != ChessPiece.PieceType.KING) {
                            moves.add(new ChessMove(position, new ChessPosition(row, col), type));
                        }
                    }
                } else {
                    // Regular capture without promotion
                    moves.add(new ChessMove(position, new ChessPosition(row, col), null));
                }
            }
        }
    }

    private boolean isPathFree(int direction) {
        ChessPosition inFront = new ChessPosition(position.getRow() + direction, position.getColumn());
        return board.getPiece(inFront) == null;
    }
}

class KnightMovement extends ChessPieceMovement {
    public KnightMovement(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    @Override
    public Collection<ChessMove> pieceMoves() {
        Collection<ChessMove> knightHops = new ArrayList<>();
        int[][] leapPatterns = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}}; // Knight's unique movements
        traverseBoard(knightHops, leapPatterns, true);
        return knightHops;
    }

    private boolean isWithinBoard(int row, int col) {
        return row > 0 && row <= 8 && col > 0 && col <= 8;
    }
}

class KingMovement extends ChessPieceMovement {
    public KingMovement(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
    @Override
    public Collection<ChessMove> pieceMoves() {
        Collection<ChessMove> kingSteps = new ArrayList<>();
        int[][] surroundingSquares = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}}; // King's one-step movement
        traverseBoard(kingSteps,surroundingSquares,true);
        return kingSteps;
    }
}
