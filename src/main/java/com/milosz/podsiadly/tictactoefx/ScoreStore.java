package com.milosz.podsiadly.tictactoefx;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class ScoreStore {
    private final Path dir  = Paths.get(System.getProperty("user.home"), ".tictactoefx");
    private final Path file = dir.resolve("score.properties");

    public static class Scores {
        public int pvp_p1Wins, pvp_p2Wins, pvp_draws;
        public int pvc_pWins, pvc_cWins, pvc_draws;

        public Scores() {}
        public static Scores of(int pvp1, int pvp2, int pvpD, int pvcP, int pvcC, int pvcD) {
            Scores s = new Scores();
            s.pvp_p1Wins = pvp1; s.pvp_p2Wins = pvp2; s.pvp_draws = pvpD;
            s.pvc_pWins  = pvcP; s.pvc_cWins  = pvcC; s.pvc_draws = pvcD;
            return s;
        }
    }

    public Scores load() {
        try {
            if (Files.notExists(file)) return new Scores();
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            Scores s = new Scores();
            s.pvp_p1Wins = Integer.parseInt(props.getProperty("pvp.p1Wins", "0"));
            s.pvp_p2Wins = Integer.parseInt(props.getProperty("pvp.p2Wins", "0"));
            s.pvp_draws  = Integer.parseInt(props.getProperty("pvp.draws",  "0"));
            s.pvc_pWins  = Integer.parseInt(props.getProperty("pvc.playerWins", "0"));
            s.pvc_cWins  = Integer.parseInt(props.getProperty("pvc.computerWins", "0"));
            s.pvc_draws  = Integer.parseInt(props.getProperty("pvc.draws", "0"));
            return s;
        } catch (Exception e) {
            return new Scores();
        }
    }

    public void save(Scores s) {
        try {
            if (Files.notExists(dir)) Files.createDirectories(dir);
            Properties props = new Properties();
            props.setProperty("pvp.p1Wins", Integer.toString(s.pvp_p1Wins));
            props.setProperty("pvp.p2Wins", Integer.toString(s.pvp_p2Wins));
            props.setProperty("pvp.draws",  Integer.toString(s.pvp_draws));

            props.setProperty("pvc.playerWins",   Integer.toString(s.pvc_pWins));
            props.setProperty("pvc.computerWins", Integer.toString(s.pvc_cWins));
            props.setProperty("pvc.draws",        Integer.toString(s.pvc_draws));

            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "TicTacToeFX scores (PvP & PvC)");
            }
        } catch (IOException ignored) {}
    }
}
