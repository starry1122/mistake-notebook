package com.wsy.mistake_notebook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mistake")
public class MistakeModuleProperties {

    private String uploadDir = "uploads";
    private Ocr ocr = new Ocr();

    public static class Ocr {
        // mock / tesseract / off
        private String mode = "tesseract";
        private String tesseractCmd = "tesseract";
        private String lang = "chi_sim+eng";

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }

        public String getTesseractCmd() { return tesseractCmd; }
        public void setTesseractCmd(String tesseractCmd) { this.tesseractCmd = tesseractCmd; }

        public String getLang() { return lang; }
        public void setLang(String lang) { this.lang = lang; }
    }

    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }

    public Ocr getOcr() { return ocr; }
    public void setOcr(Ocr ocr) { this.ocr = ocr; }
}
