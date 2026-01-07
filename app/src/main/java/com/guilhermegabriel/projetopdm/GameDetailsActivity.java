package com.guilhermegabriel.projetopdm;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class GameDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvPlatform, tvStatus, tvLoanInfo, tvYear, tvFormat, tvProgress, tvDates, tvNotes, tvRating;
    private ImageView ivCover;
    private Game currentGame;
    private GamesDB gamesDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_details);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Aumentar a área de clique dos itens do Toolbar (inclui back e action items)
        toolbar.post(() -> {
            View parent = (View) toolbar.getParent();
            if (parent != null) {
                Rect r = new Rect();
                toolbar.getHitRect(r);
                // aumenta mais para baixo (onde você relatou que não clicava)
                r.bottom += 100;
                parent.setTouchDelegate(new TouchDelegate(r, toolbar));
            }
        });

        gamesDB = new GamesDB(this);
        initViews();

        if (getIntent().hasExtra("GAME_EXTRA")) {
            currentGame = (Game) getIntent().getSerializableExtra("GAME_EXTRA");
            populateDetails();
            checkLoanStatus();
        } else {
            AmUtil.showToast(this, "Erro: Jogo não encontrado.");
            finish();
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_detail_title);
        tvPlatform = findViewById(R.id.tv_detail_platform);
        tvStatus = findViewById(R.id.tv_detail_status);
        tvLoanInfo = findViewById(R.id.tv_loan_info);
        tvYear = findViewById(R.id.tv_detail_year);
        tvFormat = findViewById(R.id.tv_detail_format);
        tvProgress = findViewById(R.id.tv_detail_progress);
        tvDates = findViewById(R.id.tv_detail_dates);
        tvNotes = findViewById(R.id.tv_detail_notes);
        tvRating = findViewById(R.id.tv_detail_rating);
        ivCover = findViewById(R.id.iv_detail_cover);
    }

    private void populateDetails() {
        if (currentGame == null) return;
        setTitle(currentGame.getTitle());
        tvTitle.setText(currentGame.getTitle());
        tvPlatform.setText(getString(R.string.label_platform, currentGame.getPlatform()));
        tvStatus.setText(getString(R.string.label_status, currentGame.getStatus()));
        tvYear.setText(getString(R.string.label_year, currentGame.getYear() > 0 ? String.valueOf(currentGame.getYear()) : "-"));
        tvFormat.setText(getString(R.string.label_format, currentGame.getFormat()));
        tvProgress.setText(getString(R.string.label_progress, currentGame.getProgress()));

        StringBuilder dates = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (currentGame.getDateStarted() != null)
            dates.append("Início: ").append(sdf.format(currentGame.getDateStarted().getTime()));
        if (currentGame.getDateCompleted() != null) {
            if (dates.length() > 0) dates.append(" | ");
            dates.append("Conclusão: ").append(sdf.format(currentGame.getDateCompleted().getTime()));
        }
        tvDates.setText(dates.length() > 0 ? dates.toString() : "-");

        tvNotes.setText(currentGame.getNotes() != null ? currentGame.getNotes() : "-");
        tvRating.setText(currentGame.getRating() > 0 ? (currentGame.getRating() + "/5") : "-");

        // Load image if available
        String uri = currentGame.getImageUri();
        ivCover.setImageResource(R.mipmap.ic_launcher);
        if (uri != null && !uri.isEmpty()) {
            if (uri.startsWith("content:") || uri.startsWith("file:")) {
                try {
                    ivCover.setImageURI(Uri.parse(uri));
                } catch (Exception ignored) {
                }
            } else if (uri.startsWith("http")) {
                final String imageUrl = uri;
                new Thread(() -> {
                    try {
                        URL url = new URL(imageUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        final Bitmap bmp = BitmapFactory.decodeStream(input);
                        if (bmp != null) runOnUiThread(() -> ivCover.setImageBitmap(bmp));
                    } catch (Exception ignored) {
                    }
                }).start();
            }
        }
    }

    private void checkLoanStatus() {
        if (currentGame != null && "Emprestado".equals(currentGame.getStatus())) {
            Loan activeLoan = gamesDB.selectActiveLoanForGame(currentGame.getId());
            if (activeLoan != null && activeLoan.getDateLent() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String loanDate = sdf.format(activeLoan.getDateLent().getTime());
                String loanText = getString(R.string.loan_info, activeLoan.getBorrowerName(), loanDate);
                tvLoanInfo.setText(loanText);
                tvLoanInfo.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(this, AddEditGameActivity.class);
            intent.putExtra("GAME_EXTRA", currentGame);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Deseja excluir o jogo '" + currentGame.getTitle() + "'?")
                    .setPositiveButton("Excluir", (dialog, which) -> {
                        if (gamesDB.deleteGame(currentGame.getId())) {
                            AmUtil.showToast(this, "Jogo excluído com sucesso.");
                            finish();
                        } else {
                            AmUtil.showToast(this, "Erro ao excluir o jogo.");
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
