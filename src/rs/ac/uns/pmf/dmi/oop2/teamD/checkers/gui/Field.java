package rs.ac.uns.pmf.dmi.oop2.teamD.checkers.gui;

import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.user.IUser;
import rs.ac.uns.pmf.dmi.oop2.teamD.checkers.utility.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Inner class representing one field of the game board
 */
class Field extends JPanel {

	private int id;
	private Board board;
	private boolean pawn;
	private boolean blue;
	private JLabel label = new JLabel();
	private IUser user;
	private boolean canContinueMove;
	private int capturedId;

	/**
	 * Caluclates field id from coordinated i and j
	 * @param i
	 * @param j
	 * @return
	 */
	public static int getId(int i, int j) {
		if ((i + j) % 2 == 0) {
			return -1;
		}

		return (Board.BOARD_SIZE * i + j) / 2 + 1;
	}

	/**
	 * Calculate i and j coordinated on board
	 * @param id Field id
	 * @return First member of a pair is i, and second is j
	 */
	public static Pair<Integer, Integer> getCoordinates(int id) {
		return new Pair<>((id - 1) / (Board.BOARD_SIZE / 2),
				((id - 1) % (Board.BOARD_SIZE / 2)) * 2 + (((id - 1) % Board.BOARD_SIZE <= (Board.BOARD_SIZE / 2)) ? 1 : 0)
				);
	}

	Field(Board board, int id, Color color, IUser user, boolean pawn) {
		this.board = board;
		this.id = id;
		setBackground(color);
		setLayout(new GridBagLayout());
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setVerticalAlignment(JLabel.CENTER);
		add(label);
		setPiece(user, pawn);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!board.isMyMove())
					return;

				if (board.getSelectedField() == null) {
					if (trySelectField()) {
						System.out.printf("-> Field %d selected\n", id);
						board.sendMove("select " + id);
					}
				}
				else {
					Field selected = board.getSelectedField();
					if (tryMakeMove()) {
						if (canContinueMove) {
							// Force player to move this piece next time
							List<Field> newValidPieces = new ArrayList<>();
							newValidPieces.add(Field.this);
							board.setValidFields(newValidPieces);
							board.setMaxMoveLength(board.getMaxMoveLength() - 2);

							board.sendMove("move " + selected.id + " " + id + " " + capturedId);
						}
						else {
							tryPromote();
							board.sendMove("final " + selected.id + " " + id + " " + capturedId);
							board.setMyMove(false);
						}
					}
				}
			}
		});
	}

	/**
	 * Selects filed if this piece can take most of the opponents pieces
	 */
	private boolean trySelectField() {
		/*i assume this next comment can be integrated into that if*/
		if(user != null && user.equals(board.getMe()) && board.getValidFields().stream().anyMatch(f -> f.id == id)){
			board.setSelectedField(this);
			return true;
		}else
			return false;
	}

	private boolean tryMakeMove() {
		//board.calculateValidFields();

		List<Pair<Field, Field>> moves = board.getValidMoves(board.getSelectedField().id);
		// if possible, make valid move
		if (moves != null && moves.stream().anyMatch(pair -> pair.first.id == id)) {
			/*setPiece(board.getSelectedField().user, board.getSelectedField().pawn);
			board.getSelectedField().removePiece();
			board.setSelectedField(null);*/

			// Remove captured piece
			Field captured = moves.stream().filter(pair -> pair.first.id == id).findFirst().get().second;
			if (captured != null) {

				capturedId = captured.id;
				IUser capturedUser = captured.getUser();
				boolean capturedIsPawn = captured.isPawn();
				captured.removePiece();

				Field from=board.getSelectedField();
				Pair<Integer,Integer> coordinatesFrom = Field.getCoordinates(from.getId());
				IUser userFrom = from.getUser();
				boolean isPawnFrom = from.isPawn();
				from.removePiece();

				this.setPiece(userFrom, isPawnFrom);

				int maxFromThis = board.maxLengthFrom(this);

				if((board.getMaxMoveLength() - 2 == maxFromThis) && maxFromThis != 0){
					canContinueMove = true;
					//board.setSelectedField(this);
				}
				else if((board.getMaxMoveLength() - 2 == maxFromThis) && maxFromThis == 0){
					canContinueMove=false;
				}
				else {
					this.removePiece();
					captured.setPiece(capturedUser,capturedIsPawn);
					from.setPiece(userFrom,isPawnFrom);
					return false;
				}


				/*List<Pair<Field, Field>> nextMoves = board.getValidMoves(id);
				canContinueMove = nextMoves != null && nextMoves.size() > 0;*/
			}
			else if(captured == null && board.getMaxMoveLength() == 1) {
				capturedId = -1;
				canContinueMove = false;
				setPiece(board.getSelectedField().user, board.getSelectedField().pawn);
				board.getSelectedField().removePiece();
				board.setSelectedField(null);
				return true;
			}
			else {
				capturedId = -1;
				canContinueMove = false;
				return false;
			}

			if (canContinueMove) {
				board.setSelectedField(Field.this);
			}
			return true;
		}
		else if (user != null) {
			// if occupied field, remove selection
			board.setSelectedField(null);
		}

		return false;
	}

	/**
	 * Promote pawn to queen if end line reached
	 */
	private boolean tryPromote() {
		if(id >= 1 && id <= 5 && isBlue()){
			setBlueQueen();
			return true;
		} else if (id>=45 && id <=50 && !isBlue()){
			setOrangeQueen();
			return true;
		}
		return false;
	}

	public boolean isBlue() {
		return blue;
	}

	public void setBlue() {
		if (user != null) {
			blue = (user.equals(board.getMe()) && board.isBlue()) || (!user.equals(board.getMe()) && !board.isBlue());
		}
	}

	void setPiece(IUser user, boolean pawn) {
		this.user = user;
		this.pawn = pawn;
		setBlue();

		if (tryPromote()) {
			return;
		}

		if (user != null) {
			if (isBlue() && isPawn()) {
				setBluePawn();
			}
			else if (!isBlue() && isPawn()) {
				setOrangePawn();
			}
			else if (isBlue() && !isPawn()) {
				setBlueQueen();
			}
			else {
				setOrangeQueen();
			}
		}
	}

	private void setBluePawn() {
		label.setIcon(board.getBluePawn());
	}

	private void setOrangePawn() {
		label.setIcon(board.getOrangePawn());
	}

	private void setBlueQueen(){
		pawn = false;
		label.setIcon(board.getBlueQueen());
	}

	private void setOrangeQueen() {
		pawn = false;
		label.setIcon(board.getOrangeQueen());
	}

	void removePiece(){
		user = null;
		label.setIcon(null);
	}

	public boolean isPawn(){
		return pawn;
	}

	public int getId(){
		return id;
	}

	public IUser getUser(){
		return user;
	}
}
