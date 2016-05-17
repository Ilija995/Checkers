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
	private boolean isPawn;
	private JLabel label = new JLabel();
	private IUser user;
	private boolean canContinueMove;

	Field(Board board, int id, Color color, IUser user, boolean isPawn) {
		this.board = board;
		this.id = id;
		setBackground(color);
		add(label);
		this.user = user;
		this.isPawn = isPawn;

		setPiece();

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!board.isMyMove())
					return;

				if (board.getSelectedField() == null) {
					if (trySelectField()) {
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

							board.sendMove("move " + selected.id + " " + id);
						}
						else {
							tryPromote();
							board.sendMove("final " + selected.id + " " + id);
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
		boolean selected=false;
		if(!board.isBlue()) {
			selected = true;
			board.setSelectedField(this);
		}
		return selected;
	}

	private boolean tryMakeMove() {
		List<Pair<Field, Field>> moves = board.getValidMoves(board.getSelectedField().id);

		// if possible, make valid move
		if (moves != null && moves.stream().anyMatch(pair -> pair.first.id == id)) {
			isPawn = board.getSelectedField().isPawn;
			setPiece();
			board.getSelectedField().removePiece();

			// Remove captured piece
			Field captured = moves.stream().filter(pair -> pair.first.id == id).findFirst().get().second;
			if (captured != null) {
				captured.removePiece();

				List<Pair<Field, Field>> nextMoves = board.getValidMoves(id);
				canContinueMove = nextMoves != null && nextMoves.size() > 0;
			}
			else {
				canContinueMove = false;
			}

			board.setSelectedField(Field.this);
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
	private void tryPromote() {
		setQueen();
	}

	private void setPiece() {
		if (isPawn) {
			setPawn();
		}
		else {
			setQueen();
		}
	}

	private void setPawn() {
		if ((user.equals(board.getMe()) && board.isBlue()) || (!user.equals(board.getMe()) && !board.isBlue())) {
			setBluePawn();
		}
		else if ((user.equals(board.getMe()) && !board.isBlue()) || (!user.equals(board.getMe()) && board.isBlue())) {
			setOrangePawn();
		}
	}

	private void setQueen() {
		Field selected = board.getSelectedField();
		List<Pair<Field,Field>> validMoves=board.getValidMoves(selected.id);
		if(user.equals(board.getMe()) && board.isBlue() && validMoves.stream().anyMatch(pair->pair.first.id==(selected.id+9)) ){
			setBlueQueen();
		}
		else if(user.equals(board.getMe()) && !board.isBlue() && validMoves.stream().anyMatch(pair->pair.first.id==(selected.id+9)) ){
			setOrangeQueen();
		}
	}

	private void setBluePawn(){
		label.setIcon(board.getBluePawn());
	}

	private void setOrangePawn() {
		label.setIcon(board.getOrangePawn());
	}

	public void setBlueQueen(){
		label.setIcon(board.getBlueQueen());
	}

	public void setOrangeQueen() {
		label.setIcon(board.getOrangeQueen());
	}

	private void removePiece(){
		user = null;
		label.setIcon(null);
	}

}
