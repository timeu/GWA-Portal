package com.gmi.nordborglab.browser.shared.service;

import com.google.web.bindery.requestfactory.shared.LoggingRequest;
import com.google.web.bindery.requestfactory.shared.RequestFactory;

public interface CustomRequestFactory extends RequestFactory {

    ExperimentRequest experimentRequest();

    PhenotypeRequest phenotypeRequest();

    HelperRequest helperRequest();

    ObsUnitRequest obsUnitRequest();

    CdvRequest cdvRequest();

    PermissionRequest permissionRequest();

    TraitRequest traitRequest();

    TaxonomyRequest taxonomyRequest();

    PassportRequest passportRequest();

    StockRequest stockRequest();

    SearchRequest searchRequest();

    GWASDataRequest gwasDataRequest();

    OntologyRequest ontologyRequest();

    AnnotationDataRequest annotationDataRequest();

    MetaAnalysisRequest metaAnalysisRequest();

    UserRequest userRequest();

    LoggingRequest loggingRequest();
}
