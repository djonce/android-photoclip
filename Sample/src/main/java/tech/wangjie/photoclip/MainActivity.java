package tech.wangjie.photoclip;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import tech.wangjie.library.PhotoClipActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int GALLERY = 0;
    private static final int CAMERA = 1;
    private static final int CLIP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY:
                    Uri uri = data.getData();
                    Log.d(TAG, uri.toString());

                    Cursor cusor = this.getContentResolver().query(uri, null, null, null, null);
                    cusor.moveToFirst();
                    String imagePath = cusor.getString(1);
                    cusor.close();

                    Intent gallery = new Intent(this, PhotoClipActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("path", imagePath);
                    bundle.putInt(PhotoClipActivity.CROP_TYPE, PhotoClipActivity.CORP_KEY_AVATAR);
                    gallery.putExtras(bundle);
                    startActivityForResult(gallery, CLIP);
                    break;

                case CAMERA:
                    if (photoFile != null && photoFile.exists()) {
                        Intent camera = new Intent(this, PhotoClipActivity.class);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("path", photoFile.getPath());
                        bundle1.putInt(PhotoClipActivity.CROP_TYPE, PhotoClipActivity.CORP_KEY_COVER);
                        camera.putExtras(bundle1);
                        startActivityForResult(camera, CLIP);
                    }
                    break;

                case CLIP:
                    String clipImagePath = data.getStringExtra("path");
                    ((ImageView)findViewById(R.id.imageView)).setImageBitmap(BitmapFactory.decodeFile(clipImagePath));
                    break;
            }
        }
    }

    File photoFile;
    public void doCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = new File(getExternalFilesDir(null) + "/temp.jpg");
        if (photoFile.exists()) {
            photoFile.delete();
        }

        try {
            photoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(intent, CAMERA);
    }

    public void doGallery(View view) {
        try{
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, GALLERY);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

}
