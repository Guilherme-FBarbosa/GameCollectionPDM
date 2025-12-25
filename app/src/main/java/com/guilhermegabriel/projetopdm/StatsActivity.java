package com.guilhermegabriel.projetopdm;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    private GamesDB gamesDB;
    private TextView tvTotalGames, tvCompletedPercentage, tvGamesByPlatform;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats); // Será criado a seguir
        setTitle("Estatísticas da Coleção");

        gamesDB = new GamesDB(this);
        initViews();
        calculateAndShowStats();
    }

    private void initViews() {
        tvTotalGames = findViewById(R.id.tv_total_games);
        tvCompletedPercentage = findViewById(R.id.tv_completed_percentage);
        tvGamesByPlatform = findViewById(R.id.tv_games_by_platform);
    }

    private void calculateAndShowStats() {
        ArrayList<Game> allGames = gamesDB.selectAll();
        int totalGames = allGames.size();

        if (totalGames == 0) {
            tvTotalGames.setText("Total de Jogos: 0");
            tvCompletedPercentage.setText("Concluídos: 0%");
            tvGamesByPlatform.setText("Nenhum jogo na coleção.");
            return;
        }

        // 1. Total de jogos
        tvTotalGames.setText("Total de Jogos: " + totalGames);

        // 2. Percentual de concluídos
        long completedCount = allGames.stream().filter(g -> "Concluído".equals(g.getStatus())).count();
        long completedPercentage = (completedCount * 100) / totalGames;
        tvCompletedPercentage.setText("Concluídos: " + completedPercentage + "%");

        // 3. Contagem por plataforma
        Map<String, Integer> platformCounts = new HashMap<>();
        for (Game game : allGames) {
            platformCounts.put(game.getPlatform(), platformCounts.getOrDefault(game.getPlatform(), 0) + 1);
        }

        StringBuilder platformStats = new StringBuilder("Jogos por Plataforma:\n");
        for (Map.Entry<String, Integer> entry : platformCounts.entrySet()) {
            platformStats.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        tvGamesByPlatform.setText(platformStats.toString());
    }
}
