package com.gmi.nordborglab.browser.server.domain.cdv;

import com.fasterxml.jackson.annotation.JsonView;
import com.gmi.nordborglab.browser.server.controller.rest.json.Views;
import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.util.GWASRuntimeInfo;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cdv_g2p_protocol", schema = "cdv")
@AttributeOverride(name = "id", column = @Column(name = "cdv_g2p_protocol_id"))
@SequenceGenerator(name = "idSequence", sequenceName = "cdv.cdv_source_cdv_source_id_seq", allocationSize = 1)
public class StudyProtocol extends BaseEntity {

    @JsonView(Views.Public.class)
    private String analysis_method;

    @JsonView(Views.Public.class)
    private String type;

    @JsonView(Views.Public.class)
    private String fullname;

    @JsonView(Views.Public.class)
    private String description;

    @OneToMany(mappedBy = "protocol", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Study> studies = new ArrayList<Study>();

    @OneToMany(mappedBy = "studyProtocol", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<GWASRuntimeInfo> gwasRuntimeInfos = new HashSet<>();

    public String getAnalysisMethod() {
        return analysis_method;
    }

    public void setAnalysisMethod(String analysis_method) {
        this.analysis_method = analysis_method;
    }

    protected void addStudy(Study study) {
        studies.add(study);
    }

    public List<Study> getStudies() {
        return Collections.unmodifiableList(studies);
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<GWASRuntimeInfo> getGwasRuntimeInfos() {
        return gwasRuntimeInfos;
    }
}
