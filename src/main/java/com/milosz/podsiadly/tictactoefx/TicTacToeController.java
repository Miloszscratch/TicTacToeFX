package com.milosz.podsiadly.tictactoefx;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Optional;
import java.util.Random;
import java.util.prefs.Preferences;

public class TicTacToeController {

    @FXML private GridPane boardGrid;
    @FXML private Label statusLabel;
    @FXML private Label scoreLabel;
    @FXML private Button newRoundBtn, resetScoresBtn, setupBtn;

    private Board board;
    private Player player1, player2, currentPlayer;
    private boolean playerVsComputer;
    private int computerDifficultyLevel;
    private boolean gameOver = false;

    private final Button[][] cells = new Button[3][3];
    private final Random rng = new Random();
    private final ScoreStore store = new ScoreStore();
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());

    private int pvp_p1Wins = 0, pvp_p2Wins = 0, pvp_draws = 0;
    private int pvc_pWins  = 0, pvc_cWins  = 0, pvc_draws = 0;


    @FXML
    private void initialize() {
        buildBoardUI();

        ScoreStore.Scores s = store.load();
        pvp_p1Wins = s.pvp_p1Wins;  pvp_p2Wins = s.pvp_p2Wins;  pvp_draws = s.pvp_draws;
        pvc_pWins  = s.pvc_pWins;   pvc_cWins  = s.pvc_cWins;   pvc_draws = s.pvc_draws;

        configureGameWithDialog();
        updateStatus();
        updateScore();
    }

    private void buildBoardUI() {
        boardGrid.setHgap(8);
        boardGrid.setVgap(8);
        boardGrid.setAlignment(Pos.CENTER);

        ColumnConstraints wide = new ColumnConstraints();
        wide.setHalignment(HPos.CENTER);
        boardGrid.getColumnConstraints().setAll(wide, wide, wide);

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
                boolean alreadyMarked = !cells[r][c].getText().isBlank();
                cells[r][c].setDisable(disabled || alreadyMarked);
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
        configureGameWithDialog();
        updateScore();
        updateStatus();
    }

    private void onCellClick(int row, int col) {
        if (gameOver || !board.isCellAvailable(row, col)) return;

        makeMoveAndAdvance(row, col);
        if (gameOver) return;

        if (playerVsComputer && currentPlayer == player2) {
            PauseTransition pause = new PauseTransition(Duration.millis(350));
            pause.setOnFinished(e -> doComputerTurn());
            pause.play();
        }
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
            endGameAlert(currentPlayer.getName() + " (" + currentPlayer.getSymbol() + ") " +
                    Translations.translate("wins!"));
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

    private void persistScores() {
        store.save(ScoreStore.Scores.of(
                pvp_p1Wins, pvp_p2Wins, pvp_draws,
                pvc_pWins, pvc_cWins, pvc_draws
        ));
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

    private void configureGameWithDialog() {
        board = new Board();
        clearBoardUI();
        gameOver = false;

        Optional<SetupResult> res = showSetupDialog();
        SetupResult cfg = res.orElseGet(() -> defaultSetupFromPrefs());

        prefs.put("lang", cfg.language);
        prefs.put("mode", cfg.vsComputer ? "PvC" : "PvP");
        prefs.put("p1", cfg.p1);
        prefs.put("p2", cfg.p2);
        prefs.putInt("diff", cfg.difficulty);

        Translations.setLanguage(cfg.language);
        playerVsComputer = cfg.vsComputer;
        player1 = new Player(1, 'X', cfg.p1);
        if (playerVsComputer) {
            player2 = new Player(2, 'O', Translations.translate("Computer"));
            computerDifficultyLevel = cfg.difficulty;
        } else {
            player2 = new Player(2, 'O', cfg.p2);
            computerDifficultyLevel = 0;
        }

        currentPlayer = player1;
        updateButtonsText();
        updateStatus();
    }

    private SetupResult defaultSetupFromPrefs() {
        String lang = prefs.get("lang", "EN");
        boolean vsComp = !prefs.get("mode", "PvC").equals("PvP");
        String p1 = prefs.get("p1", "Player 1");
        String p2 = prefs.get("p2", "Player 2");
        int diff = prefs.getInt("diff", 1);
        return new SetupResult(lang, vsComp, p1, p2, diff);
    }

    private static final class SetupResult {
        final String language;
        final boolean vsComputer;
        final String p1, p2;
        final int difficulty;

        SetupResult(String language, boolean vsComputer, String p1, String p2, int difficulty) {
            this.language = language == null ? "EN" : language;
            this.vsComputer = vsComputer;
            this.p1 = (p1 == null || p1.isBlank()) ? "Player 1" : p1.trim();
            this.p2 = (p2 == null || p2.isBlank()) ? "Player 2" : p2.trim();
            this.difficulty = Math.min(3, Math.max(1, difficulty));
        }
    }

    private Optional<SetupResult> showSetupDialog() {
        Dialog<SetupResult> dialog = new Dialog<>();
        dialog.setTitle(Translations.translate("Setup"));
        dialog.setHeaderText(Translations.translate("Game Preferences"));

        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(Translations.translate("Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

        try {
            dialog.getDialogPane().getStylesheets().add(
                    TicTacToeApplication.class.getResource("app.css").toExternalForm()
            );
            dialog.getDialogPane().getStyleClass().add("setup-dialog");
        } catch (Exception ignored) {}

        GridPane gp = new GridPane();
        gp.setHgap(12);
        gp.setVgap(12);
        gp.setPadding(new Insets(14, 16, 16, 16));
        dialog.getDialogPane().setContent(gp);

        Label langHdr = new Label(Translations.translate("Language"));
        langHdr.getStyleClass().add("section-title");

        Label modeHdr = new Label(Translations.translate("Mode"));
        modeHdr.getStyleClass().add("section-title");

        Label playersHdr = new Label(Translations.translate("Players"));
        playersHdr.getStyleClass().add("section-title");

        Label diffHdr = new Label(Translations.translate("Difficulty"));
        diffHdr.getStyleClass().add("section-title");

        ComboBox<String> langBox = new ComboBox<>();
        langBox.getItems().addAll("EN", "PL");
        langBox.getSelectionModel().select(prefs.get("lang", "EN"));

        ToggleGroup modeGroup = new ToggleGroup();
        ToggleButton pvcBtn = new ToggleButton(Translations.translate("Player vs Computer"));
        ToggleButton pvpBtn = new ToggleButton(Translations.translate("Player vs Player"));
        pvcBtn.setToggleGroup(modeGroup); pvpBtn.setToggleGroup(modeGroup);
        if (prefs.get("mode", "PvC").equals("PvP")) pvpBtn.setSelected(true); else pvcBtn.setSelected(true);
        ToolBar modeBar = new ToolBar(pvcBtn, pvpBtn);
        modeBar.getStyleClass().add("segmented");

        TextField p1Field = new TextField(prefs.get("p1", "Player 1"));
        p1Field.setPromptText(Translations.translate("Player 1 (X)"));
        p1Field.getStyleClass().add("rounded");

        TextField p2Field = new TextField(prefs.get("p2", "Player 2"));
        p2Field.setPromptText(Translations.translate("Player 2 (O)"));
        p2Field.getStyleClass().add("rounded");

        ComboBox<String> diffBox = new ComboBox<>();
        String easy = Translations.translate("Easy");
        String medium = Translations.translate("Medium");
        String hard = Translations.translate("Hard");
        diffBox.getItems().addAll(easy, medium, hard);
        int savedDiff = prefs.getInt("diff", 1);
        diffBox.getSelectionModel().select(savedDiff == 3 ? hard : (savedDiff == 2 ? medium : easy));

        int r = 0;
        gp.add(langHdr, 0, r++, 2, 1);
        gp.add(new Label(Translations.translate("Choose language:")), 0, r);
        gp.add(langBox, 1, r++);

        gp.add(modeHdr, 0, r++, 2, 1);
        gp.add(modeBar, 0, r++, 2, 1);

        gp.add(playersHdr, 0, r++, 2, 1);
        gp.add(new Label(Translations.translate("Player 1 (X)")), 0, r);
        gp.add(p1Field, 1, r++);
        gp.add(new Label(Translations.translate("Player 2 (O)")), 0, r);
        gp.add(p2Field, 1, r++);

        gp.add(diffHdr, 0, r++, 2, 1);
        gp.add(new Label(Translations.translate("Level")), 0, r);
        gp.add(diffBox, 1, r++);

        Runnable updateVisibility = () -> {
            boolean isPvC = pvcBtn.isSelected();
            p2Field.setDisable(isPvC);
            diffBox.setDisable(!isPvC);
        };
        pvcBtn.selectedProperty().addListener((o, ov, nv) -> updateVisibility.run());
        pvpBtn.selectedProperty().addListener((o, ov, nv) -> updateVisibility.run());
        updateVisibility.run();

        Node okBtn = dialog.getDialogPane().lookupButton(ok);
        okBtn.disableProperty().bind(p1Field.textProperty().isEmpty());

        dialog.setResultConverter(btn -> {
            if (btn != ok) return null;
            String lang = langBox.getValue();
            boolean vsComp = pvcBtn.isSelected();
            String diff = diffBox.getValue();
            int level = diff == null ? 1 : (diff.equals(medium) ? 2 : (diff.equals(hard) ? 3 : 1));
            return new SetupResult(lang, vsComp, p1Field.getText(), p2Field.getText(), level);
        });

        return dialog.showAndWait();
    }
}
