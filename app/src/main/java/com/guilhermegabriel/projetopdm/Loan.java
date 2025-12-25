package com.guilhermegabriel.projetopdm;

import java.io.Serializable;
import java.util.Calendar;

public class Loan implements Serializable {
    private int id;
    private int mGameId;
    private String mGameTitle;
    private String mBorrowerName;
    private Calendar mDateLent;
    private Calendar mDateReturned; // null se ainda emprestado

    public Loan() {
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGameId() {
        return mGameId;
    }

    public void setGameId(int gameId) {
        this.mGameId = gameId;
    }

    public String getGameTitle() {
        return mGameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.mGameTitle = gameTitle;
    }

    public String getBorrowerName() {
        return mBorrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.mBorrowerName = borrowerName;
    }

    public Calendar getDateLent() {
        return mDateLent;
    }

    public void setDateLent(Calendar dateLent) {
        this.mDateLent = dateLent;
    }

    public Calendar getDateReturned() {
        return mDateReturned;
    }

    public void setDateReturned(Calendar dateReturned) {
        this.mDateReturned = dateReturned;
    }

    // Constantes para SQLite
    public static final String TABLE_NAME = "t_loans";
    public static final String COL_ID = "_id";
    public static final String COL_GAME_ID = "c_game_id";
    public static final String COL_GAME_TITLE = "c_game_title";
    public static final String COL_BORROWER_NAME = "c_borrower_name";
    public static final String COL_DATE_LENT = "c_date_lent";
    public static final String COL_DATE_RETURNED = "c_date_returned";
}
