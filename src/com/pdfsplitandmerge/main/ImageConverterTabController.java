/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pdfsplitandmerge.main;

import com.pdfsplitandmerge.controller.MainWindowController;
import com.pdfsplitandmerge.model.PdfProperties;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.thehecklers.monologfx.MonologFX;
import org.thehecklers.monologfx.MonologFXBuilder;
import org.thehecklers.monologfx.MonologFXButton;
import org.thehecklers.monologfx.MonologFXButtonBuilder;

/**
 * FXML Controller class
 *
 * @author Ezzored
 */
public class ImageConverterTabController implements Initializable {

    final ObservableList<PdfProperties> convertFiles = FXCollections.observableArrayList();
    final ObservableList<String> fileTypes = FXCollections.observableArrayList();
    @FXML
    private TableView convertTable;
    @FXML
    private Button addFileBtn;
    @FXML
    private Button removeFileBtn;
    @FXML
    private TableColumn numberColumn;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn pathColumn;
    @FXML
    private TableColumn pagesColumn;
    @FXML
    private TextArea rangeTextArea;
    @FXML
    private TextArea baseNameTextArea;
    @FXML
    private TextArea folderNameTextArea;
    @FXML
    private TextArea resolutionTextArea;
    @FXML
    private ChoiceBox fileTypeChoice;
    @FXML
    private Button browseFiletBtn;
    @FXML
    private Button exportBtn;
    @FXML
    private RadioButton allPagesRadioBtn;
    @FXML
    private RadioButton rangeRadioBtn;
    MonologFXButton okBtn;
    MonologFX dialog;
    File openedFile;
    PDDocument document;
    File selectedDir;
    String selectedFileType;

    @FXML
    private void exportBtnClick(ActionEvent event) throws IOException {
        if (validateInputFields()) {
            if (document != null) {

                String folder = folderNameTextArea.getText().replace("\\", "/");
                File dir = new File(folder);

                if (!dir.isDirectory()) {
                    MonologFXButton.Type retval = showDialog();
                    if (retval.equals(MonologFXButton.Type.YES)) {
                        dir.mkdirs();
                    }
                }

                List<PDPage> pages = document.getDocumentCatalog().getAllPages();

                for (int i = 0; i < pages.size(); i++) {
                    BufferedImage image = pages.get(i).convertToImage(BufferedImage.TYPE_INT_RGB, Integer.parseInt(resolutionTextArea.getText()));
                    String fileName = folder + "/" + baseNameTextArea.getText() + "-" + i + "." + selectedFileType;
                    File outputFile = new File(fileName);
                    ImageIO.write(image, selectedFileType, outputFile);
                }
            } else {
                initializeErrorButtons();
                dialog.setMessage("Please select a file to split!");
                dialog.showDialog();
            }
        }

    }

    @FXML
    private void addFileBtnClick(ActionEvent event) {

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
            convertFiles.add(new PdfProperties(1, openedFile.getName(), openedFile.getParent(), document.getNumberOfPages()));
        }

        baseNameTextArea.setText(openedFile.getName().substring(0, openedFile.getName().length() - 4));
        folderNameTextArea.setText(openedFile.getParent());

        addFileBtn.setDisable(true);
        removeFileBtn.setDisable(false);
    }

    @FXML
    private void removeFileBtnClick(ActionEvent event) throws IOException {
        document.close();
        convertFiles.clear();
        addFileBtn.setDisable(false);
        removeFileBtn.setDisable(true);
    }

    @FXML
    private void browseFileBtnClick(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Please select a directory...");
        selectedDir = directoryChooser.showDialog(null);
        if (selectedDir != null) {
            folderNameTextArea.setText(selectedDir.getAbsolutePath());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fileTypes.addAll("jpg", "png", "gif", "bmp");
        fileTypeChoice.setItems(fileTypes);
        removeFileBtn.setDisable(true);

        numberColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, Integer>("number"));

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, String>("fileName"));

        pathColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, String>("path"));

        pagesColumn.setCellValueFactory(
                new PropertyValueFactory<PdfProperties, Integer>("pages"));

        convertTable.setItems(convertFiles);

        fileTypeChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue ov, Number value, Number new_value) {
                selectedFileType = fileTypes.get(new_value.intValue());
            }
        });
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

        if (convertFiles.isEmpty()) {
            initializeErrorButtons();
            dialog.setMessage("Please select a PDF file!");
            dialog.showDialog();
            return false;
        }

        if (rangeTextArea.getText().isEmpty() && rangeRadioBtn.isSelected()) {
            initializeErrorButtons();
            dialog.setMessage("Please enter a valid page range!");
            dialog.showDialog();
            return false;
        }

        if (resolutionTextArea.getText().isEmpty()) {
            initializeErrorButtons();
            dialog.setMessage("Please enter a valid resulution!");
            dialog.showDialog();
            return false;
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
