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
    }

    //INTERNAL LOGIC
    private int heildKm = 0;
    private double heildGjald = 0.00;
    private int fjoldiManada = 0;

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
            double manGjald = manKm * erLoglegt(flokkur);

            heildKm += manKm;
            heildGjald += manGjald;
            fjoldiManada++;

            // Uppfærir output textfields
            manKmOutput.setText(String.valueOf(manKm));
            fjoldiManadaOutput.setText(String.valueOf(fjoldiManada));
            heildKmOutput.setText(String.valueOf(heildKm));

            
            // Reiknar gjald per kilometra
            if (heildKm > 0) {
                gjaldPerKmOutput.setText(String.format("%.2f", (double) heildGjald / heildKm));
            } else {
                gjaldPerKmOutput.setText("0.00");
            }

            // Hreinsun inntaka
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
        gjaldPerKmOutput.setText("0.00");
        
        heildKm = 0;
        heildGjald = 0.00;
        fjoldiManada = 0;
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

    public void skraManud(int upphaf, int lokKm, String flokkur) {
        // er i onSkra.
    }

    public double getGjaldKilometra(String flokkur) {
        return erLoglegt(flokkur);
    }

    /** ... 
     * Helper method sem athugar hvort flokk og skilar gjaldi per kílómetra.
     * Data er byggð á íslenskum k gjöldum 
     * @param flokkur bílaflokkur sem notandi slær inn.
     * @return gjald per kílómetra ef flokk er löglegur, annars 0 = error code.
     * 
    */
    public double erLoglegt(String flokkur) {
        if (flokkur.equals("A")) return 4.15;
        if (flokkur.equals("B")) return 6.95;
        if (flokkur.equals("C")) return 10.00;
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