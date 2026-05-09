package is.vidmot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller klasi fyrir KmGjaldView.fxml
 *
 * Notaður til að skrá kílómetraakstur og reikna út gjald per kílómetra.
 * Heldur utan um sögu skráninga (ObservableList) og sýnir í ListView.
 *
 * Flokkar:
 *   A = Bifhjól / vespur     -> 4.15 kr/km (fast gjald)
 *   B = Almennir bílar (≤3500 kg) -> 6.95 kr/km (fast gjald)
 *   C = Þungir bílar (>3500 kg)   -> gjald fer eftir þyngd (sjá taxta)
 *
 * author: Gabríel del Rosario
 * email: gdr5@hi.is
 * version: Kílometrargjald 2.0 - History
 */
public class kmGjaldController {

    // ---------- FXML inputs ----------
    @FXML private TextField flokkurInput;
    @FXML private TextField thyngdInput;
    @FXML private TextField upphafsInput;
    @FXML private TextField lokKmInput;

    // ---------- FXML outputs ----------
    @FXML private TextField manKmOutput;
    @FXML private TextField fjoldiManadaOutput;
    @FXML private TextField heildKmOutput;
    @FXML private TextField gjaldPerKmOutput;

    // ---------- FXML buttons ----------
    @FXML private Button SkraBtn;
    @FXML private Button Hreinsa;
    @FXML private Button EydaBtn;
    @FXML private Button HreinsaSoguBtn;

    // ---------- FXML history panel ----------
    @FXML private ListView<kmGjaldManudur> soguListi;

    // ---------- Persistence ----------
    private static final Path SAVE_FILE = Path.of(
            System.getProperty("user.home"), ".kmgjald", "saga.csv");

    // ---------- Internal state ----------
    private int heildKm = 0;
    private double heildGjald = 0.00;
    private int fjoldiManada = 0;

    /**
     * ObservableList er sérstök gerð af lista sem JavaFX getur "hlustað á".
     * Þegar við bætum við eða fjarlægjum úr þessum lista uppfærist ListView sjálfvirkt.
     * Þetta heitir "Observer pattern" og er mjög algengt í GUI forritun.
     */
    private final ObservableList<kmGjaldManudur> saga = FXCollections.observableArrayList();

    /**
     * Initalizer method sem keyrist við hleðslu á view.
     * Stillir output sem read-only, læsir thyngdInput nema flokkur sé "C",
     * og tengir history list við ListView.
     */
    @FXML
    public void initialize() {
        manKmOutput.setEditable(false);
        fjoldiManadaOutput.setEditable(false);
        heildKmOutput.setEditable(false);
        gjaldPerKmOutput.setEditable(false);

        // Sjálfgefið: þyngdarreiturinn er læstur (aðeins virkur fyrir flokk C)
        thyngdInput.setDisable(true);
        thyngdInput.setPromptText("Aðeins fyrir flokk C");

        // Hlustar á breytingar á flokkurInput - opnar/lokar thyngdInput sjálfvirkt
        flokkurInput.textProperty().addListener((observable, oldValue, newValue) -> {
            String flokkur = newValue.trim().toUpperCase();
            if (flokkur.equals("C")) {
                thyngdInput.setDisable(false);
                thyngdInput.setPromptText("t.d 4500");
            } else {
                thyngdInput.setDisable(true);
                thyngdInput.clear();
                thyngdInput.setPromptText("Aðeins fyrir flokk C");
            }
        });

        // Tengja söguna við ListView - allt sem við bætum við "saga" birtist sjálfkrafa
        soguListi.setItems(saga);

        // Hlaða vistaðar skráningar frá fyrri keyrslu
        loadSaga();
    }

    // ==========================================================================
    // SKRA - skráir nýjan mánuð
    // ==========================================================================
    @FXML
    private void OnSkra() {
        try {
            String flokkur = flokkurInput.getText().trim().toUpperCase();
            int upphaf = Integer.parseInt(upphafsInput.getText().trim());
            int lokKm = Integer.parseInt(lokKmInput.getText().trim());

            // 1) Athuga flokk
            if (!erLoglegurFlokkur(flokkur)) {
                error("Ólöglegur flokkur. Notaðu A, B eða C.");
                return;
            }

            // 2) Athuga km
            if (upphaf < 0 || lokKm < 0) {
                error("Kílómetrastaða getur ekki verið neikvæð");
                return;
            }
            if (lokKm < upphaf) {
                error("Lokakílómetrar geta ekki verið minni en upphafskílómetrar");
                return;
            }
            if (lokKm == upphaf) {
                error("Enginn akstur skráður - lokakílómetrar eru þeir sömu og upphafs");
                return;
            }

            // 3) Reikna gjald per km - fyrir C flokk þurfum við þyngd
            double gjaldPerKm;
            int thyngd = 0;
            if (flokkur.equals("C")) {
                String thyngdText = thyngdInput.getText().trim();
                if (thyngdText.isEmpty()) {
                    error("Sláðu inn þyngd (kg) fyrir flokk C bíla");
                    return;
                }
                thyngd = Integer.parseInt(thyngdText);
                if (thyngd <= 3500) {
                    error("Flokkur C er fyrir bíla yfir 3500 kg. Notaðu B fyrir léttari bíla.");
                    return;
                }
                if (thyngd > 10000) {
                    error("Þyngd er utan taxtans (hámark 10.000 kg).");
                    return;
                }
                gjaldPerKm = gjaldFyrirThyngd(thyngd);
            } else {
                gjaldPerKm = gjaldFyrirFlokk(flokkur);
            }

            // 4) Reikna mánaðarakstur og gjald
            int manKm = lokKm - upphaf;
            double manGjald = manKm * gjaldPerKm;

            heildKm += manKm;
            heildGjald += manGjald;
            fjoldiManada++;

            // 5) Bæta í söguna - ListView uppfærist sjálfkrafa því ObservableList
            kmGjaldManudur ny = new kmGjaldManudur(LocalDate.now(), flokkur, thyngd, upphaf, lokKm, gjaldPerKm);
            saga.add(0, ny);  // Bæta fremst (nýjasta efst í listanum)

            // 6) Uppfæra output reiti
            manKmOutput.setText(String.format("%,d km / %,.2f kr", manKm, manGjald));
            fjoldiManadaOutput.setText(String.valueOf(fjoldiManada));
            heildKmOutput.setText(String.format("%,d km / %,.2f kr", heildKm, heildGjald));

            if (heildKm > 0) {
                gjaldPerKmOutput.setText(String.format("%.2f", heildGjald / heildKm));
            } else {
                gjaldPerKmOutput.setText("0.00");
            }

            // 7) Hreinsun inntaka (en ekki sögu!)
            flokkurInput.clear();
            thyngdInput.clear();
            upphafsInput.setText("0");
            lokKmInput.setText("0");

            // 8) Vista saga á disk
            saveSaga();

        } catch (NumberFormatException e) {
            error("Ógilt inntak - sláðu inn tölur í km og þyngd reitina");
        }
    }

    // ==========================================================================
    // HREINSA - hreinsar inntök og talnafjölda en EKKI söguna
    // ==========================================================================
    @FXML
    private void OnHreinsa() {
        flokkurInput.clear();
        thyngdInput.clear();
        upphafsInput.clear();
        lokKmInput.clear();
        manKmOutput.clear();
        fjoldiManadaOutput.clear();
        heildKmOutput.clear();
        gjaldPerKmOutput.clear();

        upphafsInput.setText("0");
        lokKmInput.setText("0");
        manKmOutput.setText("0");
        fjoldiManadaOutput.setText("0");
        heildKmOutput.setText("0");
        gjaldPerKmOutput.setText("0.00");

        heildKm = 0;
        heildGjald = 0.00;
        fjoldiManada = 0;

        thyngdInput.setDisable(true);
        thyngdInput.setPromptText("Aðeins fyrir flokk C");

        // Athugið: söguna sjálfri er ekki eytt hér
    }

    // ==========================================================================
    // EYDA SKRÁNING - fjarlægir valda skráningu úr sögu
    // ==========================================================================
    @FXML
    private void OnEyda() {
        kmGjaldManudur valid = soguListi.getSelectionModel().getSelectedItem();
        if (valid == null) {
            error("Veldu skráningu í listanum til að eyða");
            return;
        }

        // Fjarlægja úr lista
        saga.remove(valid);

        // Uppfæra heildartölurnar (draga frá það sem var í þessari skráningu)
        heildKm -= valid.getEknirKm();
        heildGjald -= valid.getGjald();
        fjoldiManada--;

        // Uppfæra reiti
        endurReiknaSamtala();
        saveSaga();
    }

    // ==========================================================================
    // HREINSA SOGU - eyðir öllum skráningum
    // ==========================================================================
    @FXML
    private void OnHreinsaSogu() {
        if (saga.isEmpty()) {
            return;  // ekkert að hreinsa
        }

        // Staðfestingargluggi - viljum ekki eyða óvart!
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Staðfesta");
        confirm.setHeaderText(null);
        confirm.setContentText("Viltu örugglega eyða allri sögu skráninga? Það er ekki hægt að snúa við.");
        confirm.showAndWait().ifPresent(svar -> {
            if (svar == javafx.scene.control.ButtonType.OK) {
                saga.clear();
                heildKm = 0;
                heildGjald = 0.00;
                fjoldiManada = 0;
                endurReiknaSamtala();
                saveSaga();
            }
        });
    }

    /**
     * Uppfærir heildar-output reitina út frá núverandi gildum.
     * Notað eftir að skráningu er eytt eða sögu hreinsuð.
     */
    private void endurReiknaSamtala() {
        fjoldiManadaOutput.setText(String.valueOf(fjoldiManada));
        heildKmOutput.setText(String.format("%,d km / %,.2f kr", heildKm, heildGjald));
        if (heildKm > 0) {
            gjaldPerKmOutput.setText(String.format("%.2f", heildGjald / heildKm));
        } else {
            gjaldPerKmOutput.setText("0.00");
        }
        // Núllstilla "þessi mánuður" þar sem það á ekki lengur við
        manKmOutput.setText("0");
    }

    // ==========================================================================
    // GETTERS
    // ==========================================================================
    public int getHeildEknirKm()       { return heildKm; }
    public double getHeildarGreidsla() { return heildGjald; }
    public int getFjoldiManada()       { return fjoldiManada; }
    public ObservableList<kmGjaldManudur> getSaga() { return saga; }

    // ==========================================================================
    // GJALDA FALL
    // ==========================================================================

    /**
     * Skilar gjaldi per km fyrir flokk A eða B (föst gjöld).
     * Fyrir flokk C, notaðu gjaldFyrirThyngd() í staðinn.
     */
    public double gjaldFyrirFlokk(String flokkur) {
        if (flokkur.equals("A")) return 4.15;
        if (flokkur.equals("B")) return 6.95;
        if (flokkur.equals("C")) return 9.85;
        return 0;
    }

    public boolean erLoglegurFlokkur(String flokkur) {
        return flokkur.equals("A") || flokkur.equals("B") || flokkur.equals("C");
    }

    /**
     * Skilar km-gjaldi fyrir flokk C bíla eftir þyngd (kg).
     * Tafla byggð á íslenskum kílómetragjöldum (sjá island.is/kilometragjald):
     *   3501 –  5000 kg : 9.85  kr/km
     *   5001 –  6000 kg : 10.44 kr/km
     *   6001 –  7000 kg : 11.06 kr/km
     *   7001 –  8000 kg : 11.73 kr/km
     *   8001 –  9000 kg : 12.43 kr/km
     *   9001 – 10000 kg : 13.18 kr/km
     */
    public double gjaldFyrirThyngd(int thyngd) {
        if (thyngd <= 3500)  return 0;
        if (thyngd <= 5000)  return 9.85;
        if (thyngd <= 6000)  return 10.44;
        if (thyngd <= 7000)  return 11.06;
        if (thyngd <= 8000)  return 11.73;
        if (thyngd <= 9000)  return 12.43;
        if (thyngd <= 10000) return 13.18;
        return 0;
    }

    public double getGjaldKilometra(String flokkur) {
        return gjaldFyrirFlokk(flokkur);
    }

    // ==========================================================================
    // PERSISTENCE - vista og hlaða saga
    // ==========================================================================

    /**
     * Vistar alla skráningar í CSV skrá: ~/.kmgjald/saga.csv
     * Hvert lína: dagsetning,flokkur,thyngd,upphaf,lokKm,gjaldPerKm
     */
    private void saveSaga() {
        try {
            Files.createDirectories(SAVE_FILE.getParent());
            List<String> lines = new ArrayList<>();
            for (kmGjaldManudur m : saga) {
                lines.add(String.join(",",
                        m.getDagsetning().toString(),
                        m.getFlokkur(),
                        String.valueOf(m.getThyngd()),
                        String.valueOf(m.getUpphaf()),
                        String.valueOf(m.getLokKm()),
                        String.valueOf(m.getGjaldPerKm())
                ));
            }
            Files.write(SAVE_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Gögn eru í minni - keyrsla heldur áfram þótt vista takist ekki
        }
    }

    /**
     * Hleður vistaðar skráningar úr CSV skrá við ræsingu.
     * Ef skráin er ekki til (fyrsta keyrsla) eða er skemmd, byrjum við ferskt.
     */
    private void loadSaga() {
        if (!Files.exists(SAVE_FILE)) return;
        try {
            List<String> lines = Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                if (parts.length != 6) continue;
                LocalDate dagsetning = LocalDate.parse(parts[0]);
                String flokkur = parts[1];
                int thyngd = Integer.parseInt(parts[2]);
                int upphaf = Integer.parseInt(parts[3]);
                int lokKm = Integer.parseInt(parts[4]);
                double gjaldPerKm = Double.parseDouble(parts[5]);
                kmGjaldManudur m = new kmGjaldManudur(dagsetning, flokkur, thyngd, upphaf, lokKm, gjaldPerKm);
                saga.add(m);
                heildKm += m.getEknirKm();
                heildGjald += m.getGjald();
                fjoldiManada++;
            }
            if (!saga.isEmpty()) {
                endurReiknaSamtala();
            }
        } catch (IOException | NumberFormatException e) {
            // Skrá er skemmd - byrjum ferskt (gömlu gögnin eru enn á disk)
        }
    }

    /**
     * Basic error controller sem keyrir alert glugga með villumeldingu.
     */
    private void error(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Villa");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
