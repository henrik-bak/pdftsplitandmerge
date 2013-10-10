/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pdfsplitandmerge.model;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 * @author Ezzored
 */
public class PdfMergeResult {
    
     private Boolean success;
    private String errorMessage;
    private PDDocument documents;

    public PdfMergeResult(Boolean success) {
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

    public PDDocument getDocuments() {
        return documents;
    }

    public void setDocuments(PDDocument documents) {
        this.documents = documents;
    }
    
    
    
}
