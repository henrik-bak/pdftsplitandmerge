/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pdfsplitandmerge.controller;

import com.pdfsplitandmerge.model.PdfProperties;
import com.pdfsplitandmerge.model.PdfSplitResult;
import com.pdfsplitandmerge.service.IOService;
import com.pdfsplitandmerge.service.PdfService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Splitter;
import org.thehecklers.monologfx.MonologFX;
import org.thehecklers.monologfx.MonologFXBuilder;
import org.thehecklers.monologfx.MonologFXButton;
import org.thehecklers.monologfx.MonologFXButtonBuilder;

/**
 * FXML Controller class
 *
 * @author Ezzored
 */
public class SplitTabController implements Initializable {

   private static String password = "asd";
    PDDocument document;
    List<PDDocument> documents = null;
    Splitter splitter;
    File openedFile;
    File selectedDir;
    final ObservableList<PdfProperties> splitFiles = FXCollections.observableArrayList();
    Integer splitPageNumber;
    String splitByRangeText;
    @FXML
    private TableView splitTable;
    @FXML
    private Button addFileSplitBtn;
    @FXML
    private Button browseFileSplitBtn;
    @FXML
    private Button removeFileSplitBtn;
    @FXML
    private TableColumn numberColumn;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn pathColumn;
    @FXML
    private TableColumn pagesColumn;
    @FXML
    private RadioButton splitPagesRadioBtn;
    @FXML
    private RadioButton splitByRangeRadioBtn;
    @FXML
    private TextArea splitPagesTextArea;
    @FXML
    private TextArea splitRangeTextArea;
    @FXML
    private TextArea baseNameTextArea;
    @FXML
    private TextArea folderNameTextArea;
    MonologFXButton okBtn;
    MonologFX dialog;

    @FXML
    private void splitPdfBtnClick(ActionEvent event) throws CryptographyException, IOException, COSVisitorException {
        if (validateInputFields()) {
            if (document != null) {
                PdfSplitResult result = PdfService.splitPdf(document, password, splitPageNumber, splitByRangeText);

                if (!result.getSuccess()) {
                    initializeErrorButtons();
                    dialog.setMessage(result.getErrorMessage());
                    dialog.showDialog();
                } else {

                    documents = result.getDocuments();

                    if (documents != null) {

                        String folder = folderNameTextArea.getText().replace("\\", "/");
                        File dir = new File(folder);

                        if (!dir.isDirectory()) {
                            MonologFXButton.Type retval = showDialog();
                            if (retval.equals(MonologFXButton.Type.YES)) {
                                dir.mkdirs();
                            }
                        }

                        for (int i = 0; i < documents.size(); i++) {
                            PDDocument doc = documents.get(i);
                            Integer docNumber = i+1;
                            String fileName = baseNameTextArea.getText() + "-" + docNumber + ".pdf";
                            IOService.writeDocument(doc, fileName, folderNameTextArea.getText());
                            doc.close();
                        }

                        for (int i = 0; documents != null && i < documents.size(); i++) {
                            PDDocument doc = (PDDocument) documents.get(i);
                            doc.close();
                        }
                        documents = null;
                    }
                }
            } else {
                initializeErrorButtons();
                dialog.setMessage("Please select a file to split!");
                dialog.showDialog();
            }
        }
    }

    @FXML
    private void addFileToSplit(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        openedFile = fileChooser.showOpenDialog(null);

        if (openedFile != null) {
            try {
                document = PDDocument.load(openedFile);
            } catch (IOException ex) {
                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
            splitFiles.add(new PdfProperties(1, openedFile.getName(), openedFile.getParent(), document.getNumberOfPages()));
        }

        baseNameTextArea.setText(openedFile.getName().substring(0, openedFile.getName().length() - 4));
        folderNameTextArea.setText(openedFile.getParent());

        addFileSplitBtn.setDisable(true);
        removeFileSplitBtn.setDisable(false);
    }

    @FXML
    private void removeFileToSplit(ActionEvent event) throws IOException {
        document.close();
        splitFiles.clear();
        addFileSplitBtn.setDisable(false);
        removeFileSplitBtn.setDisable(true);
    }

    @FXML
    private void browseFileToSplit(ActionEvent event) {
         DirectoryChooser directoryChooser = new DirectoryChooser();
         directoryChooser.setTitle("Please select a directory...");
         selectedDir = directoryChooser.showDialog(null);
         if (selectedDir!=null)
            folderNameTextArea.setText(selectedDir.getAbsolutePath());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        removeFileSplitBtn.setDisable(true);

        numberColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, Integer>("number"));

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, String>("fileName"));

        pathColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, String>("path"));

        pagesColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, Integer>("pages"));

        splitTable.setItems(splitFiles);
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

    private MonologFXButton.Type showDialog() {
        MonologFXButton mlb = MonologFXButtonBuilder.create()
                .defaultButton(true)
                .type(MonologFXButton.Type.YES)
                .build();

        MonologFXButton mlb2 = MonologFXButtonBuilder.create()
                .cancelButton(true)
                .type(MonologFXButton.Type.NO)
                .build();

        MonologFX mono = MonologFXBuilder.create()
                .modal(true)
                .type(MonologFX.Type.QUESTION)
                .message("Folder does not exists! Do you want to create it?")
                .titleText("Folder does not exists!")
                .button(mlb)
                .button(mlb2)
                .buttonAlignment(MonologFX.ButtonAlignment.RIGHT)
                .build();
        return mono.showDialog();
    }

    private boolean validateInputFields() {

        if (splitFiles.isEmpty()) {
            initializeErrorButtons();
            dialog.setMessage("Please select a PDF file!");
            dialog.showDialog();
            return false;
        }

        if (splitPagesRadioBtn.isSelected()) {
            try {
                splitPageNumber = Integer.parseInt(splitPagesTextArea.getText());
            } catch (NumberFormatException e) {
                initializeErrorButtons();
                dialog.setMessage("Please enter a valid page number!");
                dialog.showDialog();
                return false;
            }
        }

        if (splitPagesTextArea.getText().isEmpty() && splitPagesRadioBtn.isSelected()) {
            initializeErrorButtons();
            dialog.setMessage("Please enter a valid page number!");
            dialog.showDialog();
            return false;
        }

        if (splitRangeTextArea.getText().isEmpty() && splitByRangeRadioBtn.isSelected()) {
            initializeErrorButtons();
            dialog.setMessage("Please enter a valid range!");
            dialog.showDialog();
            return false;
        } else {
            splitByRangeText = splitRangeTextArea.getText();
        }

        if (baseNameTextArea.getText().isEmpty()) {
            initializeErrorButtons();
            dialog.setMessage("Please enter a valid base name for the new files!");
            dialog.showDialog();
            return false;
        }
        if (folderNameTextArea.getText().isEmpty()) {
            initializeErrorButtons();
            dialog.setMessage("Please select/enter a valid folder name for the new files!");
            dialog.showDialog();
            return false;
        }
        return true;
    }
}
