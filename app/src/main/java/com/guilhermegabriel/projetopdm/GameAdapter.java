package com.guilhermegabriel.projetopdm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameAdapter extends ArrayAdapter<Game> {

    private final Context mContext;
    private final int mResource;
    private final HashMap<String, Bitmap> imageCache = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public GameAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Game> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Game game = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (convertView == null) {
            convertView = inflater.inflate(mResource, parent, false);
        }

        ImageView ivPlatformIcon = convertView.findViewById(R.id.iv_platform_icon);
        TextView tvGameTitle = convertView.findViewById(R.id.tv_game_title);
        TextView tvPlatformFormat = convertView.findViewById(R.id.tv_platform_format);
        TextView tvStatusProgress = convertView.findViewById(R.id.tv_status_progress);
        TextView tvRatingDate = convertView.findViewById(R.id.tv_rating_date);

        if (game != null) {
            tvGameTitle.setText(game.getTitle());

            String platformAndFormat = game.getPlatform() + " | " + game.getFormat();
            tvPlatformFormat.setText(platformAndFormat);

            String statusAndProgress = game.getStatus() + " | " + game.getProgress() + "% ";
            tvStatusProgress.setText(statusAndProgress);

            String date = "";
            if (game.getDateCompleted() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                date = sdf.format(game.getDateCompleted().getTime());
            }
            String ratingAndDate = "Nota: " + game.getRating() + "/5 | " + date;
            tvRatingDate.setText(ratingAndDate);

            // Lógica de imagem: se for URI local usa setImageURI, se for URL remoto carrega assíncrono
            String imageUri = game.getImageUri();
            ivPlatformIcon.setImageResource(R.mipmap.ic_launcher); // placeholder
            if (imageUri != null && !imageUri.isEmpty()) {
                if (imageUri.startsWith("content:") || imageUri.startsWith("file:")) {
                    try {
                        ivPlatformIcon.setImageURI(Uri.parse(imageUri));
                    } catch (Exception e) {
                        // fallback
                        ivPlatformIcon.setImageResource(R.mipmap.ic_launcher);
                    }
                } else if (imageUri.startsWith("http")) {
                    // check cache
                    if (imageCache.containsKey(imageUri)) {
                        ivPlatformIcon.setImageBitmap(imageCache.get(imageUri));
                    } else {
                        // load asynchronously
                        ivPlatformIcon.setTag(imageUri);
                        executor.submit(() -> {
                            try {
                                URL url = new URL(imageUri);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setDoInput(true);
                                connection.connect();
                                InputStream input = connection.getInputStream();
                                Bitmap bmp = BitmapFactory.decodeStream(input);
                                if (bmp != null) {
                                    imageCache.put(imageUri, bmp);
                                    // set image on UI thread if the view still expects this URI
                                    ((Activity) mContext).runOnUiThread(() -> {
                                        if (imageUri.equals(ivPlatformIcon.getTag())) {
                                            ivPlatformIcon.setImageBitmap(bmp);
                                        }
                                    });
                                }
                            } catch (Exception ignored) { }
                        });
                    }
                }
            }

            // Lógica de cores por status
            int colorId;
            switch (game.getStatus()) {
                case "Por Jogar":       colorId = R.color.status_por_jogar; break;
                case "A Jogar":         colorId = R.color.status_a_jogar; break;
                case "Concluído":       colorId = R.color.status_concluido; break;
                case "Abandonado":      colorId = R.color.status_abandonado; break;
                case "Emprestado":      colorId = R.color.status_emprestado; break;
                default:                colorId = android.R.color.transparent; break;
            }
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, colorId));
        }

        return convertView;
    }
}
