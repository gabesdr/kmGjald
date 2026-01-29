package is.vidmot;

import javafx.fxml.FXML;
import javafx.scene.control.Label; // Deprecated, er ekki í notkun, geymd ef er þörf á þess síðar.
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/** ... 
 * Controller klasi fyrir KmGjaldView.fxml
 * Notaður til að skrá kílómetraakstur og reikna út gjald per kílómetra.
 * Hefur innbyggða villumeðhöndlun fyrir ólöglegar inntök.
 * Notar so líka kmGjald.css fyrir stílingu.
 * 
 * @author Gabríel del Rosario
 * @email gdr5@hi.is
 * @version 1.0
 * 
 * Það er ekki víst að allar upplýsingar sem koma fram í þessum forriti séu nákvæmar eða uppfærðar eftir lögum og reglum kílómetragjalda.
 * 
*/
public class kmGjaldController {
    //FXMLTextFieldsInputs
    @FXML
    private TextField flokkurInput;

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
    */
    @FXML
    public void initialize() {
        manKmOutput.setEditable(false);
        fjoldiManadaOutput.setEditable(false);
        heildKmOutput.setEditable(false);
        gjaldPerKmOutput.setEditable(false);

        heildKmOutput.setFocusTraversable(false);
        fjoldiManadaOutput.setFocusTraversable(false);
        manKmOutput.setFocusTraversable(false);
        gjaldPerKmOutput.setFocusTraversable(false);
    }

    //INTERNAL LOGIC
    private int heildKm = 0;
    private int heildGjald = 0;
    private int fjoldiManada = 0;
    private static final int KM_PER_MONTH = 1;

    /** ... 
     * 
     * Höndlar Skrá og Hreinsunar takkanna.
     * 
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

            if (erLoglegt(flokkur) == 0) {
                error("Ólöglegur flokkur");
                return;
            }

            if (lokKm < upphaf) {
                error("Lokakílómetrar geta ekki verið minni en upphafskílómetrar");
                return;
            }

            int manKm = lokKm - upphaf;
            int manGjald = manKm * erLoglegt(flokkur);

            heildKm += manKm;
            heildGjald += manGjald;
            fjoldiManada = (heildKm / KM_PER_MONTH) + 1; // Reiknir fjölda mánaða per 1km skv Island.is.

            manKmOutput.setText(String.valueOf(manKm));
            fjoldiManadaOutput.setText(String.valueOf(fjoldiManada));
            heildKmOutput.setText(String.valueOf(heildKm));
            
            if (heildKm > 0) {
                gjaldPerKmOutput.setText(String.format("%.2f", (double) heildGjald / heildKm));
            } else {
                gjaldPerKmOutput.setText("0");
            }

            flokkurInput.clear();
            upphafsInput.setText("0");
            lokKmInput.setText("0");
            
        } catch (NumberFormatException e) {
            error("Ógilt inntak");
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
        gjaldPerKmOutput.setText("0");
        
        heildKm = 0;
        heildGjald = 0;
        fjoldiManada = 0;
    }


    /** ... 
     * Helper methods til að ná í heildarkílometra, heildargreiðslu og fjölda mánaða.
     * 
    */
    // Helper Methods
    public int getHeildEknirKm() {
        return heildKm;
    }

    private int getHeildarGreidsl() {
        return heildGjald;
    }

    public int getFjoldiManada() {
        return fjoldiManada;
    }

    public void skraManud(int upphaf, int lokKm, String flokkur) {
        // er i onSkra.
    }

    public double getGjaldKilometra(String flokkur) {
        return erLoglegt(flokkur);
    }

    public int erLoglegt(String flokkur) {
        if (flokkur.equals("A")) return 7; // 0.000kg - 3.500kg = 7.00isk, 6.95 avg
        if (flokkur.equals("B")) return 7; // 0.000kg - 3.500kg = 7.00isk, 6.95 avg
        if (flokkur.equals("C")) return 10; // 3.501kg - 7.500kg = 11.00isk, 10 avg
        return 0;
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