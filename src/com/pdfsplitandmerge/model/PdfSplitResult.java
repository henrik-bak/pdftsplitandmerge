/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pdfsplitandmerge.model;

import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 * @author Ezzored
 */
public class PdfSplitResult {
    
    private Boolean success;
    private String errorMessage;
    private List<PDDocument> documents;

    public PdfSplitResult(Boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<PDDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<PDDocument> documents) {
        this.documents = documents;
    }
    
}
