package com.example.sabhatia.assignment2;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button takePictureButton;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int SEARCH_ACTIVITY_REQUEST_CODE = 0;
    public static int AUTO_ENABLED = 0;
    private Bitmap mImageBitmap;
    private ImageView imageView;
    private String mCurrentPhotoPath;
    private Uri imageUri;
    private Button button2;
    private Button button3, calender;
    private ArrayList<String> photoGallery;
    private int currentPhotoIndex = 0;
    private Button auto, upload;
    private static final int SPEECH_REQUEST_CODE = 0;
    private BackgroundTask btask;
    private String upLoadServerUri = null;


    private int serverResponseCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        Log.i( "SCREEN NAME", "Setting screen name: " + this.getLocalClassName() );
//        MyApp.tracker().setScreenName( "Image~" + this.getLocalClassName() );
  //      MyApp.tracker().send( new HitBuilders.ScreenViewBuilder().build() );

        takePictureButton = findViewById( R.id.button );
        imageView = findViewById( R.id.imageView );
        button2 = findViewById( R.id.button2 ); //left button
        photoGallery = new ArrayList<String>();

        button3 = findViewById( R.id.button3 );
        auto = findViewById( R.id.button4 );
        button3.setOnClickListener( righOnClick );
        calender = findViewById( R.id.button8 );
        populateGallery();
        AUTO_ENABLED = 0;

        upload = findViewById( R.id.button7 );
        upLoadServerUri = "http://10.0.2.2/UploadToServer.php";

        button2.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Button3 clicked", Toast.LENGTH_SHORT).show();
                //disable auto scroll

                if (AUTO_ENABLED == 1) {
                    btask.cancel( true );
                    AUTO_ENABLED = 0;
                    auto.setEnabled( true );

                } else {
                    --currentPhotoIndex;

                    changePic();
                }

                HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
                eventBuilder.setAction("Swipe left").setCategory("Click");
                MyApp.tracker().send(eventBuilder.build());


            }
        } );

        upload.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // dialog = ProgressDialog.show(UploadToServer.this, "", "Uploading file...", true);

                HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
                eventBuilder.setAction("upload").setCategory("Click");
                MyApp.tracker().send(eventBuilder.build());


                new Thread( new Runnable() {
                    public void run() {
                        runOnUiThread( new Runnable() {
                            public void run() {
                                // messageText.setText("uploading started.....");
                            }
                        } );

                        uploadFile();

                    }
                } ).start();
            }
        } );
    }


/*
ROIGHT BUTTON - ANONYMOUS INSTANCE
 */

    private View.OnClickListener righOnClick = new View.OnClickListener() {

        public void onClick(View view) {
            //Toast.makeText(MainActivity.this, "Right cloicked", Toast.LENGTH_SHORT).show();

            //disable auto scroll


            HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
            eventBuilder.setAction("Swipe right").setCategory("Click");
            MyApp.tracker().send(eventBuilder.build());


            if (AUTO_ENABLED == 1) {
                btask.cancel( true );
                AUTO_ENABLED = 0;
                auto.setEnabled( true );

            } else {
                ++currentPhotoIndex;
                changePic();
            }
        }
    };


    public void changePic() {

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction("change pic").setCategory("Click");
        MyApp.tracker().send(eventBuilder.build());

        if (currentPhotoIndex < 0)
            currentPhotoIndex = 0;
        if (currentPhotoIndex >= photoGallery.size())
            currentPhotoIndex = photoGallery.size() - 1;

        if (currentPhotoIndex > 0) {
            mCurrentPhotoPath = photoGallery.get( currentPhotoIndex );
            Log.d( "phpotoleft, size", Integer.toString( photoGallery.size() ) );
            Log.d( "photoleft, index", Integer.toString( currentPhotoIndex ) );
            displayPhoto( mCurrentPhotoPath );
        }
    }

    private ArrayList<String> populateGallery() {
        File file = new File( Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.sabhatia.assignment2/files/Pictures" );

        File[] fList = file.listFiles();
        if (fList != null) {
            for (File f : file.listFiles()) {
                photoGallery.add( f.getPath() );
            }
        }
        return photoGallery;
    }

    private void displayPhoto(String path) {
        ImageView iv = (ImageView) findViewById( R.id.imageView );
        iv.setImageBitmap( BitmapFactory.decodeFile( path ) );
    }


    @Override
    public void onResume() {

        super.onResume();
    }


    /*
    Snap button
     */
    public void buttonClick(View view) {
        //Toast.makeText(this, "SNAP CLICKED", Toast.LENGTH_SHORT).show();

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction("take pic").setCategory("Click");
        MyApp.tracker().send(eventBuilder.build());


        Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );

        if (intent.resolveActivity( getPackageManager() ) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d( "FileCreation", "Failed" );
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile( this,
                        "com.example.sabhatia.assignment2.pictures.fileprovider",
                        photoFile );
                intent.putExtra( MediaStore.EXTRA_OUTPUT, photoURI );
                startActivityForResult( intent, REQUEST_IMAGE_CAPTURE );
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);*/
            displayPhoto( mCurrentPhotoPath );

        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText( this, "Picture was not taken", Toast.LENGTH_SHORT );
        }
        populateGallery();


        if (requestCode == SPEECH_REQUEST_CODE) {
            Toast.makeText( this, "voice recognition", Toast.LENGTH_SHORT ).show();
            if (resultCode == RESULT_OK) {
                ArrayList<String> voice = data.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );

                Toast.makeText( this, "you said " + voice.get( 0 ), Toast.LENGTH_SHORT ).show();

                if (voice.get( 0 ).contains( "next" ) || voice.get( 0 ).contains( "right" )) {
                    ++currentPhotoIndex;
                    changePic();
                } else {
                    --currentPhotoIndex;
                    changePic();
                }
            }
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new Date() );
        String imageFileName = "JPEG_" + timeStamp + "_";
        File dir = getExternalFilesDir( Environment.DIRECTORY_PICTURES );
        File image = File.createTempFile( imageFileName, ".jpg", dir );
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d( "createImageFile", mCurrentPhotoPath );
        return image;
    }

    //handles auto scroll  button
    public void auto(View view) {

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction("auto swipe").setCategory("Click");
        MyApp.tracker().send(eventBuilder.build());

        if (currentPhotoIndex == 0) {
            mCurrentPhotoPath = photoGallery.get( currentPhotoIndex );
            displayPhoto( mCurrentPhotoPath );
            Toast.makeText( this, "size was 0", Toast.LENGTH_SHORT ).show();
        }
        AUTO_ENABLED = 1;

        // disable button
        auto.setEnabled( false );

        btask = new BackgroundTask();
        btask.execute();


    }


    //handles switch button
    public void switchActivity(View view) {
        //switching activity

        Intent canvasIntent = new Intent( this, CanvasActivity.class );
        //send the same image to this activity


        if (mCurrentPhotoPath == null) {
            Toast.makeText( this, "Must display a pic first", Toast.LENGTH_SHORT ).show();
        } else {
            canvasIntent.putExtra( "path", mCurrentPhotoPath );
            canvasIntent.putExtra( "index", currentPhotoIndex );
            canvasIntent.putExtra( "size", photoGallery.size() );

            startActivity( canvasIntent );
        }


    }

    public void audio(View view) {

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction("audio click").setCategory("Click");
        MyApp.tracker().send(eventBuilder.build());


        Intent vrIntent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH );
        vrIntent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
        vrIntent.putExtra( RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault() );
        //vrIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak slowly and enunciate clearly");
        startActivityForResult( vrIntent, SPEECH_REQUEST_CODE );
    }

    public void uploadFile() {

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction("upload pic").setCategory("Click");
        MyApp.tracker().send(eventBuilder.build());

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        mCurrentPhotoPath = photoGallery.get( currentPhotoIndex );
        File sourceFile = new File( mCurrentPhotoPath );

        if (!sourceFile.isFile()) {

            Log.e( "uploadFile", "Source File not exist :"
                    + mCurrentPhotoPath );

        } else {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream( sourceFile );
                URL url = new URL( upLoadServerUri );

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput( true ); // Allow Inputs
                conn.setDoOutput( true ); // Allow Outputs
                conn.setUseCaches( false ); // Don't use a Cached Copy
                conn.setRequestMethod( "POST" );
                conn.setRequestProperty( "Connection", "Keep-Alive" );
                conn.setRequestProperty( "ENCTYPE", "multipart/form-data" );
                conn.setRequestProperty( "Content-Type", "multipart/form-data;boundary=" + boundary );
                conn.setRequestProperty( "uploaded_file", mCurrentPhotoPath );

                dos = new DataOutputStream( conn.getOutputStream() );

                dos.writeBytes( twoHyphens + boundary + lineEnd );
                dos.writeBytes( "Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + mCurrentPhotoPath + "\"" + lineEnd );

                dos.writeBytes( lineEnd );

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min( bytesAvailable, maxBufferSize );
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read( buffer, 0, bufferSize );

                while (bytesRead > 0) {

                    dos.write( buffer, 0, bufferSize );
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min( bytesAvailable, maxBufferSize );
                    bytesRead = fileInputStream.read( buffer, 0, bufferSize );

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes( lineEnd );
                dos.writeBytes( twoHyphens + boundary + twoHyphens + lineEnd );

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i( "uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode );

                if (serverResponseCode == 200) {
                    Log.i( "uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode );


                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                Log.e( "Upload file to server", "error: " + ex.getMessage(), ex );
            } catch (Exception e) {

                e.printStackTrace();

                Log.e( "Upload file to srvr exp", "Exception : " + e.getMessage(), e );
            }


        }
    }

    public void calendarActivity(View view) {

        Intent canvasIntent = new Intent( this, calendarPageActivity.class );
        //send the same image to this activity
        startActivity( canvasIntent );


    }


    private class BackgroundTask extends AsyncTask<Void, Void, Void> {

        private boolean end = false;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                while (AUTO_ENABLED == 1) {

                    System.out.println( "curr" + currentPhotoIndex );
                    System.out.println( "size" + photoGallery.size() );
                    //.makeText(MainActivity.this, "current " + currentPhotoIndex, Toast.LENGTH_SHORT).show();
                    if (currentPhotoIndex <= 0) {
                        end = false;
                        System.out.println( "in less than 0" );
                    } else if (currentPhotoIndex <= photoGallery.size() && end == false) {
                        currentPhotoIndex++;
                        System.out.println( "in less than size" );

                    } else if (currentPhotoIndex > photoGallery.size()) {
                        end = true;
                        currentPhotoIndex = photoGallery.size();
                        currentPhotoIndex--;
                        System.out.println( "in greater than size" );
                    } else {
                        currentPhotoIndex--;
                        System.out.println( "in last" );
                    }

                    mCurrentPhotoPath = photoGallery.get( currentPhotoIndex );
                    displayPhoto( mCurrentPhotoPath );

                    Thread.sleep( 4000 );
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate( values );
        }

        @Override
        protected void onPostExecute(Void param) {

        }

        protected void onPause() {

        }

    }
}

