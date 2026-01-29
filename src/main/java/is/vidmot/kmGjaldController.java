package is.vidmot;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class kmGjaldController {

    //FXMLLabels
    @FXML
    private Label Flokkur;

    @FXML
    private Label Upphaf;

    @FXML
    private Label manKm;

    @FXML
    private Label heildKm;

    @FXML
    private Label fjoldiManada;

    @FXML
    private Label gjaldKilometra;

    @FXML
    private Label lokKm;

    //FXMLTextFields
    @FXML
    private TextField flokkurInput;

    @FXML
    private TextField upphafsKmInput;

    @FXML
    private TextField lokKmInput;

    @FXML
    private TextField manKmOutput;

    @FXML 
    private TextField fjoldiManadaOutput;

    @FXML
    private TextField heildKmOutput;

    @FXML
    private TextField gjaldPerKm;

    //FXML Buttons
    @FXML
    private Button SkraBtn;

    @FXML
    private Button Hreinsa;

    @FXML
    private void OnSkra() {
        // TODO IMPLEMENT FUNCTIONALITY RECORDING KM STATUS
    }

    @FXML 
    private void OnHreinsa() {
        flokkurInput.clear();
        upphafsKmInput.clear();
        lokKmInput.clear();
        manKmOutput.clear();
        heildKmOutput.clear();
        fjoldiManadaOutput.clear();
        gjaldPerKm.clear();
    }
}