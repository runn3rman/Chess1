package chess;
import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTeamTurn;
    public ChessGame() {
        this.currentTeamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.currentTeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            // No piece at the given position
            return null;
        }

        TeamColor opponentColor = (piece.getTeamColor() == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : potentialMoves) {
            makeTempMove(move); // Simulate the move
            // Check if the moving piece's king is in check after the move
            boolean isCurrentKingInCheck = isInCheck(piece.getTeamColor());
            boolean isMovingIntoCheck = false;

            if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                // Additional check for king moves: The king should not move into a square that is under attack
                isMovingIntoCheck = isSquareUnderAttack(move.getEndPosition(), opponentColor);
            }

            if (!isCurrentKingInCheck && !isMovingIntoCheck) {
                validMoves.add(move); // If the move doesn't result in a check and doesn't move the king into check, add it to the valid moves
            }

            undoTempMove(move); // Undo the simulated move
        }

        return validMoves;
    }

    // Helper method to check if a square is under attack
    private boolean isSquareUnderAttack(ChessPosition position, TeamColor attackerColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) { //go through each square
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if (currentPiece != null && currentPiece.getTeamColor() == attackerColor) {
                    Collection<ChessMove> opponentMoves = currentPiece.pieceMoves(board, currentPosition);
                    for (ChessMove opponentMove : opponentMoves) {
                        if (opponentMove.getEndPosition().equals(position)) {
                            return true; // The square is under attack by an opponent's piece
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at the start position.");
        }
        if (piece.getTeamColor() != this.currentTeamTurn) {
            throw new InvalidMoveException("It's not " + piece.getTeamColor() + "'s turn.");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition()); //get all Valid moves to check
        if (legalMoves != null && legalMoves.contains(move)) {
            // Execute the move
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);

            // Handle pawn promotion
            if (move.promotionPiece() != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.promotionPiece()));
            }

            // Change turn
            this.currentTeamTurn = (this.currentTeamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        } else {
            throw new InvalidMoveException("This move is not allowed.");
        }
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) {
            // King not found, should not happen if the board is in a valid state
            return false;
        }

        // Check if any of the opposing team's pieces can capture the King
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    // It's an opposing piece, check its valid moves
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            // King can be captured by this move
                            return true;
                        }
                    }
                }
            }
        }

        // No opposing pieces can capture the King
        return false;
    }

    private ChessPosition findKingPosition(TeamColor teamColor) { //helper function
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position; // Found the King
                }
            }
        }
        return null; // King not found
    }



    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // 1. Check if the King is in Check
        if (!isInCheck(teamColor)) {
            return false;  // If the king is not in check, it can't be in checkmate.
        }

        // 2. Generate All Possible Moves for the Team
        Collection<ChessMove> possibleMoves = getAllPossibleMoves(teamColor);

        // 3. Verify if Any Move Can Evade Check
        for (ChessMove move : possibleMoves) {
            makeTempMove(move); // Simulate the move

            if (!isInCheck(teamColor)) { // If the move takes the king out of check
                undoTempMove(move); // Undo the move
                return false;  // It's not checkmate since the king can evade check.
            }

            undoTempMove(move); // Undo the move
        }

        // 4. Determine Checkmate
        return true; // If no moves can take the king out of check, it's checkmate.
    }

    /**
     * Generate all possible moves for the specified team.
     *
     * @param teamColor the color of the team
     * @return a collection of all possible moves
     */
    private Collection<ChessMove> getAllPossibleMoves(TeamColor teamColor) { //helper function
        Collection<ChessMove> allMoves = new ArrayList<>();
        // Traverse the entire board
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                // If the piece belongs to the team, get its valid moves
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> pieceMoves = piece.pieceMoves(board, position);
                    if (pieceMoves != null) {
                        allMoves.addAll(pieceMoves);
                    }
                }
            }
        }
        return allMoves;
    }

    // Store the original and temporary state of the squares for undoing moves
    private Map<ChessPosition, ChessPiece> originalState = new HashMap<>();
    private Map<ChessPosition, ChessPiece> temporaryState = new HashMap<>();

    /**
     * Simulates making a move on the board.
     *
     * @param move the move to make
     */
    private void makeTempMove(ChessMove move) { //more helpers
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece movingPiece = board.getPiece(start);

        // Save the original state
        originalState.put(start, movingPiece);
        originalState.put(end, board.getPiece(end));

        // Make the move on the board
        board.addPiece(end, movingPiece); // Move the piece to the new position
        board.addPiece(start, null); // Remove the piece from the original position

        // Save the temporary state
        temporaryState.put(end, movingPiece);
        temporaryState.put(start, board.getPiece(start));
    }

    /**
     * Undoes a simulated move, restoring the board to its previous state.
     *
     * @param move the move to undo
     */
    private void undoTempMove(ChessMove move) { //more helpers
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        // Restore the pieces to their original positions
        board.addPiece(start, originalState.get(start));
        board.addPiece(end, originalState.get(end));

        // Clear the saved states as they are no longer needed
        originalState.remove(start);
        originalState.remove(end);
        temporaryState.remove(start);
        temporaryState.remove(end);
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // If the team is in check, it's not a stalemate.
        if (isInCheck(teamColor)) {
            return false;
        }

        // Get all possible moves for the team
        Collection<ChessMove> possibleMoves = getAllPossibleMoves(teamColor);

        // Check if there's any legal move available
        for (ChessMove move : possibleMoves) {
            makeTempMove(move); // Simulate the move

            // If after making a move, the king is not in check, then it's not a stalemate
            if (!isInCheck(teamColor)) {
                undoTempMove(move); // Undo the move
                return false;  // There's at least one valid move, so it's not stalemate.
            }

            undoTempMove(move); // Undo the move
        }

        // If no valid moves are available, it's a stalemate.
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTeamTurn == chessGame.currentTeamTurn && Objects.equals(originalState, chessGame.originalState) && Objects.equals(temporaryState, chessGame.temporaryState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTeamTurn, originalState, temporaryState);
    }
}
