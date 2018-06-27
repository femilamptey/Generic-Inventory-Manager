/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chemicalinventorymanager.Screens;

import chemicalinventorymanager.DatabaseManager;
import chemicalinventorymanager.Supplier;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author BENTSI-ENCHILL_e
 */
public class SupplierSummaryController implements Initializable {

    Supplier supplier;
    
    @FXML
    Label supplierNameLabel;
    
    @FXML
    Label supplierEmailLabel;
    
    @FXML
    Label supplierPhoneLabel;
    
    @FXML
    Label itemsSuppliedLabel;
    
    public SupplierSummaryController(String id) throws SQLException{
        supplier = DatabaseManager.getSupplierWithId(id);
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        supplierNameLabel.setText(supplier.name);
        supplierEmailLabel.setText(supplier.email);
        supplierPhoneLabel.setText(supplier.phone);
        
    }    
    
}
