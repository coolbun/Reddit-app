package com.example.rishabhja.reddit.posts;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rishabhja.reddit.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class LoadImageActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_DATE = 0;
    private static final String TAG = "LoadImageActivity";
    private static final int CAPTURE_IMAGE = 2;
    private File photoLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_image);


        Button button = (Button) findViewById(R.id.open_image);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);
//                intent.putExtra(Intent.EXTRA_TEXT, "extra text");
//                intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
//                intent.createChooser(intent, getString(R.string.intent_chooser_text));
                startActivityForResult(intent, REQUEST_CONTACT);
            }
        });
        //Environment.getExternalStorageDirectory()


        File externalFilesDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        photoLocation = new File(externalFilesDir, "Picture");

        button = (Button) findViewById(R.id.camera);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoLocation));
                startActivityForResult(intent, CAPTURE_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_DATE) {
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            //What fields of contct are required
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };

            //perform query on contentresolver
            Cursor c = this.getContentResolver().query(
                    contactUri, queryFields, null, null, null
            );
            try {
                if (c.getCount() == 0)
                    return;
                c.moveToFirst();
                String contact = c.getString(0);
                Log.d(TAG, contact);
                Toast.makeText(this, contact, 300).show();
            } finally {
                c.close();
            }
        } else if (requestCode == CAPTURE_IMAGE) {
            ImageView imageView = (ImageView) findViewById(R.id.camera_image);
            Picasso.with(this).load(photoLocation)
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }
    }
}
