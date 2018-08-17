package com.example.trips.guessthefootballer;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> celebUrls = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];

    TextView timeControl;
    TextView score;
    TextView scoreUpdatefinal;
    int scoreUpdate = 0;

    RelativeLayout main;
    RelativeLayout main2;

    ImageView right;
    ImageView wrong;

    ImageView footballer;
    Button button1;
    Button button2;
    Button button3;
    Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = (RelativeLayout)findViewById(R.id.main);
        main2 = (RelativeLayout)findViewById(R.id.main2);

        timeControl = (TextView)findViewById(R.id.timeControl);
        score = (TextView)findViewById(R.id.score);
        scoreUpdatefinal = (TextView)findViewById(R.id.scoreUpdatefinal);

        right = (ImageView)findViewById(R.id.imageView);
        wrong = (ImageView)findViewById(R.id.imageView2);

        footballer = (ImageView)findViewById(R.id.circle_footballer);
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        button4 = (Button)findViewById(R.id.button4);

        DownloadTask task = new DownloadTask();
        String result;
        try {
            result = task.execute("http://www.espn.com/espn/feature/story/_/id/21589590/fc-100-best-men-players-managers-world-football#!").get();
            Pattern p = Pattern.compile("png\">\n" +
                    "          <img src=\"(.*?)\">\n" +
                    "          <div");
            Matcher m = p.matcher(result);
            while (m.find()){
                celebUrls.add(m.group(1));
            }

            p = Pattern.compile("<h3>(.*?)</h3>");
            m = p.matcher(result);
            while (m.find()){
                celebNames.add(m.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        startCountDown();
        createNewQuestion();
    }

    public void tryAgain(View view) {
        main.setVisibility(View.VISIBLE);
        main2.setVisibility(View.INVISIBLE);
        createNewQuestion();
        startCountDown();
    }

    @SuppressLint("StaticFieldLeak")
    public class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {

            StringBuilder result = new StringBuilder();
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data!=-1){
                    char current = (char) data;
                    result.append(current);
                    data = reader.read();
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ImageDownloader extends AsyncTask<String,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void createNewQuestion(){

        Random random = new Random();
        chosenCeleb = random.nextInt(celebUrls.size());


        ImageDownloader imageTask = new ImageDownloader();
        Bitmap celebImage;
        try {
            celebImage = imageTask.execute(celebUrls.get(chosenCeleb)).get();
            footballer.setImageBitmap(celebImage);

            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation;
            for (int i=0;i<4;i++){
                if (i==locationOfCorrectAnswer){
                    answers[i] = celebNames.get(chosenCeleb);
                }else {
                    incorrectAnswerLocation = random.nextInt(celebUrls.size());
                    while (incorrectAnswerLocation == chosenCeleb){
                        incorrectAnswerLocation = random.nextInt(celebUrls.size());
                    }
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }

            button1.setText(answers[0]);
            button2.setText(answers[1]);
            button3.setText(answers[2]);
            button4.setText(answers[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public void celebChosen(View view) {
        right.setVisibility(View.INVISIBLE);
        wrong.setVisibility(View.INVISIBLE);

        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            scoreUpdate+=5;
            score.setText(String.valueOf(scoreUpdate));
            right.setVisibility(View.VISIBLE);
        }else {
            scoreUpdate-=2;
            score.setText(String.valueOf(scoreUpdate));
            wrong.setVisibility(View.VISIBLE);
            //Toast.makeText(getApplicationContext(),"Wrong!, he was "+celebNames.get(chosenCeleb),Toast.LENGTH_SHORT).show();
        }
        createNewQuestion();
    }

    public void startCountDown(){
        new CountDownTimer(120100,1000){

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                int min = (int) millisUntilFinished/60000;
                int sec = (int) (millisUntilFinished/1000 - min*60);
                String secString = String.valueOf(sec);
                if(sec<=9){
                    secString = "0"+sec;
                }
                timeControl.setText(min+":"+secString);

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                timeControl.setText("0:00");
                main.setVisibility(View.INVISIBLE);
                main2.setVisibility(View.VISIBLE);
                scoreUpdatefinal.setText("Your Score: "+String.valueOf(scoreUpdate));
            }
        }.start();
    }
}
