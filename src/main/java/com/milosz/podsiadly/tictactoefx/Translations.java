package com.milosz.podsiadly.tictactoefx;

public class Translations {
    private static String currentLanguage = "EN";
    public static void setLanguage(String language) { currentLanguage = language.toUpperCase(); }

    public static String translate(String message) {
        return switch (currentLanguage) {
            case "PL" -> switch (message) {
                case "Enter name for Player 1 (X):" -> "Podaj imię dla Gracza 1 (X):";
                case "Enter name for Player 2 (O):" -> "Podaj imię dla Gracza 2 (O):";
                case "Current Player: " -> "Aktualny gracz: ";
                case "Invalid move! Try again." -> "Nieprawidłowy ruch! Spróbuj ponownie.";
                case "Player " -> "Gracz ";
                case "wins!" -> "wygrywa!";
                case "It's a draw!" -> "Remis!";
                case "Player vs Computer" -> "Gracz vs Komputer";
                case "Player vs Player" -> "Gracz vs Gracz";
                case "Choose mode (1/2): " -> "Wybierz tryb gry (1/2): ";
                case "Language / Język" -> "Język";
                case "Mode" -> "Tryb";
                case "Choose language / Wybierz język:" -> "Wybierz język:";
                case "Easy" -> "Łatwy";
                case "Medium" -> "Średni";
                case "Hard" -> "Trudny";
                case "Computer" -> "Komputer";
                case "Choose difficulty level (1-3): " -> "Wybierz poziom trudności (1-3): ";
                case "New Round" -> "Nowa runda";
                case "Reset Scores" -> "Wyzeruj wyniki";
                case "Setup" -> "Ustawienia";
                case "Score" -> "Wynik";
                case "Draws" -> "Remisy";
                default -> message;
            };
            default -> switch (message) {
                case "Enter name for Player 1 (X):" -> "Enter name for Player 1 (X):";
                case "Enter name for Player 2 (O):" -> "Enter name for Player 2 (O):";
                case "Current Player: " -> "Current Player: ";
                case "Invalid move! Try again." -> "Invalid move! Try again.";
                case "Player " -> "Player ";
                case "wins!" -> "wins!";
                case "It's a draw!" -> "It's a draw!";
                case "Player vs Computer" -> "Player vs Computer";
                case "Player vs Player" -> "Player vs Player";
                case "Choose mode (1/2): " -> "Choose mode (1/2): ";
                case "Language / Język" -> "Language";
                case "Mode" -> "Mode";
                case "Choose language / Wybierz język:" -> "Choose language:";
                case "Easy" -> "Easy";
                case "Medium" -> "Medium";
                case "Hard" -> "Hard";
                case "Computer" -> "Computer";
                case "Choose difficulty level (1-3): " -> "Choose difficulty level (1-3): ";
                case "New Round" -> "New Round";
                case "Reset Scores" -> "Reset Scores";
                case "Setup" -> "Setup";
                case "Score" -> "Score";
                case "Draws" -> "Draws";
                default -> message;
            };
        };
    }
}
