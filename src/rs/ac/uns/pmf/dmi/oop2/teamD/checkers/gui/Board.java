package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui;

import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server.IUserDb;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.IUser;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.utility.Pair;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * JPanel representing checkers board with all of its logic
 */
class Board extends JPanel {

	public static final int BOARD_SIZE = 10;

	private static final int[] DI = {1, 1, -1, -1};
	private static final int[] DJ = {1, -1, 1, -1};

	private CheckersWindow window;
	private Field[][] board = new Field[BOARD_SIZE][BOARD_SIZE];
	private Icon bluePawn;
	private Icon orangePawn;
	private Icon blueQueen;
	private Icon orangeQueen;
	private Icon selectBlue;
	private Icon selectOrange;
	private Icon selectBlueQueen;
	private Icon selectOrangeQueen;
	private IUserDb userDb;
	private Field selectedField;
	private boolean isBlue;
	private IUser me;
	private boolean myMove;
	private List<Field> validFields;
	private int maxMoveLength;

	Board(IUserDb userDb, CheckersWindow window) {
		this.userDb = userDb;
		this.window = window;

		bluePawn = new ImageIcon("res\\blue.png");
		orangePawn = new ImageIcon("res\\orange.png");
		blueQueen = new ImageIcon("res\\blueQueen.png");
		orangeQueen = new ImageIcon("res\\orangeQueen.png");
		selectBlue = new ImageIcon("res\\selectBlue.png");
		selectOrange = new ImageIcon("res\\selectOrange.png");
		selectBlueQueen = new ImageIcon("res\\selectBlueQueen.png");
		selectOrangeQueen = new ImageIcon("res\\selectOrangeQueen.png");
	}

	Icon getBluePawn() {
		return bluePawn;
	}

	Icon getOrangePawn() {
		return orangePawn;
	}

	Icon getBlueQueen() {
		return blueQueen;
	}

	Icon getOrangeQueen() {
		return orangeQueen;
	}

	Icon getSelectBlue(){
		return selectBlue;
	}

	Icon getSelectOrange(){
		return selectOrange;
	}

	Icon getSelectBlueQueen(){
		return selectBlueQueen;
	}

	Icon getSelectOrangeQueen(){
		return selectOrangeQueen;
	}

	IUser getMe() {
		return me;
	}

	boolean isBlue() {
		return isBlue;
	}

	Field getSelectedField() {
		return selectedField;
	}

	void setSelectedField(Field selectedField) {
		if (this.selectedField != null) {
			this.selectedField.deselect();
		}
		this.selectedField = selectedField;
		if (this.selectedField != null) {
			this.selectedField.select();
		}
	}

	boolean isMyMove() {
		return myMove;
	}

	void setMyMove(boolean myMove) {
		this.myMove = myMove;
	}

	List<Field> getValidFields() {
		return validFields;
	}

	void setValidFields(List<Field> validFields) {
		this.validFields = validFields;
	}

	int getMaxMoveLength(){ return maxMoveLength; }

	void setMaxMoveLength(int m){
		maxMoveLength=m;
	}
	/**
	 * Calculates which fields are valid
	 * A field is valid if it contains a piece of this player and
	 * maximum number of opponents pieces can be captured starting form that piece
	 */
	boolean calculateValidFields() {
		List<Field> valid = new ArrayList<>();
		int max = 1;
		int thisFieldMax;

		for(int i = 0;i < BOARD_SIZE;i++) {
			for(int j = 0;j < BOARD_SIZE;j++) {
				if(board[i][j].getUser() != null && board[i][j].getUser().equals(getMe())) {
						thisFieldMax = maxLengthFrom(board[i][j]);
						if(thisFieldMax > max) {
							valid = new ArrayList<>();
							valid.add(board[i][j]);
							max = thisFieldMax;
						}
						else if(thisFieldMax == max) {
							valid.add(board[i][j]);
						}
				}
			}
		}

		if (valid.size() == 0) {
			return false;
		}

		setValidFields(valid);
		setMaxMoveLength(max);

		for(Field t:valid){
			System.out.println(t.getId());
		}
		System.out.println(max);

		return true;
	}

	int maxLengthFrom(Field f){
		int id = f.getId();
		List<Pair<Field, Field>> moves = getValidMoves(id);

		if(moves == null || moves.size() == 0)
			return 0;

		if(moves.stream().noneMatch(p -> p.second != null)) {
			return 1;
		}

		int currentLength;
		int max = 0;
		for(Pair<Field,Field> pair: moves){
			if(pair.second != null){

				Field eaten=pair.second;
				int idEaten=eaten.getId();
				Pair<Integer,Integer> coordinatesEaten = Field.getCoordinates(idEaten);
				IUser userEaten = eaten.getUser();
				boolean isPawnEaten = eaten.isPawn();
				eaten.removePiece();

				Pair<Integer,Integer> coordinatesFrom = Field.getCoordinates(id);
				IUser userFrom = f.getUser();
				boolean isPawnFrom = f.isPawn();
				f.removePiece();

				Pair<Integer,Integer> coordinatesTo = Field.getCoordinates(pair.first.getId());
				pair.first.setPiece(userFrom, isPawnFrom);
				int maxLengthFromThis = maxLengthFrom(pair.first);
				if(maxLengthFromThis == 1) {
					currentLength = 2;
				}
				else {
					currentLength = 2 + maxLengthFromThis;
				}

				if(currentLength > max) {
					max = currentLength;
				}

				board[coordinatesTo.first][coordinatesTo.second].removePiece();
				board[coordinatesEaten.first][coordinatesEaten.second].setPiece(userEaten, isPawnEaten);
				board[coordinatesFrom.first][coordinatesFrom.second].setPiece(userFrom, isPawnFrom);
			}
		}

		return max;
	}


	void sendMove(String move) {
		try {
			userDb.send(me, move);
		} catch (RemoteException ex) {
			window.reportError("Error while sending a message.", false, ex);
		}
	}

	void init(IUser me, IUser opponent, boolean blue) {
		this.me = me;
		this.isBlue = blue;

		setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));

		System.out.printf("==> Board init <================\n");

		for (int i = 0; i < BOARD_SIZE; ++i) {
			for (int j = 0; j < BOARD_SIZE; ++j) {
				if ((i + j) % 2 == 0) {
					board[i][j] = new Field(Board.this, -1, Color.LIGHT_GRAY, null, false);
				}
				else {
					int fieldId = (BOARD_SIZE * i + j) / 2 + 1;
					boolean orangeSide = fieldId <= BOARD_SIZE / 2 * (BOARD_SIZE / 2 - 1);
					boolean blueSide = fieldId > BOARD_SIZE / 2 * (BOARD_SIZE / 2 + 1);

					board[i][j] = new Field(
							Board.this,
							fieldId,
							Color.DARK_GRAY,
							orangeSide ? ((blue) ? opponent : me) : blueSide ? ((blue) ? me : opponent) : null,
							orangeSide || blueSide
					);
				}
				System.out.printf(" %2d", board[i][j].getId());
				add(board[i][j]);
			}
			System.out.println();
		}

		System.out.printf("================================\n");
	}

	private boolean areValidCoordinated(int i, int j) {
		return i >= 0 && i < BOARD_SIZE && j >= 0 && j < BOARD_SIZE;
	}

	/**
	 * Returns the list of fields that a player can legally move to from base field
	 * A field is paired with another filed if in the moving process it captures
	 * opponents piece.
	 * @param filedId Base field's id
	 * @return The list of pairs of fields where first field in a pair represents target
	 * field and the second field the opponents captured field. If no piece is captured,
	 * second field is null.
	 */
	List<Pair<Field, Field>> getValidMoves(int filedId) {
		List<Pair<Field, Field>> moves = new ArrayList<>();
		Pair<Integer, Integer> coordinates = Field.getCoordinates(filedId);
		int i = coordinates.first;
		int j = coordinates.second;

		if (areValidCoordinated(i, j) && board[i][j].getUser() != null) {

			for (int off = 0; off < DI.length; ++off) {
				int ii = i + DI[off];
				int jj = j + DJ[off];
				int pawnMoves = 1;
				Field opponent = null;
				while(areValidCoordinated(ii, jj)) {
					if (board[i][j].isPawn() && pawnMoves > 1) {
						break; // pawn can go only to neighbor or next to neighbor field
					}

					if (board[ii][jj].getUser() == null) {
						if (!board[i][j].isPawn() || // if queen it is valid to move in any direction
								opponent != null || // if pawn captures opponents piece, it can move in any direction
								(board[i][j].isBlue() && board[i][j].getId() > board[ii][jj].getId()) || // otherwise, it can move only forward
								(!board[i][j].isBlue()) && board[i][j].getId() < board[ii][jj].getId()) {
							moves.add(new Pair<>(board[ii][jj], opponent));
						}
						++pawnMoves;
					}
					else if (!board[ii][jj].getUser().equals(me) && opponent == null) {
						opponent = board[ii][jj]; // if neighbor is opponent try to capture him
					}
					else {
						break;
					}

					ii += DI[off];
					jj += DJ[off];
				}
			}
		}

		Board.printMoves(board[i][j], moves);

		return moves;
	}

	void selectPiece(int id) {
		System.out.printf("-> Opponent selected %d\n", id);
	}

	void movePiece(int from, int to, int capture) {
		System.out.printf("-> Opponent moved %d to %d, and captured %d\n", from, to, capture);

		Pair<Integer, Integer> fromCoord = Field.getCoordinates(from);
		int fromI = fromCoord.first;
		int fromJ = fromCoord.second;

		System.out.printf("----> From [%d, %d]\n", fromI, fromJ);

		if (!areValidCoordinated(fromI, fromJ)) {
			return;
		}

		Pair<Integer, Integer> toCoord = Field.getCoordinates(to);
		int toI = toCoord.first;
		int toJ = toCoord.second;

		System.out.printf("----> To [%d, %d]\n", toI, toJ);

		if (!areValidCoordinated(toI, toJ)) {
			return;
		}

		if (capture != -1) {
			Pair<Integer, Integer> captureCoord = Field.getCoordinates(capture);
			int captureI = captureCoord.first;
			int captureJ = captureCoord.second;

			System.out.printf("----> Captured [%d, %d]\n", captureI, captureJ);

			if (areValidCoordinated(captureI, captureJ)) {
				board[captureI][captureJ].removePiece();
			}
		}

		System.out.printf("----> Trying set piece\n");
		board[toI][toJ].setPiece(board[fromI][fromJ].getUser(), board[fromI][fromJ].isPawn());

		System.out.printf("----> Trying remove piece\n");
		board[fromI][fromJ].removePiece();

		System.out.printf("----> Success\n");
	}

	public static void printMoves(Field start, List<Pair<Field, Field>> moves) {
		System.out.println("===> Printing moves <=====================");

		if (moves == null) {
			System.out.println("\tNo moves");
		}
		else {
			for (Pair<Field, Field> move : moves) {
				System.out.printf("\t%d -%s-> %d\n", start.getId(), (move.second == null) ? "" : "(" + move.second.getId() + ")", move.first.getId());
			}
		}


		System.out.println("==========================================");
	}
}
