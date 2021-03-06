package com.gmi.nordborglab.browser.server.controller.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.gmi.nordborglab.browser.server.controller.rest.exceptions.ResourceNotFoundException;
import com.gmi.nordborglab.browser.server.controller.rest.json.Views;
import com.gmi.nordborglab.browser.server.controller.rest.util.PaginationUtil;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Created by uemit.seren on 7/8/15.
 */
@RestController
@RequestMapping("/api/analyses")
public class CdvRestController extends AbstractRestController {

    @Autowired
    private CdvService cdvService;



    @RequestMapping(method = RequestMethod.GET)
    @JsonView(Views.Studies.class)
    ResponseEntity<Collection<Study>> getAnalyses(@RequestParam(defaultValue = "0", value = "page") int page, @RequestParam(defaultValue = "50", value = "size") int size) {
        Page<Study> studyPage = cdvService.findAll(ConstEnums.TABLE_FILTER.ALL, null, page, size);
        HttpHeaders headers = null;
        if (page > studyPage.getTotalPages()) {
            throw new ResourceNotFoundException("No data found for the selected page");
        }
        try {
            headers = PaginationUtil.generatePaginationHttpHeaders(studyPage, "/api/analyses", page, size);
        } catch (URISyntaxException e) {

        }
        return new ResponseEntity<Collection<Study>>(studyPage.getContent(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{analysisId}", method = RequestMethod.GET)
    @JsonView(Views.StudyDetail.class)
    ResponseEntity<Study> getAnalysis(@PathVariable Long analysisId) {
        Study study = cdvService.findStudy(analysisId);
        if (study == null)
            throw new ResourceNotFoundException("Study not found");
        return new ResponseEntity<>(study, HttpStatus.OK);
    }

    @RequestMapping(value = "/{analysisId}/phenotype", method = RequestMethod.GET)
    @JsonView(Views.PhenotypeDetail.class)
    ResponseEntity<TraitUom> getPhenotypeOfAnalysis(@PathVariable Long analysisId) {
        Study study = cdvService.findStudy(analysisId);
        if (study == null)
            throw new ResourceNotFoundException("Study not found");
        return new ResponseEntity<>(study.getPhenotype(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{analysisId}/ld/{chr}/{position}", method = RequestMethod.GET)
    @PreAuthorize("hasPermission(#analysisId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    public void getLdForSnp(@PathVariable Long analysisId, @PathVariable String chr, @PathVariable Long position, OutputStream writer, HttpServletResponse response) throws IOException {
        // retrieve data
        passThroughRequest(response, "/analysis/" + analysisId + "/ld/" + chr + "/" + position);
    }

    @RequestMapping(value = "/{analysisId}/ld_region/{chr}/{startPos}/{endPos}", method = RequestMethod.GET)
    @PreAuthorize("hasPermission(#analysisId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    public void getLdForRegion(@PathVariable Long analysisId, @PathVariable String chr, @PathVariable Long startPos, @PathVariable Long endPos, OutputStream writer, HttpServletResponse response) throws IOException {
        // retrieve data
        passThroughRequest(response, "/analysis/" + analysisId + "/ld/region/" + chr + "/" + startPos + "/" + endPos);
    }

    @RequestMapping(value = "/{analysisId}/ld_exact/{chr}/{position}", method = RequestMethod.GET)
    @PreAuthorize("hasPermission(#analysisId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    public void getExactLd(@PathVariable Long analysisId, @PathVariable String chr, @PathVariable Long position, HttpServletResponse response) throws IOException {
        // retrieve data
        Study study = cdvService.findStudy(analysisId);
        Long genotypeId = study.getAlleleAssay().getId();
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean isFirst = true;
        for (Trait trait : study.getTraits()) {
            if (!isFirst) {
                builder.append(",");
            }
            builder.append(trait.getObsUnit().getStock().getPassport().getId()).append("");
            isFirst = false;
        }
        builder.append("]");
        passThroughRequest(response, "/ld/" + genotypeId + "/" + chr + "/" + position, builder.toString());
    }

    @RequestMapping(value = "/calculate_ld/{alleleAssayId}/{chr}/{position}", method = RequestMethod.POST)
    public void calculateLd(@RequestBody(required = false) String body, @PathVariable Long alleleAssayId, @PathVariable String chr, @PathVariable Long position, @RequestParam(defaultValue = "250") Integer range, HttpServletResponse response) throws IOException {
        if (range > 250) {
            throw new IOException("Only a range of 250 SNPs are supported");
        }
        String url = "/ld/" + alleleAssayId + "/" + chr + "/" + position + "?num_snps=" + range;
        passThroughRequest(response, url, body != null ? body : "");
    }


    @RequestMapping(method = RequestMethod.GET, value = "/{analysisId}/plots")
    @PreAuthorize("hasPermission(#analysisId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    public void downloadGWASPlot(HttpServletResponse response, HttpServletRequest request,
                                 @PathVariable Long analysisId,
                                 @RequestParam(value = "chr", required = false) String chr,
                                 @RequestParam(value = "mac", required = false, defaultValue = "15") Integer minMac,
                                 @RequestParam(value = "format", required = false, defaultValue = "png") String format) throws IOException {
        downloadPlotRequest(response, "study", "gwas", analysisId, chr, minMac, format);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/{analysisId}/qqplots")
    @PreAuthorize("hasPermission(#analysisId,'com.gmi.nordborglab.browser.server.domain.cdv.Study','READ')")
    public void downloadQQPlot(HttpServletResponse response, HttpServletRequest request,
                               @PathVariable Long analysisId,
                               @RequestParam(value = "format", required = false, defaultValue = "png") String format) throws IOException {
        downloadPlotRequest(response, "study", "qq", analysisId, null, 0, format);
    }


}
