package com.example.morales;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class UpdateActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button chooseImageBtn;
    private Button updateBtn;
    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private ImageView chosenImageView;
    private ProgressBar uploadProgressBar;

    private Uri mImageUri;

    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    private String itemKey, imageURL;
    private AwesomeValidation awesomeValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        chooseImageBtn = findViewById(R.id.button_choose_image);
        updateBtn = findViewById(R.id.updateBtn);
        nameEditText = findViewById(R.id.nameEditText);
        descriptionEditText = findViewById ( R.id.descriptionEditText );
        priceEditText = findViewById(R.id.priceEditText);
        quantityEditText = findViewById ( R.id.quantityEditText );
        chosenImageView = findViewById(R.id.chosenImageView);
        uploadProgressBar = findViewById(R.id.progress_bar);

        //RECEIVE DATA FROM ITEMSACTIVITY VIA INTENT
        Intent i=this.getIntent();
        itemKey=i.getExtras().getString("ITEM_KEY");
        String name=i.getExtras().getString("NAME_KEY");
        String nameLower=i.getExtras().getString("NAMELOWER_KEY");
        imageURL=i.getExtras().getString("IMAGE_KEY");
        String description=i.getExtras().getString("DESCRIPTION_KEY");
        String price=i.getExtras().getString("PRICE_KEY");
        String quantity=i.getExtras().getString("QUANTITY_KEY");

        //SET RECEIVED DATA TO TEXTVIEWS AND IMAGEVIEWS
//        nameEditText.setText(name);
//        descriptionEditText.setText(description);
//        priceEditText.setText(price);
//        quantityEditText.setText(quantity);

//        Picasso.with(this).load(mImageUri).resize(512,512).into(chosenImageView);

        //Initialize Validation Style
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        //Adding Validation
        awesomeValidation.addValidation(this, R.id.nameEditText, RegexTemplate.NOT_EMPTY, R.string.err_name);
        //awesomeValidation.addValidation(this, R.id.description, RegexTemplate.NOT_EMPTY, R.string.err_description);
        awesomeValidation.addValidation(this, R.id.priceEditText, "^[1-9]\\d{0,7}(?:\\.\\d{1,4})?|\\.\\d{1,4}$", R.string.err_price);
        awesomeValidation.addValidation(this, R.id.quantityEditText, "^[1-9]\\d*$", R.string.err_quantity);


        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("products_uploads");

        chooseImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(UpdateActivity.this, "An Upload is Still in Progress", Toast.LENGTH_SHORT).show();
                } else {
                    //Check Validation
                    if(awesomeValidation.validate())
                        uploadFile();
                    else
                        Toast.makeText(UpdateActivity.this, "Please Check Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.with(this).load(mImageUri).resize(512,512).into(chosenImageView);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {
            StorageReference fileReference = mStorage.getReferenceFromUrl(imageURL);

            uploadProgressBar.setVisibility(View.VISIBLE);
            uploadProgressBar.setIndeterminate(true);

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot> () {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    uploadProgressBar.setVisibility(View.VISIBLE);
                                    uploadProgressBar.setIndeterminate(false);
                                    uploadProgressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(UpdateActivity.this, "Product Update successful", Toast.LENGTH_SHORT).show();

                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Product upload = new Product(nameEditText.getText().toString().trim(),
                                            nameEditText.getText().toString().trim(),
                                            uri.toString(),
                                            descriptionEditText.getText().toString(),
                                            priceEditText.getText().toString().trim(),
                                            quantityEditText.getText().toString().trim());

                                    mDatabaseRef.child(itemKey).setValue(upload);


                                    uploadProgressBar.setVisibility(View.INVISIBLE);
                                    openImagesActivity();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener () {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            uploadProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(UpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot> () {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            uploadProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "You haven't Selected Any file selected", Toast.LENGTH_SHORT).show();
        }
    }
    private void openImagesActivity(){
        Intent intent = new Intent(this, ItemsActivity.class);
        startActivity(intent);
        finish();
    }
}