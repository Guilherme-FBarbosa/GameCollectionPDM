package com.guilhermegabriel.projetopdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lvGames;
    private GamesDB gamesDB;
    private GameAdapter gameAdapter;

    private final ArrayList<String> activePlatformFilters = new ArrayList<>();
    private final ArrayList<String> activeStatusFilters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvGames = findViewById(R.id.lv_games);
        FloatingActionButton fabAddGame = findViewById(R.id.fab_add_game);
        gamesDB = new GamesDB(this);

        fabAddGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditGameActivity.class);
            startActivity(intent);
        });

        // Clique longo para Editar/Excluir
        lvGames.setOnItemLongClickListener((parent, view, position, id) -> {
            final Game selectedGame = (Game) parent.getItemAtPosition(position);
            showEditDeleteDialog(selectedGame);
            return true;
        });

        // Clique curto para Detalhes
        lvGames.setOnItemClickListener((parent, view, position, id) -> {
            Game selectedGame = (Game) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, GameDetailsActivity.class);
            intent.putExtra("GAME_EXTRA", selectedGame);
            startActivity(intent);
        });

        loadGames(null, null);
    }
    
    private void loadGames(List<String> platformFilters, List<String> statusFilters) {
        ArrayList<Game> loadedGames;
        if ((platformFilters == null || platformFilters.isEmpty()) && (statusFilters == null || statusFilters.isEmpty())) {
            loadedGames = gamesDB.selectAll();
        } else {
            loadedGames = gamesDB.getGamesWithFilters(platformFilters, statusFilters);
        }

        if (gameAdapter == null) {
            gameAdapter = new GameAdapter(this, R.layout.list_item_game, loadedGames);
            lvGames.setAdapter(gameAdapter);
        } else {
            gameAdapter.clear();
            gameAdapter.addAll(loadedGames);
            gameAdapter.notifyDataSetChanged();
        }
    }
    
     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (itemId == R.id.action_filter) {
            showFilterDialog();
            return true;
        } else if (itemId == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filtrar Jogos");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        LinearLayout platformsContainer = dialogView.findViewById(R.id.container_platforms);
        LinearLayout statusesContainer = dialogView.findViewById(R.id.container_statuses);

        final CharSequence[] platforms = getResources().getStringArray(R.array.platforms);
        final List<CheckBox> platformCheckBoxes = new ArrayList<>();
        for (CharSequence platform : platforms) {
            CheckBox cb = new CheckBox(this);
            cb.setText(platform);
            cb.setChecked(activePlatformFilters.contains(platform.toString()));
            platformsContainer.addView(cb);
            platformCheckBoxes.add(cb);
        }

        final CharSequence[] statuses = getResources().getStringArray(R.array.status_options);
        final List<CheckBox> statusCheckBoxes = new ArrayList<>();
        for (CharSequence status : statuses) {
            CheckBox cb = new CheckBox(this);
            cb.setText(status);
            cb.setChecked(activeStatusFilters.contains(status.toString()));
            statusesContainer.addView(cb);
            statusCheckBoxes.add(cb);
        }

        builder.setPositiveButton("Aplicar", (dialog, which) -> {
            activePlatformFilters.clear();
            for (CheckBox cb : platformCheckBoxes) {
                if (cb.isChecked()) {
                    activePlatformFilters.add(cb.getText().toString());
                }
            }
            activeStatusFilters.clear();
            for (CheckBox cb : statusCheckBoxes) {
                if (cb.isChecked()) {
                    activeStatusFilters.add(cb.getText().toString());
                }
            }
            loadGames(activePlatformFilters, activeStatusFilters);
            AmUtil.showToast(this, "Filtro aplicado!");
        });

        builder.setNegativeButton("Limpar", (dialog, which) -> {
            activePlatformFilters.clear();
            activeStatusFilters.clear();
            loadGames(null, null);
            AmUtil.showToast(this, "Filtros limpos!");
        });

        builder.setNeutralButton("Cancelar", null);
        builder.create().show();
    }

    private void sortGames(SortCriterion criterion) {
        // ArrayAdapter.sort usa o comparador para ordenar a lista interna.
        Comparator<Game> comparator = null;
        switch (criterion) {
            case BY_TITLE_ASC:                comparator = (g1, g2) -> g1.getTitle().compareToIgnoreCase(g2.getTitle()); break;
            case BY_TITLE_DESC:               comparator = (g1, g2) -> g2.getTitle().compareToIgnoreCase(g1.getTitle()); break;
            case BY_PLATFORM_ASC:             comparator = (g1, g2) -> g1.getPlatform().compareToIgnoreCase(g2.getPlatform()); break;
            case BY_PROGRESS_DESC:            comparator = (g1, g2) -> Integer.compare(g2.getProgress(), g1.getProgress()); break;
            case BY_DATE_ADDED_DESC:          comparator = (g1, g2) -> Integer.compare(g2.getId(), g1.getId()); break;
            case BY_RATING_DESC:              comparator = (g1, g2) -> Integer.compare(g2.getRating(), g1.getRating()); break;
        }
        if (comparator != null) {
            gameAdapter.sort(comparator);
        }
    }

    private void showSortDialog() {
        final SortCriterion[] criteria = SortCriterion.values();
        final CharSequence[] sortOptions = new CharSequence[criteria.length];
        for (int i = 0; i < criteria.length; i++) {
            sortOptions[i] = criteria[i].toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ordenar por");
        builder.setItems(sortOptions, (dialog, which) -> {
            SortCriterion selected = criteria[which];
            sortGames(selected);
            AmUtil.showToast(MainActivity.this, "Lista ordenada por: " + selected);
        });
        builder.create().show();
    }

    private void showEditDeleteDialog(final Game game) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(game.getTitle());
        builder.setItems(new CharSequence[]{"Editar", "Excluir"}, (dialog, which) -> {
            if (which == 0) { // Editar
                Intent intent = new Intent(MainActivity.this, AddEditGameActivity.class);
                intent.putExtra("GAME_EXTRA", game);
                startActivity(intent);
            } else if (which == 1) { // Excluir
                showDeleteConfirmationDialog(game);
            }
        });
        builder.create().show();
    }

    private void showDeleteConfirmationDialog(final Game game) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Exclusão");
        builder.setMessage("Tem certeza que deseja excluir o jogo '" + game.getTitle() + "'?");
        builder.setPositiveButton("Excluir", (dialog, which) -> {
            if (gamesDB.deleteGame(game.getId())) {
                AmUtil.showToast(MainActivity.this, "Jogo excluído com sucesso.");
                loadGames(activePlatformFilters, activeStatusFilters);
            } else {
                AmUtil.showToast(MainActivity.this, "Erro ao excluir o jogo.");
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sobre o Game Collection");
        builder.setMessage("Projeto para a disciplina de Programação para Dispositivos Móveis\n\nDesenvolvido por: Guilherme Barbosa e Gabriel Bezerra");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGames(activePlatformFilters, activeStatusFilters);
    }
}
