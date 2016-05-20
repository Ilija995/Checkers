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
	private IUserDb userDb;
	private Field selectedField;
	private boolean isBlue;
	private IUser me;
	private boolean myMove;
	private List<Field> validFields;

	Board(IUserDb userDb, CheckersWindow window) {
		this.userDb = userDb;
		this.window = window;

		bluePawn = new ImageIcon("res\\blue.png");
		orangePawn = new ImageIcon("res\\orange.png");
		blueQueen = new ImageIcon("res\\blueQ.png");
		orangeQueen = new ImageIcon("res\\orangeQ.png");
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
		this.selectedField = selectedField;
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

	/**
	 * Calculates which fields are valid
	 * A field is valid if it contains a piece of this player and
	 * maximum number of opponents pieces can be captured starting form that piece
	 */
	void calculateValidFields() {
		List<Field> valid=new ArrayList<>();
		int k=0;
		int max=1;
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;i++){
				if(board[i][j].getUser().equals(board.getMe())){
					k=maxlengthFrom(board[i][j]);
					if(k>max){
						ListIterator<Field> li=valid.listIterator();
						while(li.hasNext()){
							li.remove();
						}
						valid.add(board[i][j]);
					}else if(k==max){
						valid.add(board[i][j]);
					}
				}
			}
		}
		setValidFields(valid);
	}
	int maxlengthFrom(Field f){
		int r=0;
		int max=0;
		int id=f.getId();
		List<Pair<Field, Field>> moves = getValidMoves(id);
		if(moves==null) return 0;
		else{
			if(moves.stream().noneMatch(p->p.second!=null)){
				return 1;
			}else{
				for(Pair<Field,Field> pair: moves){
					if(pair.second!=null){
						r=2+maxlengthFrom(pair.second);
						if(r>max) {
							max = r;
							r=0;
						}
						else r=0;
					}
				}
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
