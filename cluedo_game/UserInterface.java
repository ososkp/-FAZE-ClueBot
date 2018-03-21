package cluedo_game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * UserInterface
 * This class is the JFrame that will hold all of the individual JPanels:
 *  Game Board
 *  Input Box
 *  Text Display Box
 */
public class UserInterface extends JPanel {
    // Frame which will contain the GUI
    private JFrame display = new JFrame();
    // The user input portion of the display
    // First it is built into 'in', then it is loaded into the 'input' JPanel
    private UserInputBox in = new UserInputBox();
    private JPanel input = in.createInputPanel();
    private JButton startGameButton;

    // Text output portion of the display, generated in the same way as user input
    private OutputTextDisplay out = new OutputTextDisplay();
    private JPanel output = out.createOutputPanel();

    // The board image portion of the UI
    JPanel boardImagePanel;
    BoardImage myImg;

    // The guessing panel, which replaces the input panel
//    private JPanel guess = in.createGuessPanel();

    // The overall display panel that will control layout of the 3 panels
    private JPanel userDisplay = new JPanel();

    // Pointer to player whose turn it is. When we add 'turns', the turn object will send info to this
    private Token currentPlayer;
    private Tokens playerList;

    /**
     * The constructor for the UI which will set off a chain of events drawing all of the components
     * Everything so far is done in buildGUI, but when we add game logic it will also(?) be contained here
     */
    public UserInterface(Tokens playerList) {
        /* This is going to happen AFTER the start game button is pressed */
        this.playerList = playerList;
        this.currentPlayer = playerList.getFirst();
        this.buildGUI();
    }

    /**
     * buildGui creates the graphical aspect of the UI
     */
    public void buildGUI() {
        // Set frame size to house JPanels
        display.setSize(800, 700);
        display.setTitle("Cluedo");
        display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // BorderLayout for overall JPanel
        userDisplay.setLayout(new BorderLayout());
        // Add the input and output panels in the appropriate positions
        userDisplay.add(input, BorderLayout.SOUTH);
        userDisplay.add(output, BorderLayout.EAST);
        /* Setting the board */
        myImg = new BoardImage();
        boardImagePanel = myImg.returnPanel();
        boardImagePanel = myImg.refreshMe();

        userDisplay.add(boardImagePanel);

        // Add formatted JPanel to the frame
        display.add(userDisplay);

        /*
        TODO: Not going to use this, but I'm saving it in case we need to reference it for movement
        */
        // DELETE
//        JPanel movementPanel = movementUpdate();
//        JOptionPane.showConfirmDialog(this, "Demonstrating Movement....");
//        userDisplay.remove(boardImagePanel);
//        userDisplay.add(movementPanel);
//        display.invalidate();
//        display.validate();
//        display.repaint();

        // Make the UI visible
        display.setVisible(true);
    }

    public void refreshDisplayForNextTurn(Token p) {
        // Tell players whose turn it is
        in.whoseTurnLabel.setText("     It is now " + p.getName() + "'s turn. Moves Left: " + GameLogic.getMovesLeft());
        // Update what player is allowed to input
        out.updateAllowedCommandsBasedOnSquare(p);
    }

    /**
     * The user input portion of the GUI
     */
    private class UserInputBox {
        final int FIELD_WIDTH = 10;
        private JTextField inputField = new JTextField(FIELD_WIDTH);
        private JLabel whoseTurnLabel = new JLabel("     Welcome to Cluedo");
        private JLabel promptLabel = new JLabel("     What would you like to do?");
        private JButton performActionButton;
        private JButton exitChoiceButton;
        private JButton viewNotesButton;
        private UserInputListener returnPressListener;
        private ExitChoiceListener returnPressExitListener;
        private ViewNotesListener returnPressViewNotesListener;

        public JPanel createInputPanel() {
            JPanel input = new JPanel();
            input.setLayout(new BorderLayout());
            JButton startGameButton = createStartGameButton();

            input.setBorder(BorderFactory.createEtchedBorder(Color.lightGray, Color.black));

            // Making it so user can press 'return' to 'Perform Action'
            returnPressListener = new UserInputListener();
            inputField.addActionListener(returnPressListener);

            returnPressExitListener = new ExitChoiceListener();
            returnPressViewNotesListener = new ViewNotesListener();

            input.add(whoseTurnLabel, BorderLayout.NORTH);
            input.add(promptLabel, BorderLayout.CENTER);
            input.add(inputField, BorderLayout.SOUTH);
            input.add(startGameButton, BorderLayout.EAST);

            inputField.requestFocus();

            return input;
        }

        /**
         * A button that must be pressed to start the game
         *
         * @return the button, to place into a JPanel
         */
        private JButton createStartGameButton() {
            startGameButton = new JButton("Start Game");
            ActionListener listener = new StartGameListener();
            startGameButton.addActionListener(listener);

            return startGameButton;
        }

        class StartGameListener implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                GameLogic.Dice.rollDice();
                input.remove(startGameButton);
                performActionButton = createPerformActionButton();
                input.add(performActionButton, BorderLayout.EAST);
                whoseTurnLabel.setText("     It is now " + currentPlayer.getName() + "'s turn. Moves Left: "
                        + GameLogic.getMovesLeft());
                out.updateAllowedCommandsBasedOnSquare(currentPlayer);
                inputField.setText("");
                input.revalidate();
                output.revalidate();
            }
        }

        /**
         * Button for the user to press when they enter a command
         *
         * @return the button, to place into a JPanel
         */
        private JButton createPerformActionButton() {
            JButton performAction = new JButton("Perform Action");
            ActionListener listener = new UserInputListener();
            performAction.addActionListener(listener);

            return performAction;
        }

        /*
         * This ActionListener reads when somebody presses the "Perform Action" button
         */
        class UserInputListener implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                String result = GameLogic.PlayerEntry.ActionPerformer(currentPlayer, inputField.getText());

                // If user did not enter an appropriate command, show a JOptionPane telling
                // them to reenter the command then clear the input box.
                if (!GameLogic.PlayerEntry.getCommandSuccessful()) {
                    JOptionPane.showMessageDialog(null, result);
                }

                /* If the user wants to get helpful hints */
                if (result.equals("help")) {
                	System.out.println("I am working on this now!");
                }

                if (result.equals("notes") || result.equals("cheat")){
                    switchToViewNotes(result);
                }
                else {
                    if (currentPlayer.getLocationAsString().equals("room")) {
                        switch (result) {
                            // If player has chosen to exit a room, bring up the appropriate prompt if necessary
                            case "exitChoice":
                                /*
                                Change input and output to handle user input of an exit choice
                                 */
                                switchToExitChoiceButton();
                                return;
                            case "exit":
                                result = (currentPlayer.getName() + " has exited the room.");
                                break;
                            // If player is making a guess, enter the appropriate menu
                            case "Guess Prompt":
//                                JOptionPane.showConfirmDialog(null, "This is a placeholder panel for guessing.");
                                result = currentPlayer.getName() + " is making a guess.";
                                break;
                            case "passage":
                                break;
                        }
                    }

                    // If the turn was successful, cycle to next turn
                    if (GameLogic.PlayerEntry.wasTurnSuccessful()) {
                        if(result.equals("done")){
                            out.updateMoveHistory(currentPlayer.getName() + " has finished the turn early.");
                        }
                        else {
                            out.updateMoveHistory(result);
                            if (currentPlayer.getInRoom() == null) {
                                System.out.println("Player:\t" + currentPlayer.getName() + "\tAction: " + inputField.getText()
                                        + "\t\tNew Location: " + currentPlayer.getSquareOn().getPositionAsString());

                           int[] destinationCoordinates, currentCoordinates;
                          // JPanel panelAfterPlayerMove = null;
                          userDisplay.remove(boardImagePanel);
                           switch (inputField.getText()) {
                               case "up"://Since the player has already moved, I need to get the sqare of the undo operation
                               case "u":
                                   destinationCoordinates = currentPlayer.getSquareOn().getPosition();
                                   currentCoordinates = currentPlayer.getSquareOn().getBelow().getPosition();
                                   boardImagePanel = myImg.move(currentCoordinates[0], currentCoordinates[1], destinationCoordinates[0], destinationCoordinates[1]);
                                   break;
                               case "down":
                               case "d":
                                    destinationCoordinates = currentPlayer.getSquareOn().getPosition();
                                    currentCoordinates = currentPlayer.getSquareOn().getAbove().getPosition();
                                    boardImagePanel = myImg.move(currentCoordinates[0], currentCoordinates[1], destinationCoordinates[0], destinationCoordinates[1]);
                                   break;
                               case "left":
                               case "l":
                                    destinationCoordinates = currentPlayer.getSquareOn().getPosition();
                                    currentCoordinates = currentPlayer.getSquareOn().getRight().getPosition();
                                    boardImagePanel = myImg.move(currentCoordinates[0], currentCoordinates[1], destinationCoordinates[0], destinationCoordinates[1]);
                                    break;
                               case "right":
                               case "r":
                                    destinationCoordinates = currentPlayer.getSquareOn().getPosition();
                                    currentCoordinates = currentPlayer.getSquareOn().getLeft().getPosition();
                                    boardImagePanel = myImg.move(currentCoordinates[0], currentCoordinates[1], destinationCoordinates[0], destinationCoordinates[1]);
                                    break;
                               default:
                                   destinationCoordinates = new int[2];
                                   System.out.println("No direction detected ERROR");
                                   break;
                           }

                           System.out.println("Destination Coords: " + destinationCoordinates[0]);
                           // TODO: Josh plz fix below -- is fixed

                          // int[] currentPlayergetPositionArray = currentPlayer.getPosition();
                        //    System.out.println("Moving from "+ currentPlayergetPositionArray[0] + ","+currentPlayergetPositionArray[1] + " to " + destinationCoordinates[0] + "," + destinationCoordinates[1]);
                           //boardImagePanel = movePlayerAndUpdate(currentPlayer.getPosition(), destinationCoordinates);
                           userDisplay.add(boardImagePanel);
                           display.invalidate();
                           display.validate();
                           display.repaint();
                        //    boardImagePanel.revalidate();
                            }
                            else {

                                //I think this is the right place
                                userDisplay.remove(boardImagePanel);
                                boardImagePanel = myImg.move(currentPlayer.getInRoom().getName(), currentPlayer.getName());
                                userDisplay.add(boardImagePanel);
                                display.invalidate();
                                display.validate();
                                display.repaint();

                                // Print action and location to system out
                                System.out.println("Player:\t" + currentPlayer.getName() + "\tAction: " + inputField.getText()
                                        + "\t\tNew Location: " + currentPlayer.getInRoom().getName());
                                // Only update move history with player's room if they aren't making a guess
                                    // Otherwise it'll say they're in the room multiple times.
                                if (!result.equals(currentPlayer.getName() + " is making a guess."))
                                    out.updateMoveHistory(currentPlayer.getName()
                                        + " has entered the " + currentPlayer.getInRoom().getName());
                            }
                        }

                        // Switch player if the turn is over (or if they had entered 'done'
                        if (GameLogic.getMovesLeft() == 0) {
                            currentPlayer = currentPlayer.next();
                            // Update move history to show new turn and where the player is.
                            //      This will be less useful when GUI works
                            out.updateMoveHistory("It is now " + currentPlayer.getName() + "'s turn. Location: "
                                    + currentPlayer.safeGetLocation());
                            // Roll the dice for the next player
                            GameLogic.Dice.rollDice();
                        }

                        // Update input display with that player
                        refreshDisplayForNextTurn(currentPlayer);
                    }
                    // If not, show error and do not cycle to next turn
                        // Error doesn't show if player viewed notes
                    else if (!(result.equals("notes") || result.equals("cheat") || result.equals("help"))){
                        // This will be an error message if move was unsuccessful
                    	JOptionPane.showMessageDialog(null, result);
                    }
                }
                GameLogic.PlayerEntry.resetSwitches();
                inputField.setText("");
                inputField.requestFocus();
            }
        }

        public void switchToExitChoiceButton(){
            switchInputToExitPicker();
            out.roomExitChoicesUpdater();
            input.remove(performActionButton);
            input.add(createExitPickerButton(), BorderLayout.EAST);
            inputField.setText("");

            input.revalidate();
            output.revalidate();
        }

        private JButton createExitPickerButton(){
            exitChoiceButton = new JButton("Choose Exit");
            ActionListener listener = new ExitChoiceListener();
            exitChoiceButton.addActionListener(listener);

            return exitChoiceButton;
        }

        class ExitChoiceListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = -1;
                // Check to ensure the entry was an integer
                try {
                    choice = Integer.valueOf(inputField.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Please enter only an integer value");
                }
                // Call method in GameLogic to see if entry was valid for the number of exits
                GameLogic.PlayerEntry.checkRoomExit(currentPlayer, choice);

                // The checkRoomExit method switches 'roomExitCheck' to true if successful
                if (GameLogic.PlayerEntry.getRoomExitCheck()) {

                    userDisplay.remove(boardImagePanel);
                    System.out.println("Would exit here");
                    //boardImagePanel = myImg.movetoExit(currentPlayer.getInRoom().getName(), choice, currentPlayer.getName());
                    userDisplay.add(boardImagePanel);
                    display.invalidate();
                    display.validate();
                    display.repaint();

                    out.updateMoveHistory(currentPlayer.getName() + " has exited the room.");
                    switchToInput(returnPressExitListener, exitChoiceButton);
                    if(currentPlayer.getInRoom() == null) {
                        System.out.println("Player:\t" + currentPlayer.getName() + "\tAction: Exit " + choice
                                + "\t\tNew Location: " + currentPlayer.getSquareOn().getPositionAsString());
                    }
                }
            }
        }

        /**
         * This method changes the user input panel to reflect choice for exiting a room
         */
        public void switchInputToExitPicker() {
            input.remove(promptLabel);
            inputField.removeActionListener(returnPressListener);
            inputField.addActionListener(returnPressExitListener);
            promptLabel.setText("     Which exit would you like to take?");
            input.add(promptLabel, BorderLayout.CENTER);
        }

        /**
         * This method switches back to general user input
         *  It can be called to remove panels for either 'exit' or 'notes'/'cheat'
         */
        public void switchToInput(ActionListener al, JButton button) {
            inputField.removeActionListener(al);
            inputField.addActionListener(returnPressListener);
            in.inputField.setText("");
            in.inputField.requestFocus();
            promptLabel.setText("     What would you like to do?");
            input.remove(button);
            input.add(performActionButton, BorderLayout.EAST);
            input.revalidate();
            refreshDisplayForNextTurn(currentPlayer);
        }

        public void switchToViewNotes(String in){
            switchInputToViewNotes();
            out.viewNotes(in);
            input.remove(performActionButton);
            input.add(createViewNotesButton(), BorderLayout.EAST);
            inputField.setText("");

            input.revalidate();
            output.revalidate();
        }

        // TODO: NOT WORKING
        private void switchInputToViewNotes(){
            input.remove(promptLabel);
            inputField.removeActionListener(returnPressListener);
            inputField.addActionListener(returnPressViewNotesListener);
            promptLabel.setText("     You are viewing your notes.");
            input.add(promptLabel, BorderLayout.CENTER);
            input.revalidate();
        }

        private JButton createViewNotesButton(){
            viewNotesButton = new JButton("Done");
            ActionListener listener = new ViewNotesListener();
            viewNotesButton.addActionListener(listener);

            return viewNotesButton;
        }

        class ViewNotesListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                out.endViewNotes();
                switchToInput(returnPressViewNotesListener, viewNotesButton);
            }
        }
    }

    // So GameLogic can clear this field for guessing and room exit
    public void clearInputField() {
        in.inputField.setText("");
        in.inputField.requestFocus();
    }

    /**
     * The user output portion of the GUI
     */
    public class OutputTextDisplay {
        JTextArea moveHistory;
        JScrollPane scroller;
        JScrollPane notesScroller;
        JPanel allowedCommandsDisplay;
        JLabel locationReadout;
        JPanel possibleCommandsList;

        public OutputTextDisplay() {
            moveHistory = new JTextArea("", 10, 15);
            moveHistory.setEnabled(false);
            moveHistory.setLineWrap(true);
            moveHistory.setForeground(Color.BLACK);

            createAllowedCommandsDisplay();

            /*
            A static part of the output text display that will update based on where the player is,
            but stays in the same place at the top of that JPanel.
             */
            locationReadout = new JLabel("<html>Welcome to Cluedo!<br/>Possible Commands:</html>");
            locationReadout.setForeground(Color.white);
            locationReadout.setHorizontalAlignment(JLabel.CENTER);
            allowedCommandsDisplay.add(locationReadout, BorderLayout.NORTH);

            /*
            This portion of the output text display is refreshed every turn to display appropriate commands
            based on whose turn it is and where they are.
             */
            possibleCommandsList = new JPanel();
            possibleCommandsList.setBackground(Color.GRAY);
            possibleCommandsList.setLayout(new GridLayout(4, 1));
            possibleCommandsList.setBorder(BorderFactory.createEtchedBorder(Color.BLACK, Color.BLACK));
            allowedCommandsDisplay.add(possibleCommandsList, BorderLayout.CENTER);

            allowedCommandsDisplay.revalidate();

            /*
            The move history will be in a scrollable window
             */
            scroller = new JScrollPane(moveHistory);
        }

        public void createAllowedCommandsDisplay() {
            allowedCommandsDisplay = new JPanel();
            allowedCommandsDisplay.setBorder(BorderFactory.createEtchedBorder(Color.BLACK, Color.BLACK));
            allowedCommandsDisplay.setLayout(new BorderLayout());
            allowedCommandsDisplay.setOpaque(true);
            allowedCommandsDisplay.setBackground(Color.GRAY);
            allowedCommandsDisplay.setForeground(Color.white);
        }

        /**
         * updateAllowedCommandsBasedOnSquare
         * When a new player's turn starts, this method ensures the output text display updates correctly
         * according to what type of square they are on.
         *
         * @param p the player whose turn it is
         */
        public void updateAllowedCommandsBasedOnSquare(Token p) {
            // The text in the readout depends on what square/room the player is on
            // p == null is for testing (hopefully), won't be in the game
            allowedCommandsDisplay.remove(possibleCommandsList);
            possibleCommandsList.removeAll();

            if (p == null)
                locationReadout.setText("Not on the board. Testing?");
            else if (p.getSquareOn() instanceof FloorSquare)
                locationReadout.setText("<html>You are on a Floor square.<br/>Possible Commands:</html>");
                // This will only be accessed after a player exits the room
            else if (p.getSquareOn() instanceof WallSquare)
                locationReadout.setText("Wall Square? Something went wrong...");
            else
                locationReadout.setText("<html>You are in the " + p.getInRoom().getName()
                        + "<br/>Possible Commands:</html>");

            try {
                if (p == null)
                    throw new Exception("Player not found error");
                if (p.getInRoom() == null) {
                    possibleCommandsList.setLayout(new GridLayout(4, 1));
                    // If player is on a square, get the type of square and show their available
                    // commands based on what is available from that square.
                    switch (p.getLocationAsString()) {
                        case "floor":
                            for (String s : AcceptedUserInputs.getFloorNavigation()) {
                                JLabel d = new JLabel(s);
                                d.setForeground(Color.yellow);
                                d.setHorizontalAlignment(JLabel.CENTER);
                                possibleCommandsList.add(d);
                            }
                            break;
                        case "wall":
                            throw new Exception("Player is on a wall square.");
                        default:
                            // This case should never happen
                            throw new Exception("Error Finding Square Type");

                    }
                }
                else {
                    possibleCommandsList.setLayout(new GridLayout(3, 1));
                    ArrayList<String> options = AcceptedUserInputs.getRoomNavigation();
                    for (String s : options) {
                        JLabel d = new JLabel(s);
                        d.setForeground(Color.yellow);
                        d.setHorizontalAlignment(JLabel.CENTER);
                        possibleCommandsList.add(d);
                    }
                }
            } catch (Exception e) {
                e.getMessage();
            }

            possibleCommandsList.repaint();
            allowedCommandsDisplay.add(possibleCommandsList);
            allowedCommandsDisplay.revalidate();
        }

        public void roomExitChoicesUpdater(){
            allowedCommandsDisplay.remove(possibleCommandsList);
            possibleCommandsList.removeAll();

            ArrayList<Integer> choices = AcceptedUserInputs.getRoomExits(currentPlayer.getInRoom());

            possibleCommandsList.setLayout(new GridLayout(choices.size(), 1));

            for (Integer i : choices) {
                JLabel d = new JLabel("Exit " + i);
                possibleCommandsList.add(d);
                d.setForeground(Color.green);
                d.setHorizontalAlignment(JLabel.CENTER);
            }

            possibleCommandsList.revalidate();

            allowedCommandsDisplay.add(possibleCommandsList);
            allowedCommandsDisplay.updateUI();
        }

        /**
         * IMPORTANT
         * update method is called BEFORE THE GAME LOGIC HAPPENS
         * So when we call the room, or the player's square, or anything else,
         * WE CALL APPROPRIATE VARIABLES BASED ON WHERE THE PLAYER WAS AT THE START OF THEIR TURN
         *
         * @param in String created by PlayerMovementHandler (in GameLogic.PlayerEntry)
         */
        public void updateMoveHistory(String in) {
            moveHistory.append(in);
            moveHistory.append("\n\n");

            // Refresh the panel after updating
            output.revalidate();
        }

        public JPanel createOutputPanel() {
            JPanel output = new JPanel();
            output.setLayout(new GridLayout(2, 1));
            output.add(scroller);
            output.add(allowedCommandsDisplay);

            return output;
        }

        // TODO: Readouts for 'notes' and 'cheat'
        public void viewNotes(String entry){
            JTextArea notes = new JTextArea("", 10, 15);
            notes.setBackground(Color.BLACK);
            notes.setForeground(Color.WHITE);
            if(entry.equals("notes")) {
                for (String s : currentPlayer.getNotes())
                    notes.append(s + "\n");
            }
            else if(entry.equals("cheat")){
                notes.append("CHEATER!!!\n");
                for (Card c : GameLogic.deck.getMurderEnvelope())
                    notes.append(c.toString() + "\n");
            }

            notesScroller = new JScrollPane(notes);
            output.removeAll();
            output.setLayout(new GridLayout(1,1));
            output.add(notesScroller);
            output.revalidate();
            userDisplay.repaint();
        }
        public void endViewNotes(){
            output.remove(notesScroller);
            output.setLayout(new GridLayout(2, 1));
            output.add(scroller);
            output.add(allowedCommandsDisplay);
            output.repaint();
        }
    }

    /**
     * The graphical portion of the GUI
     */
    public JPanel boardImagePanel() {

        BufferedImage bi = null;
        BoardImage boardimage = new BoardImage();

        try {
            bi = attemptToLoadImageFromResourceFolder();
        } catch (Exception resourceLoadException) {
            resourceLoadException.printStackTrace();
        }

        return boardimage.returnPanel(bi);
    }

    public void setBoardImagePanel(JPanel panel) {
        this.boardImagePanel = panel;
    }

    // DELETE
    // /*
    // This is a tester class for movement
    //  */
    // public JPanel movementUpdate() {
    //     BufferedImage bi = null;
    //     BoardImage boardimage = new BoardImage();
    //
    //     /*
    //     This version is a hopefully more mobile version of the image loading method
    //      */
    //     try {
    //         bi = attemptToLoadImageFromResourceFolder();
    //     } catch (Exception resourceLoadException) {
    //         resourceLoadException.printStackTrace();
    //     }
    //
    //     JPanel tempPanel = boardimage.returnPanel(bi);
    //
    //     tempPanel = boardimage.move(0, 9, 1, 9);
    //     return tempPanel;
    // }

    public JPanel movePlayerAndUpdate(String direction, String name) {
        BufferedImage bi = null;
        BoardImage boardimage = new BoardImage();

        /*
        This version is a hopefully more mobile version of the image loading method
         */
        try {
            bi = attemptToLoadImageFromResourceFolder();
        } catch (Exception resourceLoadException) {
            resourceLoadException.printStackTrace();
        }

        JPanel tempPanel = boardimage.returnPanel(bi);

        tempPanel = boardimage.move(direction, name);
        return tempPanel;
    }

    /**
     * attemptToLoadImageFromResourceFolder
     * This method pulls the URL/path from an image name and loads that into a buffered image
     *
     * @return a buffered image loaded from the hard-coded URL
     * @throws Exception Prints a stack trace in boardImagePanel if the image is not found
     */
    public BufferedImage attemptToLoadImageFromResourceFolder() throws Exception {
        URL imageUrl = this.getClass().getResource("board1.jpg");
        return ImageIO.read(imageUrl);
    }
}
