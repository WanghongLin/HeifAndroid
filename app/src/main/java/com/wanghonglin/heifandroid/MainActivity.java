/*
 * Copyright 2019 wanghonglin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanghonglin.heifandroid;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_REQUEST_CODE_CHOOSE_IMAGE = 284;
    private static final int PERMISSION_REQUEST_CODE_ENCODE = 224;
    private static final int PERMISSION_REQUEST_CODE_DECODE = 301;
    private static final int PERMISSION_REQUEST_CODE_ENCODE_YUV = 502;

    private static final String TAG = "MainActivity";
    private static final String ENCODE_OUTPUT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "output.heic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Switch assetSwitch = ((Switch) findViewById(R.id.heif_asset_switch));
        assetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                USE_HEIF_FILE_FROM_ASSETS = isChecked;
            }
        });

        findViewById(R.id.heif_encode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canAcquireReadWriteStoragePermission(PERMISSION_REQUEST_CODE_ENCODE)) {
                    choosePictureFromSystemGallery();
                }
            }
        });

        findViewById(R.id.heif_decode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canAcquireReadWriteStoragePermission(PERMISSION_REQUEST_CODE_DECODE)) {
                    Heif2BitmapTask.fire(MainActivity.this);
                }
            }
        });

        findViewById(R.id.heif_yuv_encode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canAcquireReadWriteStoragePermission(PERMISSION_REQUEST_CODE_ENCODE_YUV)) {
                    YUVToHEIFTask.fire(MainActivity.this);
                }
            }
        });
    }

    private boolean canAcquireReadWriteStoragePermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, requestCode);
            return false;
        } else {
            return true;
        }
    }

    private void choosePictureFromSystemGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(Intent.createChooser(intent, "Select image to encode to heif"), ACTIVITY_REQUEST_CODE_CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQUEST_CODE_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
                    new Bitmap2HeifTask(bitmap, this).execute();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class YUVToHEIFTask extends AsyncTask<Void, Void, String> {

        private final WeakReference<MainActivity> activity;

        public static void fire(MainActivity activity) {
            new YUVToHEIFTask(activity).execute();
        }

        private YUVToHEIFTask(MainActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (activity.get() != null) {
                activity.get().showProgress();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            HEIFUtils.encodeYUVToHEIF(HEIFUtils.bytesOfAssetName(activity.get(), "input.yuv"),
                    1080, 2160, ENCODE_OUTPUT_PATH);
            return ENCODE_OUTPUT_PATH;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (activity.get() != null) {
                activity.get().hideProgress();
            }
            Toast.makeText(activity.get(), "YUV to HEIF, write to " + ENCODE_OUTPUT_PATH,
                    Toast.LENGTH_LONG).show();
        }
    }

    private static class Heif2BitmapTask extends AsyncTask<Void, Void, Bitmap> {

        private final WeakReference<MainActivity> activity;
        private long startTimeMillis;

        public static void fire(MainActivity activity) {
            new Heif2BitmapTask(activity).execute();
        }

        private Heif2BitmapTask(MainActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (activity.get() != null) {
                activity.get().showProgress();
            }
            startTimeMillis = System.currentTimeMillis();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            if (activity.get() != null) {
                return HEIFUtils.decodeHEIFToBitmap(USE_HEIF_FILE_FROM_ASSETS ?
                        HEIFUtils.accessiblePathOfAssetName(activity.get(), "input.heic") :
                        ENCODE_OUTPUT_PATH);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null && activity.get() != null) {
                activity.get().hideProgress();

                FrameLayout heifLayout = (FrameLayout) LayoutInflater.from(activity.get()).inflate(R.layout.heif_layout, null);
                ImageView imageView = heifLayout.findViewById(R.id.heif_layout_image);
                imageView.setImageBitmap(bitmap);
                TextView textView = heifLayout.findViewById(R.id.heif_layout_text);

                final String imageInfo = "File size: " + new File(ENCODE_OUTPUT_PATH).length() / 1024 + "KB\n" +
                        "width=" + bitmap.getWidth() + ", height=" + bitmap.getHeight() + "\n" +
                        "decode timeMillis=" + (System.currentTimeMillis()-startTimeMillis) + "\n";

                textView.setText(imageInfo);
                textView.setTextColor(Color.WHITE);

                new AlertDialog.Builder(activity.get())
                        .setView(heifLayout)
                        .show();
            }
        }
    }

    private static boolean USE_HEIF_FILE_FROM_ASSETS = false;

    private void showProgress() {
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
    }

    private static class Bitmap2HeifTask extends AsyncTask<Void, Void, Long> {

        private long startTime;
        private WeakReference<MainActivity> context;

        private Bitmap bitmap;

        public Bitmap2HeifTask(Bitmap bitmap, MainActivity activity) {
            this.bitmap = bitmap;
            this.context = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (context.get() != null) {
                context.get().showProgress();
            }
        }

        @Override
        protected Long doInBackground(Void... voids) {
            startTime = System.currentTimeMillis();
            HEIFUtils.encodeBitmapToHEIF(bitmap, ENCODE_OUTPUT_PATH);
            return System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if (context.get() != null) {
                context.get().hideProgress();
                Toast.makeText(context.get(), "Encode done, took " + (aLong-startTime) + " " + ENCODE_OUTPUT_PATH, Toast.LENGTH_LONG)
                        .show();
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE_ENCODE && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            choosePictureFromSystemGallery();
        }

        if (requestCode == PERMISSION_REQUEST_CODE_DECODE && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            Heif2BitmapTask.fire(this);
        }

        if (requestCode == PERMISSION_REQUEST_CODE_ENCODE_YUV && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            YUVToHEIFTask.fire(this);
        }
    }
}
