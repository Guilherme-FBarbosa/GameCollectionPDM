package com.guilhermegabriel.projetopdm;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GameDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvPlatform, tvStatus, tvLoanInfo;
    private Game currentGame;
    private GamesDB gamesDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);
        
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
    }

    private void populateDetails() {
        if (currentGame == null) return;
        setTitle(currentGame.getTitle());
        tvTitle.setText(currentGame.getTitle());
        tvPlatform.setText(getString(R.string.label_platform, currentGame.getPlatform()));
        tvStatus.setText(getString(R.string.label_status, currentGame.getStatus()));
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

}
