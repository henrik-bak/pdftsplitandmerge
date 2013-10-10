/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pdfsplitandmerge.service;

import com.pdfsplitandmerge.model.PdfMergeResult;
import com.pdfsplitandmerge.model.PdfSplitResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldFactory;
import org.apache.pdfbox.util.PDFCloneUtility;
import org.apache.pdfbox.util.Splitter;

/**
 *
 * @author Ezzored
 */
public class PdfService {

    public static PdfSplitResult splitPdf(PDDocument document, String password, Integer pages, String range) throws CryptographyException, IOException {
        List<PDDocument> documents = null;
        PdfSplitResult result;

        Splitter splitter = new Splitter();
        if (document.isEncrypted()) {
            try {
                document.decrypt(password);
            } catch (InvalidPasswordException e) {
                result = new PdfSplitResult(Boolean.FALSE);
                result.setErrorMessage("Invalid password!");
                return result;
            }
        }

        if (pages != null) {
            splitter.setStartPage(1);
            splitter.setSplitAtPage(pages);
            splitter.setEndPage(document.getNumberOfPages());
            documents = splitter.split(document);
        } else if (!range.isEmpty()) {
            List<Integer> slides = new ArrayList<Integer>();

            String[] parts = range.split(",");

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];

                if (part.matches("^(\\d+)-(\\d+)$")) {
                    String[] limits = part.split("-", -1);
                    int from = limits[0].length() == 0 ? 0 : Integer.parseInt(limits[0]);
                    int to = limits[1].length() == 0 ? Integer.MAX_VALUE : Integer.parseInt(limits[1]);
                    if (to < from) {
                        throw new IllegalArgumentException("Invalid pattern: " + part);
                    }
                    for (int j = from; j <= to; j++) {
                        slides.add(j);
                    }
                } else if (part.matches("^\\d+$")) {
                    slides.add(Integer.parseInt(part));
                }
            }
            documents = new ArrayList<>();
            for (Integer page : slides) {
                PDDocument newPage = new PDDocument();
                newPage.addPage((PDPage) document.getDocumentCatalog().getAllPages().get(page));
                documents.add(newPage);
                newPage.close();
            }

            result = new PdfSplitResult(Boolean.TRUE);
            result.setDocuments(documents);
            //result.setErrorMessage(slides.toString());
            return result;

        }

        result = new PdfSplitResult(Boolean.TRUE);
        result.setDocuments(documents);
        return result;
    }

    public static PdfMergeResult mergePdf(List<PDDocument> documentList) throws IOException {
        PdfMergeResult result = null;
        PDDocument mergedDoc = new PDDocument();

        java.util.Vector<PDDocument> tobeclosed = new java.util.Vector<PDDocument>();
        try {
            for (PDDocument document : documentList) {
                tobeclosed.add(document);
                appendDocument(mergedDoc, document);
            }
        } catch (IOException ex) {
            result = new PdfMergeResult(Boolean.FALSE);
            result.setErrorMessage("Error during operation: "+ex.getMessage());
            return result;
        }        
        finally {
            for (PDDocument doc : tobeclosed) {
                doc.close();
            }
        }
        result = new PdfMergeResult(Boolean.TRUE);
        result.setDocuments(mergedDoc);
        return result;
    }
    
    public static void appendDocument(PDDocument destination, PDDocument source) throws IOException
    {
        if( destination.isEncrypted() )
        {
            throw new IOException( "Error: destination PDF is encrypted, can't append encrypted PDF documents." );
        }
        if( source.isEncrypted() )
        {
            throw new IOException( "Error: source PDF is encrypted, can't append encrypted PDF documents." );
        }
        PDDocumentInformation destInfo = destination.getDocumentInformation();
        PDDocumentInformation srcInfo = source.getDocumentInformation();
        destInfo.getDictionary().mergeInto( srcInfo.getDictionary() );

        PDDocumentCatalog destCatalog = destination.getDocumentCatalog();
        PDDocumentCatalog srcCatalog = source.getDocumentCatalog();

        // use the highest version number for the resulting pdf
        float destVersion = destination.getDocument().getVersion(); 
        float srcVersion = source.getDocument().getVersion(); 

        if (destVersion < srcVersion)
        {
            destination.getDocument().setVersion(srcVersion);
        }
            
        if( destCatalog.getOpenAction() == null )
        {
            destCatalog.setOpenAction( srcCatalog.getOpenAction() );
        }

        // maybe there are some shared resources for all pages 
        COSDictionary srcPages = (COSDictionary)srcCatalog.getCOSDictionary().getDictionaryObject( COSName.PAGES );
        COSDictionary srcResources = (COSDictionary)srcPages.getDictionaryObject( COSName.RESOURCES );
        COSDictionary destPages = (COSDictionary)destCatalog.getCOSDictionary().getDictionaryObject( COSName.PAGES );
        COSDictionary destResources = (COSDictionary)destPages.getDictionaryObject( COSName.RESOURCES );
        if (srcResources != null) 
        {
            if (destResources != null)
            {
                destResources.mergeInto(srcResources);
            }
            else
            {
                destPages.setItem(COSName.RESOURCES, srcResources);
            }
        }
        
        PDFCloneUtility cloner = new PDFCloneUtility(destination);

        try
        {
            PDAcroForm destAcroForm = destCatalog.getAcroForm();
            PDAcroForm srcAcroForm = srcCatalog.getAcroForm();
            if( destAcroForm == null )
            {
                cloner.cloneForNewDocument( srcAcroForm );
                destCatalog.setAcroForm( srcAcroForm );
            }
            else
            {
                if( srcAcroForm != null )
                {
                    mergeAcroForm(cloner, destAcroForm, srcAcroForm);
                }
            }
        }
        catch(Exception e)
        {
            throw new IOException(e.getMessage());
        }

        COSArray destThreads = (COSArray)destCatalog.getCOSDictionary().getDictionaryObject(
                COSName.THREADS);
        COSArray srcThreads = (COSArray)cloner.cloneForNewDocument(
                destCatalog.getCOSDictionary().getDictionaryObject( COSName.THREADS ));
        if( destThreads == null )
        {
            destCatalog.getCOSDictionary().setItem( COSName.THREADS, srcThreads );
        }
        else
        {
            destThreads.addAll( srcThreads );
        }

        PDDocumentNameDictionary destNames = destCatalog.getNames();
        PDDocumentNameDictionary srcNames = srcCatalog.getNames();
        if( srcNames != null )
        {
            if( destNames == null )
            {
                destCatalog.getCOSDictionary().setItem( COSName.NAMES,
                        cloner.cloneForNewDocument( srcNames ) );
            }
            else
            {
                cloner.cloneMerge(srcNames, destNames);
            }

        }

        PDDocumentOutline destOutline = destCatalog.getDocumentOutline();
        PDDocumentOutline srcOutline = srcCatalog.getDocumentOutline();
        if( srcOutline != null )
        {
            if( destOutline == null )
            {
                PDDocumentOutline cloned =
                    new PDDocumentOutline( (COSDictionary)cloner.cloneForNewDocument( srcOutline ) );
                destCatalog.setDocumentOutline( cloned );
            }
            else
            {
                PDOutlineItem first = srcOutline.getFirstChild();
                if(first != null)
                {
                    PDOutlineItem clonedFirst = new PDOutlineItem(
                            (COSDictionary)cloner.cloneForNewDocument( first ));
                    destOutline.appendChild( clonedFirst );
                }
            }
        }

        String destPageMode = destCatalog.getPageMode();
        String srcPageMode = srcCatalog.getPageMode();
        if( destPageMode == null )
        {
            destCatalog.setPageMode( srcPageMode );
        }

        COSDictionary destLabels = (COSDictionary)destCatalog.getCOSDictionary().getDictionaryObject(
                COSName.PAGE_LABELS);
        COSDictionary srcLabels = (COSDictionary)srcCatalog.getCOSDictionary().getDictionaryObject(
                COSName.PAGE_LABELS);
        if( srcLabels != null )
        {
            int destPageCount = destination.getNumberOfPages();
            COSArray destNums = null;
            if( destLabels == null )
            {
                destLabels = new COSDictionary();
                destNums = new COSArray();
                destLabels.setItem( COSName.NUMS, destNums );
                destCatalog.getCOSDictionary().setItem( COSName.PAGE_LABELS, destLabels );
            }
            else
            {
                destNums = (COSArray)destLabels.getDictionaryObject( COSName.NUMS );
            }
            COSArray srcNums = (COSArray)srcLabels.getDictionaryObject( COSName.NUMS );
            if (srcNums != null)
            {
                for( int i=0; i<srcNums.size(); i+=2 )
                {
                    COSNumber labelIndex = (COSNumber)srcNums.getObject( i );
                    long labelIndexValue = labelIndex.intValue();
                    destNums.add( COSInteger.get( labelIndexValue + destPageCount ) );
                    destNums.add( cloner.cloneForNewDocument( srcNums.getObject( i+1 ) ) );
                }
            }
        }

        COSStream destMetadata = (COSStream)destCatalog.getCOSDictionary().getDictionaryObject( COSName.METADATA );
        COSStream srcMetadata = (COSStream)srcCatalog.getCOSDictionary().getDictionaryObject( COSName.METADATA );
        if( destMetadata == null && srcMetadata != null )
        {
            PDStream newStream = new PDStream( destination, srcMetadata.getUnfilteredStream(), false );
            newStream.getStream().mergeInto( srcMetadata );
            newStream.addCompression();
            destCatalog.getCOSDictionary().setItem( COSName.METADATA, newStream );
        }

        //finally append the pages
        List<PDPage> pages = srcCatalog.getAllPages();
        Iterator<PDPage> pageIter = pages.iterator();
        while( pageIter.hasNext() )
        {
            PDPage page = pageIter.next();
            PDPage newPage =
                new PDPage( (COSDictionary)cloner.cloneForNewDocument( page.getCOSDictionary() ) );
            newPage.setCropBox( page.findCropBox() );
            newPage.setMediaBox( page.findMediaBox() );
            newPage.setRotation( page.findRotation() );
            destination.addPage( newPage );
        }
    }


    private static int nextFieldNum = 1;

    /**
     * Merge the contents of the source form into the destination form
     * for the destination file.
     *
     * @param cloner the object cloner for the destination document
     * @param destAcroForm the destination form
     * @param srcAcroForm the source form
     * @throws IOException If an error occurs while adding the field.
     */
    private static void mergeAcroForm(PDFCloneUtility cloner, PDAcroForm destAcroForm, PDAcroForm srcAcroForm)
        throws IOException
    {
        List destFields = destAcroForm.getFields();
        List srcFields = srcAcroForm.getFields();
        if( srcFields != null )
        {
            if( destFields == null )
            {
                destFields = new COSArrayList();
                destAcroForm.setFields( destFields );
            }
            Iterator srcFieldsIterator = srcFields.iterator();
            while (srcFieldsIterator.hasNext())
            {
                PDField srcField = (PDField)srcFieldsIterator.next();
                PDField destField =
                    PDFieldFactory.createField(
                        destAcroForm,
                        (COSDictionary)cloner.cloneForNewDocument(srcField.getDictionary() ));
                // if the form already has a field with this name then we need to rename this field
                // to prevent merge conflicts.
                if ( destAcroForm.getField(destField.getFullyQualifiedName()) != null )
                {
                    destField.setPartialName("dummyFieldName"+(nextFieldNum++));
                }
                destFields.add(destField);
            }
        }
    }
}
