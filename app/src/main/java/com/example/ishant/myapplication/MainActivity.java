package com.example.ishant.myapplication;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishant.myapplication.AugustSettings.AugustSettingsActivity;
import com.example.ishant.myapplication.TaskScheduler.AugustTaskSchedulerActivity;

public class MainActivity extends AppCompatActivity implements RecognitionListener,View.OnClickListener {


    //Connected.
    private boolean connected;

    public static Socket socket;

    //Listen
    private boolean listen;

    //String to hold text to send to server.
    private String textToSend;

    //Notification Id for the app.
    private final int NOTIFICATION_ID_RUNNING = 2188;

    //Notification manager to handle notifications.
    private NotificationManager notificationManager;

    //Notification Builder.
    private NotificationCompat.Builder mBuilder;

    //Text View to display the text.
    private TextView sendText;

    //Progress bAr to show voice modulation while listening.
    private ProgressBar progressBar;

    //Speech recognizer to start speech recognition.
    private SpeechRecognizer speech = null;

    //Recognizer Intent.
    private Intent recognizerIntent;

    //Image Button to get voice buton.
    private ImageButton speakButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this);

        speakButton = (ImageButton) findViewById(R.id.speak);

        speakButton.setOnClickListener(this);
        sendText = (TextView) findViewById(R.id.textView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);

        progressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
        }

    }

    @Override
    public void onBeginningOfSpeech() {
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        progressBar.setIndeterminate(true);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        sendText.setText(errorMessage);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        textToSend = matches.get(0);
        progressBar.setVisibility(View.INVISIBLE);
        sendText.setText(textToSend);

        new SendMessageTask().execute();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onClick(View v) {

        if (connected) {
            listen();
        }
        else {
            new EstablishConnectionTask().execute();
        }
    }

    private void listen(){


            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            speech.startListening(recognizerIntent);


    }

    //Notification Builder Function.
    public void NotificationBuild(){
        mBuilder.setSmallIcon(R.drawable.notification_jarvis)
                .setContentTitle("August")
                .setContentText("Running")
                .setOngoing(true);
        notificationManager.notify(NOTIFICATION_ID_RUNNING, mBuilder.build());
    }

    @Override
    protected void onStart() {

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        new EstablishConnectionTask().execute();

        NotificationBuild();
        if(connected) {
            listen();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {

        notificationManager.cancel(NOTIFICATION_ID_RUNNING);

        new ReleaseConnectionTask().execute();
        super.onStop();
    }


    //Create MainActivity Menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.august_menu,menu);
        return true;
    }

    //Checks item clicked in the MainActivity Menu And sends intent to the specified Activity.
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int idOfSelectedItem = item.getItemId();

        switch(idOfSelectedItem){

            case R.id.august_settings_menu:

                Intent startSettingsActivity = new Intent(MainActivity.this, AugustSettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;

            case R.id.august_task_scheduler_menu:

                Intent startTaskSchedulerActivity = new Intent(MainActivity.this, AugustTaskSchedulerActivity.class);
                startActivity(startTaskSchedulerActivity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Responsible for sending messages to the destination.
    private class SendMessageTask extends AsyncTask<Void, Boolean, Void> {

        String stringFromServer;

        @Override
        protected void onPreExecute() {
            Toast.makeText(MainActivity.this,"Sending...",Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... urls) {

            sendMessage();

            receiveMessage();

            return null;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {

            if(values[0]){Toast.makeText(MainActivity.this,"Message sent",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this,"Message not sent",Toast.LENGTH_SHORT).show();
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(listen){
                if(stringFromServer.equals("yes")) {
                    listen();
                }
            }

        }

        //Send message to the server.
        public void sendMessage(){

            try
            {

                OutputStream os = socket.getOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(os);
                out.writeObject(textToSend);
                os.flush();
                publishProgress(true);
            }
            catch(Exception e)
            {
                publishProgress(false);
            }
        }

        //Receive string from server to start listening.
        public void receiveMessage(){

            try{

                InputStream str = socket.getInputStream();

                ObjectInputStream oin = new ObjectInputStream(str);
                stringFromServer = (String) oin.readObject();

                listen = true;
            }
            catch(Exception e){

            }
        }

    }

    //Responsible for establishing connection with the server.
    private class EstablishConnectionTask extends AsyncTask<Void, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {

            Toast.makeText(MainActivity.this,"Connecting to Server....",Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try{
                socket = new Socket("192.168.43.49",1223);
                publishProgress(true);

                return true;
            }
            catch(Exception e){

                publishProgress(false);
                return false;
            }

        }

        @Override
        protected void onProgressUpdate(Boolean... values) {

            if(values[0]){
                Toast.makeText(MainActivity.this,"Connected to Server",Toast.LENGTH_SHORT).show();
                listen();
            }
            else{
                Toast.makeText(MainActivity.this,"Server is down",Toast.LENGTH_SHORT).show();
                connected = false;

            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            connected = aBoolean;

            super.onPostExecute(aBoolean);
        }
    }

    //Responsible for ending connection with the server.
    private class ReleaseConnectionTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            try{

                socket.close();
                connected = false;

            }
            catch(Exception e){
                connected = false;

            }

            return null;
        }

    }
}

