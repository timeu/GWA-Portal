package com.gmi.nordborglab.browser.server.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
* Created with IntelliJ IDEA.
* User: uemit.seren
* Date: 1/16/13
* Time: 12:45 PM
* To change this template use File | Settings | File Templates.
*/
@XmlRootElement(name = "studygwasdata")
public class StudyGWASData {


    private String csvData;
    private String analysisMethod;
    private int genotype;

    public StudyGWASData() {
    }

    public StudyGWASData(String csvData, String analysisMethod, int genotype) {

        this.csvData = csvData;
        this.analysisMethod = analysisMethod;
        this.genotype = genotype;
    }

    @XmlElement
    public String getCsvData() {
        return csvData;
    }

    @XmlElement
    public String getAnalysisMethod() {
        return analysisMethod;
    }

    @XmlElement
    public int getGenotype() {
        return genotype;
    }
}