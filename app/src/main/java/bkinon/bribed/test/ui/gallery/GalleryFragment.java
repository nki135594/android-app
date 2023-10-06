package bkinon.bribed.test.ui.gallery;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bkinon.bribed.test.R;
import bkinon.bribed.test.databinding.FragmentGalleryBinding;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private List<String[]> dataList;
    private TextView dataTextView;
    private String currentName;
    private SearchView searchView;
    private int score;
    private int maxAttempts;

    private Button playAgainButton;
    private Handler handler;
    private Runnable timerRunnable;
    private int secondsPassed;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        dataList = new ArrayList<>();

        // Passe den Pfad zur CSV-Datei an
        String csvFilePath = getActivity().getApplicationInfo().dataDir + "/test.csv";

        try (Reader reader = new InputStreamReader(new FileInputStream(csvFilePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            for (CSVRecord csvRecord : csvParser) {
                String[] data = new String[csvRecord.size()];
                for (int i = 0; i < csvRecord.size(); i++) {
                    data[i] = csvRecord.get(i);
                }
                dataList.add(data);
            }

            // Überprüfung, ob das Einlesen erfolgreich war
            if (!dataList.isEmpty()) {
                // Log.d("GalleryFragment", "CSV-Datei erfolgreich eingelesen.");
                // Weitere Verarbeitung der Daten hier
            } else {
                // Log.d("GalleryFragment", "Fehler beim Einlesen der CSV-Datei.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Finde die Views
        dataTextView = root.findViewById(R.id.dataTextView);
        searchView = root.findViewById(R.id.searchView);
        playAgainButton = root.findViewById(R.id.playAgainButton);

        // Initialisiere das Spiel
        initGame();

        // Setze den QueryListener für die SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                checkAnswer(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Setze den ClickListener für den "Play Again"-Button
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        return root;
    }

    private void initGame() {
        // Setze den Score und die maximalen Versuche
        score = 0;
        maxAttempts = 3;

        // Starte das Spiel
        startNewRound();
        startTimer();
    }

    private void startNewRound() {
        // Überprüfe, ob alle Namen bereits abgefragt wurden
        if (dataList.isEmpty()) {
            endGame();
            return;
        }

        // Wähle einen zufälligen Namen aus den Daten
        Random random = new Random();
        int index = random.nextInt(dataList.size());
        String[] data = dataList.get(index);
        currentName = data[1]; // Annahme: Name befindet sich in der zweiten Spalte der CSV-Datei

        // Zeige den Namen als Hinweis an
        dataTextView.setText(currentName);

        // Lösche den Text in der SearchView
        searchView.setQuery("", false);
    }

    private void checkAnswer(String answer) {
        // Überprüfe, ob die Antwort korrekt ist
        if (TextUtils.isEmpty(answer)) {
            Toast.makeText(getActivity(), "Bitte gib einen Namen ein!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (answer.equalsIgnoreCase(currentName)) {
            // Antwort ist korrekt
            Toast.makeText(getActivity(), "Richtig!", Toast.LENGTH_SHORT).show();
            score++;
            updateScoreTextView();
            startNewRound();
        } else {
            // Antwort ist falsch
            Toast.makeText(getActivity(), "Falsch!", Toast.LENGTH_SHORT).show();
            endGame();
        }
    }

    private void endGame() {
        // Zeige den Endstand an
        Toast.makeText(getActivity(), "Spiel beendet! Dein Score: " + score, Toast.LENGTH_LONG).show();
        showPlayAgainButton();
        stopTimer();
    }

    private void resetGame() {
        // Setze den Score und die maximalen Versuche zurück
        score = 0;

        updateScoreTextView();
        hidePlayAgainButton();

        // Starte das Spiel neu
        startNewRound();
        startTimer();
    }

    private void updateScoreTextView() {
        TextView scoreTextView = binding.getRoot().findViewById(R.id.scoreTextView);
        scoreTextView.setText("Score: " + score);
    }

    private void showPlayAgainButton() {
        playAgainButton.setVisibility(View.VISIBLE);
    }

    private void hidePlayAgainButton() {
        playAgainButton.setVisibility(View.GONE);
    }

    private void startTimer() {
        secondsPassed = 0;
        handler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                secondsPassed++;
                updateTimerTextView();
                handler.postDelayed(this, 1000); // Wiederhole den Timer nach 1 Sekunde
            }
        };
        handler.postDelayed(timerRunnable, 1000); // Starte den Timer nach 1 Sekunde
    }

    private void stopTimer() {
        handler.removeCallbacks(timerRunnable); // Stoppe den Timer
    }

    private void updateTimerTextView() {
        TextView timerTextView = binding.getRoot().findViewById(R.id.timerTextView);
        timerTextView.setText("Zeit: " + secondsPassed + " Sekunden");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
        binding = null;
    }
}
