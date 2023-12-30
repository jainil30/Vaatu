package com.jainil.vaatu;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageViewActivity extends AppCompatActivity {

    ImageView imageView,downloadImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imageView = findViewById(R.id.fullScreenImageView);
        downloadImageView = findViewById(R.id.imageViewActivityDownloadBtn);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_FULLSCREEN );

        String url = getIntent().getStringExtra("imageUrl");
        Picasso.get().load(url).into(imageView);

        downloadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadImage(url);
            }
        });

    }

    private void DownloadImage(String url) {
        Uri uri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setVisibleInDownloadsUi(true);
        Date date = new Date();
        Toast.makeText(this, "" + date.toString(), Toast.LENGTH_SHORT).show();
        request.setDestinationInExternalFilesDir(this, DIRECTORY_DOWNLOADS, date.toString() + ".jpg");
        Toast.makeText(this, "Directory : " + DIRECTORY_DOWNLOADS , Toast.LENGTH_LONG ).show();
        downloadManager.enqueue(request);

    }


}