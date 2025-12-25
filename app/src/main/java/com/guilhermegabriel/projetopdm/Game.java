package com.guilhermegabriel.projetopdm;

import java.io.Serializable;
import java.util.Calendar;

public class Game implements Serializable {
    private int id;
    private String mTitle;
    private String mPlatform; // PS5, Xbox, Switch, PC, Mobile
    private int mYear;
    private String mFormat; // Físico, Digital
    
    // Progresso
    private String mStatus; // Por Jogar, A Jogar, Concluído, Abandonado
    private int mProgress; // 0-100
    
    // Datas
    private Calendar mDateStarted;
    private Calendar mDateCompleted;
    
    // Extra
    private String mNotes;
    private int mRating; // 1-5 estrelas (opcional)

    // Imagem (URI local ou URL)
    private String mImageUri;

    public Game() {
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getPlatform() {
        return mPlatform;
    }

    public void setPlatform(String platform) {
        this.mPlatform = platform;
    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int year) {
        this.mYear = year;
    }

    public String getFormat() {
        return mFormat;
    }

    public void setFormat(String format) {
        this.mFormat = format;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
    }

    public Calendar getDateStarted() {
        return mDateStarted;
    }

    public void setDateStarted(Calendar dateStarted) {
        this.mDateStarted = dateStarted;
    }

    public Calendar getDateCompleted() {
        return mDateCompleted;
    }

    public void setDateCompleted(Calendar dateCompleted) {
        this.mDateCompleted = dateCompleted;
    }

    public String getNotes() {
        return mNotes;
    }



    public void setNotes(String notes) {
        this.mNotes = notes;
    }

    public int getRating() {
        return mRating;
    }

    public void setRating(int rating) {
        this.mRating = rating;
    }

    public String getImageUri() {
        return mImageUri;
    }

    public void setImageUri(String imageUri) {
        this.mImageUri = imageUri;
    }

    @Override
    public String toString() {
        return mTitle + " (" + mPlatform + ")";
    }

    // Constantes para SQLite
    public static final String TABLE_NAME = "t_games";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "c_title";
    public static final String COL_PLATFORM = "c_platform";
    public static final String COL_YEAR = "c_year";
    public static final String COL_FORMAT = "c_format";
    public static final String COL_STATUS = "c_status";
    public static final String COL_PROGRESS = "c_progress";
    public static final String COL_DATE_STARTED = "c_date_started";
    public static final String COL_DATE_COMPLETED = "c_date_completed";
    public static final String COL_NOTES = "c_notes";
    public static final String COL_RATING = "c_rating";
    public static final String COL_IMAGE = "c_image_uri";
}
