package com.gmi.nordborglab.browser.server.domain.pages;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class StudyPage extends PageImpl<Study> {

    protected List<ESFacet> facets;

    public StudyPage() {
        super(new ArrayList<Study>(), new PageRequest(0, 1), 0);
    }

    public StudyPage(List<Study> content, Pageable pageable, long total, List<ESFacet> facets) {
        super(content, pageable, total);
        this.facets = facets;
    }

    public List<Study> getContents() {
        return getContent();
    }

    public List<ESFacet> getFacets() {
        return facets;
    }
}
