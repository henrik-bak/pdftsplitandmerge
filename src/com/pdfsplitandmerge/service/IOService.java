/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pdfsplitandmerge.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.thehecklers.monologfx.MonologFXButton;

/**
 *
 * @author Ezzored
 */
public class IOService {

    public static void writeDocument(PDDocument doc, String fileName, String folder) throws IOException, COSVisitorException {
        FileOutputStream output = null;
        COSWriter writer = null;

        String str = folder.replace("\\", "/");

        try {
            output = new FileOutputStream(str + "/" + fileName);
            writer = new COSWriter(output);
            writer.write(doc);

        } finally {
            if (output != null) {
                output.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }
}
