package com.miraahmad.rpsgame;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity implements
        RecognitionListener {

    private static final String KEYPHRASE = "play";
    private SpeechRecognizer recognizer;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    Button b_rock, b_paper, b_scissors;
    ImageView iv_cpu, iv_me;
    TextView tv_myWin, tv_cpuWin, tv_winlose;

    String myChoise, cpuChoise, result;


    Random r;

    int myWin, cpuWin, cpu, tries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_cpu = (ImageView) findViewById(R.id.iv_cpu);
        iv_me = (ImageView) findViewById(R.id.iv_me);

        b_rock = (Button) findViewById(R.id.b_rock);
        b_paper = (Button) findViewById(R.id.b_paper);
        b_scissors = (Button) findViewById(R.id.b_scissors);

        tv_myWin = (TextView) findViewById(R.id.tv_myWin);
        tv_cpuWin = (TextView) findViewById(R.id.tv_cpuWin);
        tv_winlose = (TextView) findViewById(R.id.tv_winlose);

        r = new Random();
        tries = 0;

        b_rock.setEnabled(false);
        b_paper.setEnabled(false);
        b_scissors.setEnabled(false);



        /*b_rock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myChoise = "rock";
                iv_me.setImageResource(R.drawable.rock);
                calculate();
            }
        });

        b_paper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myChoise = "paper";
                iv_me.setImageResource(R.drawable.paper);
                calculate();

            }
        });

        b_scissors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myChoise = "scissors";
                iv_me.setImageResource(R.drawable.scissors);
                calculate();

            }
        });*/


        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        runRecognizerSetup();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                finish();
            }
        }
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Toast.makeText(MainActivity.this, "Failed to init recognizer " + result, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Let's play!", Toast.LENGTH_SHORT).show();
                    reset();
                }
            }
        }.execute();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setKeywordThreshold(1e-10f) // Threshold to tune for keyphrase to balance between false alarms and misses
                .setBoolean("-allphone_ci", true)  // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setFloat("-vad_threshold", 3.0)

                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */


        // Create grammar-based search for digit recognition
        File digitsGrammar = new File(assetsDir, "digits.gram");
        recognizer.addGrammarSearch(KEYPHRASE, digitsGrammar);


    }

    public void calculate(String choice, int nCpu) {

        cpu = nCpu;
        myChoise = choice;


        if (tries < 9) {

            if (cpu == 0) {
                cpuChoise = "rock";
                iv_cpu.setImageResource(R.drawable.rock);
            } else if (cpu == 1) {
                cpuChoise = "paper";
                iv_cpu.setImageResource(R.drawable.paper);
            } else if (cpu == 3) {
                cpuChoise = "scissors";
                iv_cpu.setImageResource(R.drawable.scissors);
            }

            if (myChoise.equals("rock") && cpuChoise.equals("paper")) {
                result = "you lose";
                cpuWin++;
                tries++;
            } else if (myChoise.equals("rock") && cpuChoise.equals("scissors")) {
                result = "you win";
                myWin++;
                tries++;
            } else if (myChoise.equals("paper") && cpuChoise.equals("rock")) {
                result = "you win";
                myWin++;
                tries++;
            } else if (myChoise.equals("paper") && cpuChoise.equals("scissors")) {
                result = "you lose";
                cpuWin++;
                tries++;
            } else if (myChoise.equals("scissors") && cpuChoise.equals("paper")) {
                result = "you win";
                myWin++;
                tries++;
            } else if (myChoise.equals("scissors") && cpuChoise.equals("rock")) {
                result = "you lose";
                cpuWin++;
                tries++;
            } else if (myChoise.equals("scissors") && cpuChoise.equals("scissors")) {
                result = "draw";
                tries++;
            } else if (myChoise.equals("rock") && cpuChoise.equals("rock")) {
                result = "draw";
                tries++;
            } else if (myChoise.equals("paper") && cpuChoise.equals("paper")) {
                result = "draw";
                tries++;
            }

            tv_myWin.setText("Your Score: " + myWin);
            tv_cpuWin.setText("App Score: " + cpuWin);

            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();

        } else {

            recognizer.stop();
            if (cpuWin < myWin) {
                tv_winlose.setText("You are the winner!");
            } else if (cpuWin > myWin) {
                tv_winlose.setText("You are loser!");
            } else if (cpuWin == myWin) {
                tv_winlose.setText("Draw");
            }
        }


    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        reset();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

    }

    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            int rCpu;

            if (text.contains("rock")) {
                rCpu = r.nextInt(3);
                iv_me.setImageResource(R.drawable.rock);
                calculate(text, rCpu);
            } else if (text.contains("paper")) {
                rCpu = r.nextInt(3);
                iv_me.setImageResource(R.drawable.paper);
                calculate(text, rCpu);
            } else if (text.contains("scissors")) {
                rCpu = r.nextInt(3);
                iv_me.setImageResource(R.drawable.scissors);
                calculate(text, rCpu);
            } else {
                tv_winlose.setText("Choose Rock, Paper, Scissors");
            }
        }


    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeout() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    private void reset() {
        recognizer.stop();
        recognizer.startListening(KEYPHRASE);
    }
}
