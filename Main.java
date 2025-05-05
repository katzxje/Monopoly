import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame implements ActionListener {

    private GameEngine gameEngine;
    private JTextArea logArea;
    private JButton playTurnButton;
    private JLabel statusLabel; // To show current player info

    public Main() {
        // --- Player Setup (Keep console for now, can be changed to GUI later) ---
        Scanner scanner = new Scanner(System.in);
        List<String> playerNames = new ArrayList<>();

        System.out.println("Welcome to Monopoly!");
        System.out.print("Enter number of players (2-4): ");
        int numPlayers = 0;
        while (numPlayers < 2 || numPlayers > 4) {
            try {
                String input = scanner.nextLine();
                numPlayers = Integer.parseInt(input);
                if (numPlayers < 2 || numPlayers > 4) {
                    System.out.print("Invalid number. Please enter between 2 and 4: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a number. Enter number of players (2-4): ");
            }
        }

        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter name for Player " + (i + 1) + ": ");
            String name = scanner.nextLine();
            // TODO: Add validation for empty or duplicate names if desired
            playerNames.add(name);
        }
        scanner.close(); // Close scanner once names are collected

        // --- Initialize GameEngine ---
        gameEngine = new GameEngine(playerNames);

        // --- Setup GUI ---
        setTitle("Monopoly Game");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // Status Label
        statusLabel = new JLabel("Game Started. Initializing...");
        add(statusLabel, BorderLayout.NORTH);

        // Control Panel
        JPanel controlPanel = new JPanel();
        playTurnButton = new JButton("Play Turn");
        playTurnButton.addActionListener(this);
        controlPanel.add(playTurnButton);
        add(controlPanel, BorderLayout.SOUTH);

        logArea.append("--- Game Started! ---\n");
        updateStatusLabel();

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == playTurnButton) {
            if (!gameEngine.isGameOver()) {
                String turnResult = gameEngine.playTurn();
                logArea.append("\n------------------------------\n");
                logArea.append(turnResult); // Append the result from GameEngine
                logArea.setCaretPosition(logArea.getDocument().getLength()); // Scroll to bottom

                if (gameEngine.isGameOver()) {
                    logArea.append("\n--- GAME OVER ---\n");
                    // Winner message is printed by GameEngine's checkGameOver
                    playTurnButton.setEnabled(false);
                    // Optionally show a final pop-up
                    JOptionPane.showMessageDialog(this,
                            "Game Over! Check logs for the winner.",
                            "Game Over",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    updateStatusLabel(); // Update status for the next player
                }
            } else {
                 logArea.append("\n--- GAME OVER ---\n");
                 playTurnButton.setEnabled(false);
            }
        }
    }

    private void updateStatusLabel() {
        if (!gameEngine.isGameOver()) {
            Player currentPlayer = gameEngine.getCurrentPlayer();
            statusLabel.setText("Current Turn: " + currentPlayer.getName() +
                                " ($ " + currentPlayer.getMoney() +
                                ") | Position: " + currentPlayer.getPosition() +
                                (currentPlayer.isInJail() ? " (In Jail)" : ""));
        }
    }

    public static void main(String[] args) {
        // Run the GUI construction in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }
} 