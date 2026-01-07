package com.guilhermegabriel.projetopdm;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.io.InputStream;

public class AddEditGameActivity extends AppCompatActivity {

    // ... (variáveis de instância inalteradas)
    private EditText etTitle, etYear, etNotes, etCustomProgress;
    private Spinner spinnerPlatform, spinnerStatus;
    private RadioGroup rgFormat;
    private RadioButton rbPhysical, rbDigital;
    private SeekBar sbProgress;
    private TextView tvProgressLabel;
    private Button btnSave, btnCancel;

    private ImageView ivCoverPreview;
    private Button btnChooseImage;
    private RatingBar ratingBar;

    private Button btnDateStarted, btnDateCompleted;

    private GamesDB gamesDB;
    private Game currentGame; // Para saber se estamos editando ou adicionando
    // borrowerName e empréstimos temporariamente desativados

    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private Uri selectedImageUri = null;

    private Calendar pickedStartDate = null;
    private Calendar pickedCompletedDate = null;

    // Track previous spinner selection (kept for UI consistency)
    private String previousStatusSelection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_game);

        gamesDB = new GamesDB(this);
        initViews();
        setupSpinners();

        // Initialize previousStatusSelection from current spinner value to avoid immediate dialog
        previousStatusSelection = spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString() : "";

        setupListeners();

        if (getIntent().hasExtra("GAME_EXTRA")) {
            currentGame = (Game) getIntent().getSerializableExtra("GAME_EXTRA");
            setTitle("Editar Jogo");
            populateForm();
            // after populating, ensure previousStatusSelection matches current
            previousStatusSelection = currentGame.getStatus() != null ? currentGame.getStatus() : previousStatusSelection;
        } else {
            setTitle("Adicionar Jogo");
            rbPhysical.setChecked(true);
            updateProgressState(spinnerStatus.getSelectedItem().toString());
        }
    }

    // ... (initViews, setupSpinners, etc.)
     private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etYear = findViewById(R.id.et_year);
        etNotes = findViewById(R.id.et_notes);
        etCustomProgress = findViewById(R.id.et_custom_progress);
        spinnerPlatform = findViewById(R.id.spinner_platform);
        spinnerStatus = findViewById(R.id.spinner_status);
        rgFormat = findViewById(R.id.rg_format);
        rbPhysical = findViewById(R.id.rb_physical);
        rbDigital = findViewById(R.id.rb_digital);
        sbProgress = findViewById(R.id.sb_progress);
        tvProgressLabel = findViewById(R.id.tv_progress_label);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        ivCoverPreview = findViewById(R.id.iv_cover_preview);
        btnChooseImage = findViewById(R.id.btn_choose_image);
        ratingBar = findViewById(R.id.rating_bar);

        btnDateStarted = findViewById(R.id.btn_date_started);
        btnDateCompleted = findViewById(R.id.btn_date_completed);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> platformAdapter = ArrayAdapter.createFromResource(this, R.array.platforms, android.R.layout.simple_spinner_item);
        platformAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlatform.setAdapter(platformAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveGame());
        btnCancel.setOnClickListener(v -> finish());

        btnChooseImage.setOnClickListener(v -> chooseImage());

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvProgressLabel.setText("Progresso (" + progress + "%)");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        btnDateStarted.setOnClickListener(v -> showDatePicker(true));
        btnDateCompleted.setOnClickListener(v -> showDatePicker(false));

        // Temporarily disable loan prompt: simply update progress/rating visibility on status change
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();
                updateProgressState(selectedStatus);

                // Rating visible for all states except 'Por Jogar'
                if ("Por Jogar".equals(selectedStatus)) {
                    ratingBar.setVisibility(View.GONE);
                } else {
                    ratingBar.setVisibility(View.VISIBLE);
                }

                // Date buttons: start disabled when Por Jogar
                btnDateStarted.setEnabled(!"Por Jogar".equals(selectedStatus));
                // Completion date only allowed for Concluído or Emprestado
                btnDateCompleted.setEnabled("Concluído".equals(selectedStatus) || "Emprestado".equals(selectedStatus));

                // Custom progress only when Concluído
                if ("Concluído".equals(selectedStatus)) {
                    etCustomProgress.setVisibility(View.VISIBLE);
                    // keep SeekBar disabled but show potential custom value
                    sbProgress.setEnabled(false);
                } else {
                    etCustomProgress.setVisibility(View.GONE);
                }

                previousStatusSelection = selectedStatus;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void showDatePicker(final boolean isStart) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(Calendar.YEAR, y);
            picked.set(Calendar.MONTH, m);
            picked.set(Calendar.DAY_OF_MONTH, d);
            if (isStart) {
                pickedStartDate = picked;
                btnDateStarted.setText("Início: " + (d < 10 ? "0"+d : d) + "/" + (m+1 < 10 ? "0"+(m+1) : (m+1)) + "/" + y);
            } else {
                pickedCompletedDate = picked;
                btnDateCompleted.setText("Conclusão: " + (d < 10 ? "0"+d : d) + "/" + (m+1 < 10 ? "0"+(m+1) : (m+1)) + "/" + y);
            }
        }, year, month, day);
        dpd.show();
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                selectedImageUri = uri;
                ivCoverPreview.setImageURI(uri);

                // Persist permission to read this URI
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (takeFlags != 0) {
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                }
            }
        }
    }

    private void saveGame() {
        if (TextUtils.isEmpty(etTitle.getText().toString())) {
            AmUtil.showToast(this, "O título é obrigatório.");
            etTitle.requestFocus();
            return;
        }

        boolean isEditing = currentGame != null;
        Game gameToSave = isEditing ? currentGame : new Game();
        String oldStatus = isEditing ? gameToSave.getStatus() : "";
        String newStatus = spinnerStatus.getSelectedItem().toString();

        // Preenche o objeto Game com os dados do formulário
        gameToSave.setTitle(etTitle.getText().toString());
        gameToSave.setPlatform(spinnerPlatform.getSelectedItem().toString());
        String yearStr = etYear.getText().toString();
        if (!TextUtils.isEmpty(yearStr)) {
            try { gameToSave.setYear(Integer.parseInt(yearStr)); } catch (NumberFormatException ignored) { }
        }
        int selectedFormatId = rgFormat.getCheckedRadioButtonId();
        gameToSave.setFormat(selectedFormatId == R.id.rb_physical ? "Físico" : "Digital");
        gameToSave.setStatus(newStatus);

        // Progress: priority to custom progress when Concluído
        if ("Concluído".equals(newStatus) && etCustomProgress.getVisibility() == View.VISIBLE) {
            String custom = etCustomProgress.getText().toString();
            if (!TextUtils.isEmpty(custom)) {
                try {
                    int customVal = Integer.parseInt(custom);
                    if (customVal < 1) customVal = 1;
                    // allow very large values (e.g., 112%) as user said; cap at 500
                    if (customVal > 500) customVal = 500;
                    gameToSave.setProgress(customVal);
                } catch (NumberFormatException nfe) {
                    gameToSave.setProgress(sbProgress.getProgress());
                }
            } else {
                gameToSave.setProgress(sbProgress.getProgress());
            }
        } else {
            gameToSave.setProgress(sbProgress.getProgress());
        }

        gameToSave.setNotes(etNotes.getText().toString());

        // Rating: only if status != Por Jogar
        if (!newStatus.equals("Por Jogar")) {
            gameToSave.setRating((int) ratingBar.getRating());
        } else {
            gameToSave.setRating(0);
        }

        // Image: if user selected an image use it; otherwise construct a placeholder remote image URL
        if (selectedImageUri != null) {
            gameToSave.setImageUri(selectedImageUri.toString());
        } else {
            // Se não houver imagem, constrói URL de placeholder com título+plataforma
            try {
                String text = etTitle.getText().toString() + " " + spinnerPlatform.getSelectedItem().toString();
                String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.name());
                String placeholderUrl = "https://via.placeholder.com/300x300.png?text=" + encoded;
                gameToSave.setImageUri(placeholderUrl);
            } catch (Exception e) {
                gameToSave.setImageUri(null);
            }
        }

        // Datas: use picked dates if present, otherwise apply automatic logic
        if (pickedStartDate != null) {
            gameToSave.setDateStarted(pickedStartDate);
        }
        if (pickedCompletedDate != null) {
            gameToSave.setDateCompleted(pickedCompletedDate);
        }

        // Datas automáticas: aplicar antes de persistir alterações (considerando oldStatus)
        if (!isEditing) { // Jogo novo
            if (newStatus.equals("A Jogar") && gameToSave.getDateStarted() == null) gameToSave.setDateStarted(Calendar.getInstance());
            if (newStatus.equals("Concluído") && gameToSave.getDateCompleted() == null) gameToSave.setDateCompleted(Calendar.getInstance());
        } else { // Jogo existente
            if (newStatus.equals("A Jogar") && !oldStatus.equals("A Jogar") && gameToSave.getDateStarted() == null) {
                gameToSave.setDateStarted(Calendar.getInstance());
            }
            if (newStatus.equals("Concluído") && !oldStatus.equals("Concluído")) {
                if (gameToSave.getDateCompleted() == null) gameToSave.setDateCompleted(Calendar.getInstance());
            }
            if (!newStatus.equals("Concluído") && oldStatus.equals("Concluído")) {
                gameToSave.setDateCompleted(null);
            }
        }

        // Persistir o Game primeiro (inserir ou atualizar)
        boolean persisted;
        try {
            if (isEditing) {
                persisted = gamesDB.updateGame(gameToSave);
                if (!persisted) {
                    AmUtil.showToast(this, "Erro ao atualizar o jogo.");
                    return;
                }
            } else {
                long newId = gamesDB.insertGame(gameToSave);
                if (newId == -1) {
                    AmUtil.showToast(this, "Erro ao inserir o jogo.");
                    return;
                }
                gameToSave.setId((int) newId);
                persisted = true;
            }
        } catch (Exception e) {
            // Se houver qualquer problema com a DB ao salvar o jogo, mostrar o erro e abortar.
            String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
            AmUtil.showToast(this, "Erro ao salvar o jogo: " + msg);
            e.printStackTrace();
            return;
        }

        // Empréstimos estão temporariamente desativados; reintroduzir mais tarde.

        if (persisted) {
            AmUtil.showToast(this, "Jogo salvo com sucesso!");
            finish();
        }
    }
    private void updateProgressState(String status) {
        switch (status) {
            case "A Jogar":
                sbProgress.setEnabled(true);
                break;
            case "Concluído":
                sbProgress.setEnabled(false);
                sbProgress.setProgress(100);
                break;
            case "Por Jogar":
            case "Abandonado":
            default:
                sbProgress.setEnabled(false);
                sbProgress.setProgress(0);
                break;
        }
    }

    private void populateForm() {
        if (currentGame == null) return;

        etTitle.setText(currentGame.getTitle());
        etYear.setText(String.valueOf(currentGame.getYear()));
        etNotes.setText(currentGame.getNotes());

        // Selecionar item no Spinner de plataforma
        setSpinnerSelection(spinnerPlatform, currentGame.getPlatform());

        // Selecionar item no Spinner de status
        setSpinnerSelection(spinnerStatus, currentGame.getStatus());

        // Selecionar RadioButton de formato
        if ("Físico".equalsIgnoreCase(currentGame.getFormat())) {
            rbPhysical.setChecked(true);
        } else if ("Digital".equalsIgnoreCase(currentGame.getFormat())) {
            rbDigital.setChecked(true);
        }

        sbProgress.setProgress(currentGame.getProgress());
        updateProgressState(currentGame.getStatus()); // Aplica a lógica de enable/disable

        // Rating
        if (currentGame.getRating() > 0) {
            ratingBar.setRating(currentGame.getRating());
            ratingBar.setVisibility(View.VISIBLE);
        }

        // Image preview
        if (currentGame.getImageUri() != null) {
            String uri = currentGame.getImageUri();
            if (uri.startsWith("content:") || uri.startsWith("file:")) {
                ivCoverPreview.setImageURI(Uri.parse(uri));
            } else if (uri.startsWith("http")) {
                // load remote image asynchronously for preview
                final String imageUrl = uri;
                new Thread(() -> {
                    try {
                        URL url = new URL(imageUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        final Bitmap bmp = BitmapFactory.decodeStream(input);
                        if (bmp != null) {
                            runOnUiThread(() -> ivCoverPreview.setImageBitmap(bmp));
                        }
                    } catch (Exception ignored) { }
                }).start();
            }
        }

        // Dates
        if (currentGame.getDateStarted() != null) {
            pickedStartDate = currentGame.getDateStarted();
            int d = pickedStartDate.get(Calendar.DAY_OF_MONTH);
            int m = pickedStartDate.get(Calendar.MONTH);
            int y = pickedStartDate.get(Calendar.YEAR);
            btnDateStarted.setText("Início: " + (d < 10 ? "0"+d : d) + "/" + (m+1 < 10 ? "0"+(m+1) : (m+1)) + "/" + y);
        }
        if (currentGame.getDateCompleted() != null) {
            pickedCompletedDate = currentGame.getDateCompleted();
            int d = pickedCompletedDate.get(Calendar.DAY_OF_MONTH);
            int m = pickedCompletedDate.get(Calendar.MONTH);
            int y = pickedCompletedDate.get(Calendar.YEAR);
            btnDateCompleted.setText("Conclusão: " + (d < 10 ? "0"+d : d) + "/" + (m+1 < 10 ? "0"+(m+1) : (m+1)) + "/" + y);
        }

        // Custom progress: show if >100 or if status Concluído and progress not equal 100
        if ("Concluído".equals(currentGame.getStatus()) && currentGame.getProgress() != 100) {
            etCustomProgress.setVisibility(View.VISIBLE);
            etCustomProgress.setText(String.valueOf(currentGame.getProgress()));
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }
    // loan functionality temporarily removed
 }
