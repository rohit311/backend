package com.example.multimodal.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PdfToImageConverter {
    private static final int MAX_PAGES = 5; // Guard against massive PDFs
    private static final float RENDER_DPI = 150f;

    public List<Resource> convertToPngResources(byte[] pdfBytes) throws IOException {
        List<Resource> pageResources = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pagesToRender = Math.min(document.getNumberOfPages(), MAX_PAGES);

            for (int pageIndex = 0; pageIndex < pagesToRender; pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, RENDER_DPI, ImageType.RGB);

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(image, "png", baos);
                    pageResources.add(new ByteArrayResource(baos.toByteArray()));
                }
            }
        }
        return pageResources;
    }
}
