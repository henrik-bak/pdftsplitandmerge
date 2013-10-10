/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pdfsplitandmerge.controller;

import com.pdfsplitandmerge.model.PdfMergeResult;
import com.pdfsplitandmerge.model.PdfProperties;
import com.pdfsplitandmerge.service.IOService;
import com.pdfsplitandmerge.service.PdfService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.thehecklers.monologfx.MonologFX;
import org.thehecklers.monologfx.MonologFXBuilder;
import org.thehecklers.monologfx.MonologFXButton;
import org.thehecklers.monologfx.MonologFXButtonBuilder;

/**
 * FXML Controller class
 *
 * @author Ezzored
 */
public class MergeTabController implements Initializable {

    @FXML
    Button addBtn;
    @FXML
    Button removeBtn;
    @FXML
    Button upBtn;
    @FXML
    Button downBtn;
    @FXML
    Button selectBtn;
    @FXML
    Button mergeBtn;
    @FXML
    TextArea fileNameTextArea;
    @FXML
    TextArea directoryNameTextArea;
    @FXML
    CheckBox overwriteCheckBox;
    @FXML
    private TableColumn numberColumn;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn pathColumn;
    @FXML
    private TableColumn pagesColumn;
    @FXML
    private TableView mergeTable;
    PdfProperties selectedPdf;
    File selectedDir;
    List<File> openedFiles;
    final ObservableList<PdfProperties> mergeFiles = FXCollections.observableArrayList();
    List<PDDocument> documentList = new ArrayList<>();
    MonologFXButton okBtn;
    MonologFX dialog;

    @FXML
    private void addBtnClick(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        openedFiles = fileChooser.showOpenMultipleDialog(null);

        if (openedFiles != null) {
            for (int i = 0; i < openedFiles.size(); i++) {
                File file = openedFiles.get(i);
                PDDocument document = PDDocument.load(file);
                documentList.add(document);
                mergeFiles.add(new PdfProperties(i, file.getName(), file.getParent(), document.getNumberOfPages()));
            }
        }
        removeBtn.setDisable(false);
    }

    @FXML
    private void removeBtnClick(ActionEvent event) {
        if (selectedPdf != null) {
            documentList.remove(selectedPdf.getNumber().intValue());
            mergeFiles.remove(selectedPdf);
            selectedPdf = null;
        }

        if (mergeFiles.isEmpty()) {
            removeBtn.setDisable(true);
        }
    }

    @FXML
    private void upBtnClick(ActionEvent event) {
    }

    @FXML
    private void downBtnClick(ActionEvent event) {
    }

    @FXML
    private void selectBtnClick(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Please select a directory...");
        selectedDir = directoryChooser.showDialog(null);
        if (selectedDir != null) {
            directoryNameTextArea.setText(selectedDir.getAbsolutePath());
        }
    }

    @FXML
    private void mergeBtnClick(ActionEvent event) throws IOException, COSVisitorException {
        PdfMergeResult result = PdfService.mergePdf(documentList);

        if (!directoryNameTextArea.getText().isEmpty() && !fileNameTextArea.getText().isEmpty()) {

            if (result.getSuccess()) {
                IOService.writeDocument(result.getDocuments(), fileNameTextArea.getText(), directoryNameTextArea.getText());
            } else {
                initializeErrorButtons();
                dialog.setMessage(result.getErrorMessage());
                dialog.showDialog();
            }
        } else {
            initializeErrorButtons();
            dialog.setMessage("Please select a valid directory/filename!");
            dialog.showDialog();
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mergeTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            // this method will be called whenever user selected row
            @Override
            public void changed(ObservableValue observale, Object oldValue, Object newValue) {
                selectedPdf = (PdfProperties) newValue;
            }
        });

        removeBtn.setDisable(true);

        numberColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, Integer>("number"));

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, String>("fileName"));

        pathColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, String>("path"));

        pagesColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, Integer>("pages"));

        mergeTable.setItems(mergeFiles);
    }

    private void initializeErrorButtons() {
        okBtn = MonologFXButtonBuilder.create()
                .defaultButton(true)
                .type(MonologFXButton.Type.OK)
                .build();
        dialog = MonologFXBuilder.create()
                .modal(true)
                .type(MonologFX.Type.ERROR)
                .message("Not yet implemented!")
                .titleText("Error!")
                .button(okBtn)
                .buttonAlignment(MonologFX.ButtonAlignment.RIGHT)
                .build();
    }
}
