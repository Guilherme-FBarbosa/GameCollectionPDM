package com.guilhermegabriel.projetopdm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class GamesDB extends SQLiteOpenHelper {

    // ... (variáveis e construtor inalterados)
     private static final String DATABASE_NAME = "game_collection.db";
    private static final int DATABASE_VERSION = 1;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public GamesDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlGames = "CREATE TABLE " + Game.TABLE_NAME + " (" +
                Game.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Game.COL_TITLE + " TEXT NOT NULL, " +
                Game.COL_PLATFORM + " TEXT NOT NULL, " +
                Game.COL_YEAR + " INTEGER, " +
                Game.COL_FORMAT + " TEXT, " +
                Game.COL_STATUS + " TEXT, " +
                Game.COL_PROGRESS + " INTEGER, " +
                Game.COL_DATE_STARTED + " TEXT, " +
                Game.COL_DATE_COMPLETED + " TEXT, " +
                Game.COL_NOTES + " TEXT, " +
                Game.COL_RATING + " INTEGER, " +
                Game.COL_IMAGE + " TEXT" +
                ");";

        String sqlLoans = "CREATE TABLE " + Loan.TABLE_NAME + " (" +
                Loan.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Loan.COL_GAME_ID + " INTEGER NOT NULL, " +
                Loan.COL_GAME_TITLE + " TEXT NOT NULL, " +
                Loan.COL_BORROWER_NAME + " TEXT NOT NULL, " +
                Loan.COL_DATE_LENT + " TEXT NOT NULL, " +
                Loan.COL_DATE_RETURNED + " TEXT, " +
                "FOREIGN KEY(" + Loan.COL_GAME_ID + ") REFERENCES " + Game.TABLE_NAME + "(" + Game.COL_ID + ") ON DELETE CASCADE" +
                ");";

        db.execSQL(sqlGames);
        db.execSQL(sqlLoans);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Game.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Loan.TABLE_NAME);
        onCreate(db);
    }
    
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureColumnsExist(db);
    }

    /**
     * Verifica se colunas esperadas existem na tabela t_games e as adiciona se estiverem faltando.
     * Isso protege instalações antigas que não possuem as colunas novas.
     */
    private void ensureColumnsExist(SQLiteDatabase db) {
        Cursor c = null;
        try {
            c = db.rawQuery("PRAGMA table_info('" + Game.TABLE_NAME + "')", null);
            boolean hasRating = false;
            boolean hasImage = false;
            if (c != null) {
                while (c.moveToNext()) {
                    String colName = c.getString(c.getColumnIndexOrThrow("name"));
                    if (Game.COL_RATING.equals(colName)) hasRating = true;
                    if (Game.COL_IMAGE.equals(colName)) hasImage = true;
                }
            }

            if (!hasRating) {
                try {
                    db.execSQL("ALTER TABLE " + Game.TABLE_NAME + " ADD COLUMN " + Game.COL_RATING + " INTEGER DEFAULT 0");
                } catch (Exception ignored) { }
            }
            if (!hasImage) {
                try {
                    db.execSQL("ALTER TABLE " + Game.TABLE_NAME + " ADD COLUMN " + Game.COL_IMAGE + " TEXT");
                } catch (Exception ignored) { }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
    }

    // --- CRUD Operations for Games ---

    public long insertGame(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Game.COL_TITLE, game.getTitle());
        values.put(Game.COL_PLATFORM, game.getPlatform());
        values.put(Game.COL_YEAR, game.getYear());
        values.put(Game.COL_FORMAT, game.getFormat());
        values.put(Game.COL_STATUS, game.getStatus());
        values.put(Game.COL_PROGRESS, game.getProgress());
        values.put(Game.COL_DATE_STARTED, calendarToString(game.getDateStarted()));
        values.put(Game.COL_DATE_COMPLETED, calendarToString(game.getDateCompleted()));
        values.put(Game.COL_NOTES, game.getNotes());
        values.put(Game.COL_RATING, game.getRating());
        values.put(Game.COL_IMAGE, game.getImageUri());

        long id = db.insert(Game.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public boolean updateGame(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Game.COL_TITLE, game.getTitle());
        values.put(Game.COL_PLATFORM, game.getPlatform());
        values.put(Game.COL_YEAR, game.getYear());
        values.put(Game.COL_FORMAT, game.getFormat());
        values.put(Game.COL_STATUS, game.getStatus());
        values.put(Game.COL_PROGRESS, game.getProgress());
        values.put(Game.COL_DATE_STARTED, calendarToString(game.getDateStarted()));
        values.put(Game.COL_DATE_COMPLETED, calendarToString(game.getDateCompleted()));
        values.put(Game.COL_NOTES, game.getNotes());
        values.put(Game.COL_RATING, game.getRating());
        values.put(Game.COL_IMAGE, game.getImageUri());

        int rowsAffected = db.update(Game.TABLE_NAME, values, Game.COL_ID + " = ?", new String[]{String.valueOf(game.getId())});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteGame(int gameId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(Game.TABLE_NAME, Game.COL_ID + " = ?", new String[]{String.valueOf(gameId)});
        db.close();
        return rows > 0;
    }

    public Game selectGame(int gameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(Game.TABLE_NAME, null, Game.COL_ID + " = ?", new String[]{String.valueOf(gameId)}, null, null, null);
        Game game = null;
        if (c != null) {
            if (c.moveToFirst()) {
                game = cursorToGame(c);
            }
            c.close();
        }
        db.close();
        return game;
    }

    public ArrayList<Game> selectAll() {
        ArrayList<Game> games = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(Game.TABLE_NAME, null, null, null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                Game g = cursorToGame(c);
                games.add(g);
            }
            c.close();
        }
        db.close();
        return games;
    }

    public ArrayList<Game> getGamesWithFilters(List<String> platformFilters, List<String> statusFilters) {
        ArrayList<Game> games = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        List<String> selectionParts = new ArrayList<>();
        List<String> selectionArgs = new ArrayList<>();

        if (platformFilters != null && !platformFilters.isEmpty()) {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < platformFilters.size(); i++) {
                if (i > 0) placeholders.append(",");
                placeholders.append("?");
                selectionArgs.add(platformFilters.get(i));
            }
            selectionParts.add(Game.COL_PLATFORM + " IN (" + placeholders.toString() + ")");
        }

        if (statusFilters != null && !statusFilters.isEmpty()) {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < statusFilters.size(); i++) {
                if (i > 0) placeholders.append(",");
                placeholders.append("?");
                selectionArgs.add(statusFilters.get(i));
            }
            selectionParts.add(Game.COL_STATUS + " IN (" + placeholders.toString() + ")");
        }

        String selection = null;
        String[] selectionArray = null;
        if (!selectionParts.isEmpty()) {
            selection = TextUtils.join(" AND ", selectionParts);
            selectionArray = selectionArgs.toArray(new String[0]);
        }

        Cursor c = db.query(Game.TABLE_NAME, null, selection, selectionArray, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                games.add(cursorToGame(c));
            }
            c.close();
        }
        db.close();
        return games;
    }

    // --- CRUD Operations for Loans ---

    public long insertLoan(Loan loan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Loan.COL_GAME_ID, loan.getGameId());
        values.put(Loan.COL_GAME_TITLE, loan.getGameTitle());
        values.put(Loan.COL_BORROWER_NAME, loan.getBorrowerName());
        values.put(Loan.COL_DATE_LENT, calendarToString(loan.getDateLent()));
        values.put(Loan.COL_DATE_RETURNED, calendarToString(loan.getDateReturned()));

        long id = db.insert(Loan.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public boolean updateLoanOnReturn(int gameId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Loan.COL_DATE_RETURNED, calendarToString(Calendar.getInstance()));

        // Atualiza o empréstimo mais recente do jogo que ainda não foi devolvido
        int rowsAffected = db.update(Loan.TABLE_NAME, values, 
                                   Loan.COL_GAME_ID + " = ? AND " + Loan.COL_DATE_RETURNED + " IS NULL", 
                                   new String[]{String.valueOf(gameId)});
        db.close();
        return rowsAffected > 0;
    }

    public Loan selectActiveLoanForGame(int gameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(Loan.TABLE_NAME, null, Loan.COL_GAME_ID + " = ? AND " + Loan.COL_DATE_RETURNED + " IS NULL", new String[]{String.valueOf(gameId)}, null, null, null);
        Loan loan = null;
        if (c != null) {
            if (c.moveToFirst()) {
                loan = cursorToLoan(c);
            }
            c.close();
        }
        db.close();
        return loan;
    }

    // Utilitário: converte um Cursor numa instância de Game
    private Game cursorToGame(Cursor c) {
        Game g = new Game();
        int idxId = c.getColumnIndex(Game.COL_ID);
        if (idxId != -1) g.setId(c.getInt(idxId));

        int idxTitle = c.getColumnIndex(Game.COL_TITLE);
        if (idxTitle != -1) g.setTitle(c.getString(idxTitle));

        int idxPlatform = c.getColumnIndex(Game.COL_PLATFORM);
        if (idxPlatform != -1) g.setPlatform(c.getString(idxPlatform));

        int idxYear = c.getColumnIndex(Game.COL_YEAR);
        if (idxYear != -1) g.setYear(c.getInt(idxYear));

        int idxFormat = c.getColumnIndex(Game.COL_FORMAT);
        if (idxFormat != -1) g.setFormat(c.getString(idxFormat));

        int idxStatus = c.getColumnIndex(Game.COL_STATUS);
        if (idxStatus != -1) g.setStatus(c.getString(idxStatus));

        int idxProgress = c.getColumnIndex(Game.COL_PROGRESS);
        if (idxProgress != -1) g.setProgress(c.getInt(idxProgress));

        int idxDateStarted = c.getColumnIndex(Game.COL_DATE_STARTED);
        if (idxDateStarted != -1) g.setDateStarted(stringToCalendar(c.getString(idxDateStarted)));

        int idxDateCompleted = c.getColumnIndex(Game.COL_DATE_COMPLETED);
        if (idxDateCompleted != -1) g.setDateCompleted(stringToCalendar(c.getString(idxDateCompleted)));

        int idxNotes = c.getColumnIndex(Game.COL_NOTES);
        if (idxNotes != -1) g.setNotes(c.getString(idxNotes));

        int idxRating = c.getColumnIndex(Game.COL_RATING);
        if (idxRating != -1) g.setRating(c.getInt(idxRating));

        int idxImage = c.getColumnIndex(Game.COL_IMAGE);
        if (idxImage != -1) g.setImageUri(c.getString(idxImage));

        return g;
    }

    // Utilitário: converte um Cursor numa instância de Loan
    private Loan cursorToLoan(Cursor c) {
        Loan loan = new Loan();
        int idxId = c.getColumnIndex(Loan.COL_ID);
        if (idxId != -1) loan.setId(c.getInt(idxId));

        int idxGameId = c.getColumnIndex(Loan.COL_GAME_ID);
        if (idxGameId != -1) loan.setGameId(c.getInt(idxGameId));

        int idxGameTitle = c.getColumnIndex(Loan.COL_GAME_TITLE);
        if (idxGameTitle != -1) loan.setGameTitle(c.getString(idxGameTitle));

        int idxBorrowerName = c.getColumnIndex(Loan.COL_BORROWER_NAME);
        if (idxBorrowerName != -1) loan.setBorrowerName(c.getString(idxBorrowerName));

        int idxDateLent = c.getColumnIndex(Loan.COL_DATE_LENT);
        if (idxDateLent != -1) loan.setDateLent(stringToCalendar(c.getString(idxDateLent)));

        int idxDateReturned = c.getColumnIndex(Loan.COL_DATE_RETURNED);
        if (idxDateReturned != -1) loan.setDateReturned(stringToCalendar(c.getString(idxDateReturned)));

        return loan;
    }

    // --- Métodos de conversão e outros auxiliares ---
    private String calendarToString(Calendar calendar) {
        if (calendar == null) return null;
        return dateFormat.format(calendar.getTime());
    }

    private Calendar stringToCalendar(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;
        try {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(dateFormat.parse(dateString));
            return calendar;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
