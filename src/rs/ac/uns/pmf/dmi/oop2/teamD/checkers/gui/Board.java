package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui;

import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.server.IUserDb;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.IUser;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.utility.Pair;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JPanel representing checkers board with all of its logic
 */
class Board extends JPanel {

	public static final int BOARD_SIZE = 10;

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
		int max=Integer.MIN_VALUE;
		for(int i=0; i<BOARD_SIZE; i++){
			for(int j=0; j<BOARD_SIZE; j++) {
				Field root = board[i][j];
				if (root.isPawn()) {
					int rootId = getId(i, j);
					int maxx=calculateValidFields(root, rootId).first;
					Field valid=calculateValidFields(root,rootId).second;
					int count=0;
					if(maxx > max) {
						max = maxx;
						if(validFields != null) {
							validFields.remove(count);
						}
						validFields.add(valid);
						count++;
					}
					else if(maxx == max){
						validFields.add(valid);
					}
				}
			}
		}
	}

	private Pair<Integer, Field> calculateValidFields(Field root,int rootId){
		int leftId=rootId+5;
		int rightId=rootId+6;
		int max=1;
		if(root != null) {
			max++;
			List<Pair<Field, Field>> validMoves = getValidMoves(rootId);
			List<Pair<Field, Field>> left = new ArrayList<>();
			left.add(validMoves.get(0));
			Pair<Integer, Field> validLeft = calculateValidFields(left.get(0).first, leftId);
			List<Pair<Field, Field>> right = new ArrayList<>();
			right.add(validMoves.get(1));
			Pair<Integer, Field> validRight = calculateValidFields(right.get(0).second, rightId);
			if (validLeft.first > validRight.first) {
				return validLeft;
			}
		}
		return null;
	}

	public int getId(int x, int y){
		return (x * 10 + y) / 2;
	}

	void sendMove(String move) {
		try {
			userDb.send(me, move);
		} catch (RemoteException ex) {
			window.reportError("Error while sending a message.", false, ex);
		}
	}

	void init(IUser me, IUser opponent, boolean isBlue) {
		this.me = me;
		this.isBlue = isBlue;

		setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));

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
							orangeSide ? ((isBlue) ? opponent : me) : blueSide ? ((isBlue) ? me : opponent) : null,
							orangeSide || blueSide
					);
				}
				add(board[i][j]);
			}
		}
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
		List<Pair<Field,Field>> validMoves = new ArrayList<>();
		int x=0;
		int y=0;
		for(int i=0; i<BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (getId(i, j) == filedId) {
					x = i;
					y = j;
					break;
				}
			}
		}

		int[] di = { 1, -1, 1, -1};
		int[] dj = { 1, 1, -1, -1};

		if (!board[x+1][y+1].isPawn()) {
			validMoves.add(new Pair<>(board[x+1][y+1], null));
		}
		else{
			getValidMoves(getId(x+1,y+1));
		}
		if(!board[x+1][y-1].isPawn()){
			validMoves.add(new Pair<>(board[x+1][y-1], null));
		}
		else{
			getValidMoves(getId(x+1,y-1));
		}
		if(!board[x-1][y-1].isPawn()){
			validMoves.add(new Pair<>(board[x-1][y-1], null));
		}
		else{
			getValidMoves(getId(x-1,y-1));
		}
		if(!board[x-1][y+1].isPawn()){
			validMoves.add(new Pair<>(board[x-1][y+1], null));
		}
		else{
			getValidMoves(getId(x-1,y+1));
		}
		return validMoves;
	}
}
