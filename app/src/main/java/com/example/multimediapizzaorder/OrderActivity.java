package com.example.multimediapizzaorder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

public class OrderActivity extends AppCompatActivity {

    private String TAG = getClass().toString();
    private final int RC_GREETING = 1;
    private final int RC_PIZZA_SIZE = 2;
    private final int RC_PIZZA_DOUGH = 3;
    private final int RC_PIZZA_TOPPING = 4;
    private final int RC_CHECK_ORDER = 5;

    private String[] sizes = {"klein", "mittel", "groß"};
    private String[] doughs = {"vollkorn", "normal"};
    private String[] toppings = {"salami", "thunfisch", "pilze", "käse", "shrimp", "nichts"};

    private TextToSpeech textToSpeech;

    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.GERMAN);
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {
                        // don't do anything
                    }

                    @Override
                    public void onDone(String s) {
                        if (s.equals(String.valueOf(R.string.greeting))) {
                            listen(RC_GREETING);
                        } else if (s.equals(String.valueOf(R.string.pizza_size))) {
                            listen(RC_PIZZA_SIZE);
                        } else if (s.equals(String.valueOf(R.string.dough_type))) {
                            listen(RC_PIZZA_DOUGH);
                        } else if (s.equals(String.valueOf(R.string.toppings))) {
                            listen(RC_PIZZA_TOPPING);
                        } else if (s.equals(String.valueOf(R.string.order_summary))) {
                            listen(RC_CHECK_ORDER);
                        }
                    }

                    @Override
                    public void onError(String s) {
                        Log.e(TAG, "onError: " + s);
                    }
                });
                speak(R.string.greeting);
            } else {
                Log.e(TAG, "onCreate: failed to initialize TextToSpeech.");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case RC_GREETING:
                    String name = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                    Log.i(TAG, "onActivityResult: name: " + name);
                    updateHistory("User", name);
                    order = new Order(name);
                    speak(R.string.pizza_size, Arrays.stream(this.sizes).reduce((String a, String b) -> a + ", " + b).get());
                    break;
                case RC_PIZZA_SIZE:
                    String size = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                    Log.i(TAG, "onActivityResult: size: " + size);
                    updateHistory(order.name, size);
                    if (checkAnswer(size, sizes)) {
                        order.size = size;
                        speak(R.string.dough_type, Arrays.stream(this.doughs).reduce((String a, String b) -> a + ", " + b).get());
                    } else {
                        speak(getResources().getString(R.string.alternative), R.string.pizza_size, Arrays.stream(this.sizes).reduce((String a, String b) -> a + ", " + b).get());
                    }
                    break;
                case RC_PIZZA_DOUGH:
                    String doughType = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                    Log.i(TAG, "onActivityResult: doughType: " + doughType);
                    updateHistory(order.name, doughType);
                    if (checkAnswer(doughType, doughs)) {
                        order.dough = doughType;
                        speak(R.string.toppings);
                    } else {
                        speak(getResources().getString(R.string.alternative), R.string.dough_type, Arrays.stream(this.doughs).reduce((String a, String b) -> a + ", " + b).get());
                    }
                    order.dough = doughType;
                    break;
                case RC_PIZZA_TOPPING:
                    String topping = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                    Log.i(TAG, "onActivityResult: topping: " + topping);
                    updateHistory(order.name, topping);
                    if (checkAnswer(topping, toppings)) {
                        order.topping = topping;
                        // read order
                        speak(R.string.order_summary, order.toString() + getResources().getString(R.string.order_correct));
                    } else {
                        speak(getResources().getString(R.string.alternative), R.string.toppings);
                    }
                    break;
                case RC_CHECK_ORDER:
                    String answer = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                    Log.i(TAG, "onActivityResult: answer: " + answer);
                    if (answer.toLowerCase().equals("ja")) {
                        // start new activity for overview
                        Intent intent = new Intent(this, OverviewActivity.class);
                        intent.putExtra(OverviewActivity.ORDER, order);
                        startActivity(intent);
                    } else {
                        // return to start
                        speak(R.string.greeting);
                        ((TextView) findViewById(R.id.conversation)).setText("");
                    }
                    break;
            }
        } else {
            Log.e(TAG, "onActivityResult: bad result from activity.");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void speak(int id) {
        updateHistory("System", getResources().getString(id));
        textToSpeech.speak(getResources().getText(id), TextToSpeech.QUEUE_FLUSH, null, String.valueOf(id));
    }

    private void speak(String preceding, int id) {
        updateHistory("System", getResources().getString(id));
        textToSpeech.speak(preceding + getResources().getText(id), TextToSpeech.QUEUE_FLUSH, null, String.valueOf(id));
    }

    private void speak(int id, String addition) {
        if (addition == null) {
            addition = getResources().getString(R.string.error);
        }
        textToSpeech.speak(order.name + ", " + getResources().getText(id) + addition, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(id));
    }

    private void speak(String preceding, int id, String addition) {
        if (addition == null) {
            addition = getResources().getString(R.string.error);
        }
        textToSpeech.speak(preceding + order.name + ", " + getResources().getText(id) + addition, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(id));
    }

    private void listen(int code) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Bitte sprechen sie jetzt.");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        startActivityForResult(intent, code);
    }

    private void updateHistory(String user, String text) {
        ((TextView) findViewById(R.id.conversation)).append(user + ": " + text + "\n");
    }

    private boolean checkAnswer(String answer, String[] possibilities) {
        answer = answer.toLowerCase();
        for (String s : possibilities) {
            if (s.equals(answer) || s.contains(answer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        textToSpeech.shutdown();
        super.onDestroy();
    }
}