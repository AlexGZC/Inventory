package io.github.leonawicz.inventory;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.leonawicz.inventory.invdb.DbBitmapUtility;
import io.github.leonawicz.inventory.invdb.InvCursorAdapter;
import io.github.leonawicz.inventory.invdb.InvDbHelper;

public class AddProductActivity extends AppCompatActivity {

    InvDbHelper dbHelper;

    static class ViewHolder{
        EditText newPart;
        EditText newSupplier;
        EditText newQuantity;
        EditText newCost;
        EditText newPrice;
        ImageView userImageView;
    }

    ViewHolder holder;

    private final static int REQUEST_CAMERA = 1;
    private final static int SELECT_FILE = 2;
    private String imageTaskChoice;
    Bitmap currentBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);

        holder = new ViewHolder();
        holder.newPart = (EditText) findViewById(R.id.input_part_name);
        holder.newSupplier = (EditText) findViewById(R.id.input_part_supplier);
        holder.newQuantity = (EditText) findViewById(R.id.input_part_qty);
        holder.newCost = (EditText) findViewById(R.id.input_part_cost);
        holder.newPrice = (EditText) findViewById(R.id.input_part_price);
        holder.userImageView = (ImageView) findViewById(R.id.part_image);

        DbApplication app = (DbApplication) getApplicationContext();
        dbHelper = app.dbHelper;

        Button btn = (Button) findViewById(R.id.input_part_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPart = holder.newPart.getText().toString();
                String newSupplier = holder.newSupplier.getText().toString();
                int newQuantity = -1;
                double newCost = -1;
                double newPrice = -1;
                try {
                    newQuantity = Integer.valueOf(holder.newQuantity.getText().toString());
                    newCost = Double.valueOf(holder.newCost.getText().toString());
                    newPrice = Double.valueOf(holder.newPrice.getText().toString());
                    Log.v("AddProduct", "newBitmap is null: " + (currentBitmap == null));
                    if(newPart.equals("") || newSupplier.equals("") ||
                            newQuantity < 1 || newQuantity < 1 || newQuantity < 1 || currentBitmap == null){
                        Toast.makeText(AddProductActivity.this,
                                R.string.part_added_failure, Toast.LENGTH_LONG).show();
                    } else {
                        long inserted = dbHelper.insertTableRow(
                                newPart, newSupplier, newQuantity, newCost, newPrice, currentBitmap);
                        finish();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(AddProductActivity.this,
                            R.string.invalid_numbers, Toast.LENGTH_SHORT).show();
                }

            }
        });

        Button imgBtn = (Button) findViewById(R.id.input_image_btn);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //*
    // Methods below are borrowed and adapted from
    // http://www.theappguruz.com/blog/android-take-photo-camera-gallery-code-sample
    // for this Android apps Udacity inventory app project
    //*

    private void selectImage() {
        final CharSequence[] items = {
                getString(R.string.im_use_camera),
                getString(R.string.im_use_gallery),
                getString(R.string.im_cancel) };
        AlertDialog.Builder builder = new AlertDialog.Builder(AddProductActivity.this);
        builder.setTitle(getString(R.string.add_part_image));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(AddProductActivity.this);
                if (items[item].equals(getString(R.string.im_use_camera))) {
                    imageTaskChoice=getString(R.string.im_use_camera);
                    if(result)
                        cameraIntent();
                } else if (items[item].equals(getString(R.string.im_use_gallery))) {
                    imageTaskChoice=getString(R.string.im_use_gallery);
                    if(result)
                        galleryIntent();
                } else if (items[item].equals(getString(R.string.im_cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.im_select_file)),SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(imageTaskChoice.equals(getString(R.string.im_use_camera)))
                        cameraIntent();
                    else if(imageTaskChoice.equals(getString(R.string.im_use_gallery)))
                        galleryIntent();
                } else {
                    Toast.makeText(this, getString(R.string.im_permission_denied), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                bm = resize(bm, 100, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        currentBitmap = bm;
        holder.userImageView.setImageBitmap(bm);
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentBitmap = thumbnail;
        holder.userImageView.setImageBitmap(thumbnail);
    }

    public static Bitmap resize(Bitmap originalImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / originalImage.getWidth(),
                (float) maxImageSize / originalImage.getHeight());
        int width = Math.round((float) ratio * originalImage.getWidth());
        int height = Math.round((float) ratio * originalImage.getHeight());

        Bitmap bm = Bitmap.createScaledBitmap(originalImage, width,
                height, filter);
        return bm;
    }

}