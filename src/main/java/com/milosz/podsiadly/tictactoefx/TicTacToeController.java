package com.milosz.podsiadly.tictactoefx;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class TicTacToeController {
    @FXML private GridPane boardGrid;
    @FXML private Label statusLabel;
    @FXML private Label scoreLabel;
    @FXML private Button newRoundBtn, resetScoresBtn, setupBtn;

    private Board board;
    private Player player1, player2, currentPlayer;
    private boolean playerVsComputer;
    private int computerDifficultyLevel;

    private final Random random = new Random();
    private final Button[][] cells = new Button[3][3];

    private final ScoreStore store = new ScoreStore();
    private int pvp_p1Wins = 0, pvp_p2Wins = 0, pvp_draws = 0;
    private int pvc_pWins  = 0, pvc_cWins  = 0, pvc_draws = 0;

    private boolean gameOver = false;

    @FXML
    private void initialize() {
        buildBoardUI();

        ScoreStore.Scores s = store.load();
        pvp_p1Wins = s.pvp_p1Wins;  pvp_p2Wins = s.pvp_p2Wins;  pvp_draws = s.pvp_draws;
        pvc_pWins  = s.pvc_pWins;   pvc_cWins  = s.pvc_cWins;   pvc_draws = s.pvc_draws;

        configureGameWithDialogs();
        updateStatus();
        updateScore();
    }

    private void buildBoardUI() {
        boardGrid.setHgap(8);
        boardGrid.setVgap(8);
        boardGrid.setAlignment(Pos.CENTER);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                Button b = new Button(" ");
                b.getStyleClass().add("cell");
                b.setMinSize(110, 110);
                b.setFont(Font.font(36));
                final int row = r, col = c;
                b.setOnAction(e -> onCellClick(row, col));
                cells[r][c] = b;
                boardGrid.add(b, c, r);
            }
        }
    }

    private void clearBoardUI() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c].setText(" ");
                cells[r][c].setDisable(false);
            }
        }
    }

    private void disableBoard(boolean disabled) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c].setDisable(disabled || !cells[r][c].getText().isBlank());
            }
        }
    }

    @FXML
    private void onNewRound() {
        resetBoardOnly();
    }

    @FXML
    private void onResetScores() {
        pvp_p1Wins = pvp_p2Wins = pvp_draws = 0;
        pvc_pWins  = pvc_cWins  = pvc_draws  = 0;
        persistScores();
        updateScore();
    }

    @FXML
    private void onSetup() {
        configureGameWithDialogs();
        updateScore();
        updateStatus();
    }

    private void onCellClick(int row, int col) {
        if (gameOver) return;
        if (!board.isCellAvailable(row, col)) return;

        makeMoveAndAdvance(row, col);
        if (gameOver) return;

        if (playerVsComputer && currentPlayer == player2) {
            PauseTransition pause = new PauseTransition(Duration.millis(350));
            pause.setOnFinished(e -> doComputerTurn());
            pause.play();
        }
    }

    private void configureGameWithDialogs() {
        board = new Board();
        clearBoardUI();
        gameOver = false;

        ChoiceDialog<String> langDialog = new ChoiceDialog<>("EN", List.of("EN", "PL"));
        langDialog.setTitle(Translations.translate("Language / Język"));
        langDialog.setHeaderText(null);
        langDialog.setContentText(Translations.translate("Choose language / Wybierz język:"));
        String lang = langDialog.showAndWait().orElse("EN");
        Translations.setLanguage(lang);

        ChoiceDialog<String> modeDialog = new ChoiceDialog<>(
                Translations.translate("Player vs Computer"),
                Translations.translate("Player vs Computer"),
                Translations.translate("Player vs Player")
        );
        modeDialog.setTitle(Translations.translate("Mode"));
        modeDialog.setHeaderText(null);
        modeDialog.setContentText(Translations.translate("Choose mode (1/2): "));
        String mode = modeDialog.showAndWait().orElse(Translations.translate("Player vs Computer"));
        playerVsComputer = mode.equals(Translations.translate("Player vs Computer"));

        String name1 = promptText(Translations.translate("Enter name for Player 1 (X):"), "Player 1");
        if (name1 == null || name1.isBlank()) name1 = "Player 1";
        player1 = new Player(1, 'X', name1);

        if (playerVsComputer) {
            player2 = new Player(2, 'O', Translations.translate("Computer"));
            ChoiceDialog<String> diffDialog = new ChoiceDialog<>(
                    Translations.translate("Easy"),
                    Translations.translate("Easy"),
                    Translations.translate("Medium"),
                    Translations.translate("Hard")
            );
            diffDialog.setTitle("Difficulty");
            diffDialog.setHeaderText(null);
            diffDialog.setContentText(Translations.translate("Choose difficulty level (1-3): "));
            String pick = diffDialog.showAndWait().orElse(Translations.translate("Easy"));
            computerDifficultyLevel = pick.equals(Translations.translate("Medium")) ? 2
                    : pick.equals(Translations.translate("Hard")) ? 3 : 1;
        } else {
            String name2 = promptText(Translations.translate("Enter name for Player 2 (O):"), "Player 2");
            if (name2 == null || name2.isBlank()) name2 = "Player 2";
            player2 = new Player(2, 'O', name2);
        }

        currentPlayer = player1;
        updateButtonsText();
        updateStatus();
    }

    private String promptText(String prompt, String placeholder) {
        TextInputDialog d = new TextInputDialog(placeholder);
        d.setTitle("Setup");
        d.setHeaderText(null);
        d.setContentText(prompt);
        Optional<String> res = d.showAndWait();
        return res.orElse(null);
    }

    private void makeMoveAndAdvance(int row, int col) {
        board.makeMove(row, col, currentPlayer.getNumber());
        cells[row][col].setText(String.valueOf(currentPlayer.getSymbol()));
        cells[row][col].setDisable(true);

        if (board.checkWin(currentPlayer.getNumber())) {
            if (playerVsComputer) {
                if (currentPlayer == player1) pvc_pWins++; else pvc_cWins++;
            } else {
                if (currentPlayer == player1) pvp_p1Wins++; else pvp_p2Wins++;
            }
            persistScores();
            updateScore();

            gameOver = true;
            disableBoard(true);
            endGameAlert(currentPlayer.getName() + " (" + currentPlayer.getSymbol() + ") " + Translations.translate("wins!"));
            return;
        }

        if (board.isFull()) {
            if (playerVsComputer) pvc_draws++; else pvp_draws++;
            persistScores();
            updateScore();

            gameOver = true;
            disableBoard(true);
            endGameAlert(Translations.translate("It's a draw!"));
            return;
        }

        switchPlayer();
        updateStatus();
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private void doComputerTurn() {
        if (gameOver) return;

        int[] mv = (computerDifficultyLevel == 1) ? getComputerMove()
                : (computerDifficultyLevel == 2) ? getComputerMoveMedium()
                : getComputerMoveHard();

        if (mv == null || !board.isCellAvailable(mv[0], mv[1])) mv = getFallbackMove();
        makeMoveAndAdvance(mv[0], mv[1]);
    }

    private void resetBoardOnly() {
        board = new Board();
        clearBoardUI();
        gameOver = false;
        currentPlayer = player1;
        updateStatus();
    }

    private void updateStatus() {
        statusLabel.setText(Translations.translate("Current Player: ")
                + currentPlayer.getName() + " (" + currentPlayer.getSymbol() + ")");
    }

    private void updateScore() {
        if (playerVsComputer) {
            String p1 = (player1 != null) ? player1.getName() : "Player 1";
            String linePvc = "[PvC] " + p1 + " = " + pvc_pWins + "  |  "
                    + Translations.translate("Computer") + " = " + pvc_cWins + "  |  "
                    + Translations.translate("Draws") + ": " + pvc_draws;
            scoreLabel.setText(linePvc);
        } else {
            String p1 = (player1 != null) ? player1.getName() : "Player 1";
            String p2 = (player2 != null) ? player2.getName() : "Player 2";
            String linePvp = "[PvP] " + p1 + " = " + pvp_p1Wins + "  |  "
                    + p2 + " = " + pvp_p2Wins + "  |  "
                    + Translations.translate("Draws") + ": " + pvp_draws;
            scoreLabel.setText(linePvp);
        }
    }

    private void updateButtonsText() {
        newRoundBtn.setText(Translations.translate("New Round"));
        resetScoresBtn.setText(Translations.translate("Reset Scores"));
        setupBtn.setText(Translations.translate("Setup"));
        updateScore();
    }

    private void persistScores() {
        store.save(ScoreStore.Scores.of(
                pvp_p1Wins, pvp_p2Wins, pvp_draws,
                pvc_pWins, pvc_cWins, pvc_draws
        ));
    }

    private void endGameAlert(String message) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Game Over");
            a.setHeaderText(message);
            a.setContentText("");

            try {
                a.getDialogPane().getStylesheets().add(
                        TicTacToeApplication.class.getResource("app.css").toExternalForm()
                );
            } catch (Exception ignored) { }

            ButtonType again = new ButtonType(Translations.translate("New Round"), ButtonBar.ButtonData.OK_DONE);
            ButtonType exit  = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);
            a.getButtonTypes().setAll(again, exit);

            if (boardGrid.getScene() != null && boardGrid.getScene().getWindow() != null) {
                a.initOwner(boardGrid.getScene().getWindow());
            }

            Optional<ButtonType> res = a.showAndWait();
            if (res.isPresent() && res.get() == again) {
                resetBoardOnly();
            }
        });
    }

    private final Random rng = new Random();

    private int[] getComputerMove() {
        int r, c;
        do { r = rng.nextInt(3); c = rng.nextInt(3); } while (!board.isCellAvailable(r, c));
        return new int[]{r, c};
    }

    private int[] getComputerMoveMedium() {
        try {
            for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) if (board.isCellAvailable(r, c)) {
                board.markCell(r, c, currentPlayer.getNumber());
                if (board.checkWin(currentPlayer.getNumber())) { board.undoMove(r, c); return new int[]{r, c}; }
                board.undoMove(r, c);
            }
            Player opp = (currentPlayer == player1) ? player2 : player1;
            for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) if (board.isCellAvailable(r, c)) {
                board.markCell(r, c, opp.getNumber());
                if (board.checkWin(opp.getNumber())) { board.undoMove(r, c); return new int[]{r, c}; }
                board.undoMove(r, c);
            }
            return getComputerMove();
        } catch (Exception e) {
            return getFallbackMove();
        }
    }

    private int[] getComputerMoveHard() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        try {
            for (int r = 0; r < board.getSize(); r++) {
                for (int c = 0; c < board.getSize(); c++) {
                    if (board.isCellAvailable(r, c)) {
                        board.makeMove(r, c, player2.getNumber());
                        int score = miniMax(false, 0);
                        board.undoMove(r, c);
                        if (score > bestScore) {
                            bestScore = score;
                            bestMove = new int[]{r, c};
                        }
                    }
                }
            }
            if (bestMove == null) bestMove = getFallbackMove();
        } catch (Exception e) {
            bestMove = getFallbackMove();
        }
        return bestMove;
    }

    private int miniMax(boolean isMax, int depth) {
        if (board.checkWin(player2.getNumber())) return 10 - depth;
        if (board.checkWin(player1.getNumber())) return depth - 10;
        if (board.isFull()) return 0;

        int best = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (board.isCellAvailable(r, c)) {
                    int symbol = isMax ? player2.getNumber() : player1.getNumber();
                    board.makeMove(r, c, symbol);
                    int score = miniMax(!isMax, depth + 1);
                    board.undoMove(r, c);
                    best = isMax ? Math.max(best, score) : Math.min(best, score);
                }
            }
        }
        return best;
    }

    private int[] getFallbackMove() {
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (board.isCellAvailable(r, c)) return new int[]{r, c};
            }
        }
        return new int[]{0, 0};
    }
}
