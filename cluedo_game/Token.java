// Josh King - 16200099
// George Ridgway - 16200132
// Kelsey Osos - 16201972

package cluedo_game;

public class Token {
	private String name;
	private int playerNumber;
	private int[] position = new int[2]; //holding the position of the player
	private String locationAsString;
	private BoardSquare squareOn;
	private Room inRoom;

	// Variable to help with circularly linked list traversal
	private Token next;

	//Constructor
	public Token(int x, int y, String name, int playerNumber) {
		this.position[0] = x;
		this.position[1] = y;

		this.name = name;
		this.playerNumber = playerNumber;
		this.inRoom = null;
		// This is set to the spawn point when the board is created
	}

	/*
	These need to be moved to a GameEngine class
	 */
	public void enterRoom(Room room){
		this.position[0] = -1;
		this.position[1] = -1;
		this.squareOn = null;
		this.inRoom = room;
		this.setLocationAsString("room");
	}
	public void exitRoom(BoardSquare exitToSquare){
		int pos[] = exitToSquare.getPosition();
		this.position[0] = pos[0];
		this.position[1] = pos[1];
		this.squareOn = exitToSquare;
		this.inRoom = null;
	}

	//
	//Accessors
	//
	public String getName() { return name;}
	public int getPlayerNumber() {return playerNumber;}
	public int[] getPosition() {return position;}
	public String getLocationAsString() {
		if(inRoom != null)
			return "room";
		return locationAsString;
	}
	public BoardSquare getSquareOn() {
		return squareOn;
	}
	public Room getInRoom() { return inRoom; }
	public Token next() {
		return next;
	}

  	//
	//Mutators
	//
	public void setName(String name) {this.name = name;}
	public void setPlayerNumber(int playerNumber) {this.playerNumber = playerNumber;}
	public void setPosition(int[] position) {this.position = position;}
	public void setLocationAsString(String location) {this.locationAsString = location; }
	public void setSquareOn(BoardSquare squareOn) {
		this.squareOn = squareOn;
		this.setLocationAsString(squareOn.toString());
	}
	public void setInRoom(Room room) { this.inRoom = room; }
	public void setNext(Token next) {
		this.next = next;
	}
}
