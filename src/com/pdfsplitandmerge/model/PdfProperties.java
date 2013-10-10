/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pdfsplitandmerge.model;

/**
 *
 * @author Ezzored
 */
public class PdfProperties {
    
    private Integer number;
    private String fileName;
    private String path;
    private Integer pages;

    public PdfProperties() {
    }

    public PdfProperties(Integer number, String fileName, String path, Integer pages) {
        this.number = number;
        this.fileName = fileName;
        this.path = path;
        this.pages = pages;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return "PdfProperties{" + "number=" + number + ", fileName=" + fileName + ", path=" + path + ", pages=" + pages + '}';
    }
}
