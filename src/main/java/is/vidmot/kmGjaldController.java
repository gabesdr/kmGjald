package is.vidmot;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/** ...
 * Controller klasi fyrir KmGjaldView.fxml
 * Notaður til að skrá kílómetraakstur og reikna út gjald per kílómetra.
 * Hefur innbyggða villumeðhöndlun fyrir ólöglegar inntök.
 * Notar so líka kmGjald.css fyrir stílingu.
 *
 * Flokkar:
 *   A = Mótorhjól / vespur     -> 4.15 kr/km (fast gjald)
 *   B = Almennir bílar (≤3500 kg) -> 6.95 kr/km (fast gjald)
 *   C = Þungir bílar (>3500 kg)   -> gjald fer eftir þyngd (sjá taxta)
 *
 * @author Gabríel del Rosario
 * @email gdr5@hi.is
 * @version 1.1
 *
 * Það er ekki víst að allar upplýsingar sem koma fram í þessum forriti séu nákvæmar eða uppfærðar eftir lögum og reglum kílómetragjalda.
 *
*/
public class kmGjaldController {
    //FXMLTextFieldsInputs
    @FXML
    private TextField flokkurInput;

    @FXML
    private TextField thyngdInput;   // NÝTT: þyngd í kg, notað fyrir flokk C

    @FXML
    private TextField upphafsInput;

    @FXML
    private TextField lokKmInput;

    //FXMLTextfieldsOutputs
    @FXML
    private TextField manKmOutput;

    @FXML
    private TextField fjoldiManadaOutput;

    @FXML
    private TextField heildKmOutput;

    @FXML
    private TextField gjaldPerKmOutput;

    //FXML Buttons
    @FXML
    private Button SkraBtn;

    @FXML
    private Button Hreinsa;

    /** ...
     * Initalizer method sem keyrist við hleðslu á view.
     * Initilazerar output sem non-editable.
     * Læsir thyngdInput nema flokkur sé "C".
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
    }

    //INTERNAL LOGIC
    private int heildKm = 0;
    private double heildGjald = 0.00;
    private int fjoldiManada = 0;

    /** ...
     *
     * Höndlar Skrá og Hreinsunar takkanna.
     * Reiknar út kílómetra og gjald per kílómetra.
     * Uppfærir output textfields.
     * Með villumeðhöndlun fyrir ólögleg inntök.
     * @throws NumberFormatException ef inntak er ógilt eða ekki slegið inn.
     *
    */

    // Button Skra actions
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

            // 2) Athuga km - engin neikvæð gildi og lokKm verður að vera stærri en upphaf
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
            if (flokkur.equals("C")) {
                String thyngdText = thyngdInput.getText().trim();
                if (thyngdText.isEmpty()) {
                    error("Sláðu inn þyngd (kg) fyrir flokk C bíla");
                    return;
                }
                int thyngd = Integer.parseInt(thyngdText);
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

            // Uppfærir output textfields
            // Mánaðarlega: aðeins þessi mánuður
            manKmOutput.setText(String.format("%,d km / %,.2f kr", manKm, manGjald));
            fjoldiManadaOutput.setText(String.valueOf(fjoldiManada));
            // Heildarsamtala: allir mánuðir samanlagt
            heildKmOutput.setText(String.format("%,d km / %,.2f kr", heildKm, heildGjald));

            // Reiknar meðalgjald per km yfir alla mánuði
            if (heildKm > 0) {
                gjaldPerKmOutput.setText(String.format("%.2f", heildGjald / heildKm));
            } else {
                gjaldPerKmOutput.setText("0.00");
            }

            // Hreinsun inntaka
            flokkurInput.clear();
            thyngdInput.clear();
            upphafsInput.setText("0");
            lokKmInput.setText("0");

        } catch (NumberFormatException e) {
            error("Ógilt inntak - sláðu inn tölur í km og þyngd reitina");
        }
    }

    /** ...
     *
     * Takki sem hreinsar öll inntök og úttök og endurstillir innri breytur.
     *
    */
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

        // Endurstilla þyngdarreitinn - læstur þangað til flokkur C er valinn aftur
        thyngdInput.setDisable(true);
        thyngdInput.setPromptText("Aðeins fyrir flokk C");
    }


    /** ...
     * Helper methods til að ná í heildarkílometra, heildargreiðslu og fjölda mánaða.
     * @return gildi.
    */

    // Helper Methods
    public int getHeildEknirKm() {
        return heildKm;
    }

    public double getHeildarGreidsla() {
        return heildGjald;
    }

    public int getFjoldiManada() {
        return fjoldiManada;
    }

    /**
     * Skilar gjaldi per km fyrir flokk A eða B (föst gjöld).
     * Fyrir flokk C, notaðu gjaldFyrirThyngd() í staðinn.
     * @param flokkur "A", "B" eða "C"
     * @return gjald per km, eða 0 ef flokkur er ógildur
     */
    public double gjaldFyrirFlokk(String flokkur) {
        if (flokkur.equals("A")) return 4.15;   // mótorhjól / vespur
        if (flokkur.equals("B")) return 6.95;   // almennir bílar ≤3500 kg
        if (flokkur.equals("C")) return 9.85;   // sjálfgefið C-gjald (lægsta þrep)
        return 0;
    }

    /**
     * Athugar hvort flokkur sé löglegur (A, B, eða C).
     * @param flokkur strengur frá notanda
     * @return true ef löglegur, annars false
     */
    public boolean erLoglegurFlokkur(String flokkur) {
        return flokkur.equals("A") || flokkur.equals("B") || flokkur.equals("C");
    }

    /**
     * Skilar km-gjaldi fyrir flokk C bíla eftir þyngd (kg).
     * Tafla byggð á íslenskum kílómetragjöldum:
     *   3501 –  5000 kg : 9.85  kr/km
     *   5001 –  6000 kg : 10.44 kr/km
     *   6001 –  7000 kg : 11.06 kr/km
     *   7001 –  8000 kg : 11.73 kr/km
     *   8001 –  9000 kg : 12.43 kr/km
     *   9001 – 10000 kg : 13.18 kr/km
     *
     * @param thyngd heildarþyngd bíls í kg
     * @return gjald per km
     */
    public double gjaldFyrirThyngd(int thyngd) {
        if (thyngd <= 3500)  return 0;     // utan C-flokks (þetta er B)
        if (thyngd <= 5000)  return 9.85;
        if (thyngd <= 6000)  return 10.44;
        if (thyngd <= 7000)  return 11.06;
        if (thyngd <= 8000)  return 11.73;
        if (thyngd <= 9000)  return 12.43;
        if (thyngd <= 10000) return 13.18;
        return 0; // utan taxta
    }

    // Backward-compatible: gamla nafnið
    public double getGjaldKilometra(String flokkur) {
        return gjaldFyrirFlokk(flokkur);
    }

    /** ...
     * Basic error controller sem keyrir alert glugga með villumeldingu.
    */
    private void error(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Villa");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
