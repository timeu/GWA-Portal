package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.cdv.Transformation;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.AlleleAssayRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitRepository;
import com.gmi.nordborglab.browser.server.repository.TransformationRepository;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CdvServiceTest extends BaseTest {

    @Resource
    private CdvService service;

    @Resource
    private TraitRepository traitRepository;

    @Resource
    private StudyRepository studyRepository;

    @Resource
    private MutableAclService aclService;

    @Resource
    private TransformationRepository transformationRepository;

    @Resource
    private AlleleAssayRepository alleleAssayRepository;

    @Before
    public void setUp() {
        SecurityUtils.setAnonymousUser();
    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }

    @Test
    public void testFindStudiesByPhenotypeId() {
        SecurityUtils.setAnonymousUser();
        StudyPage page = service.findStudiesByPhenotypeId(1L, 0, 50);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    public void checkNoVisiblePermissionWhenNoAdmin() {
        SecurityUtils.setAnonymousUser();
        Study study = service.findStudy(1L);
        assertTrue((study.getUserPermission().getMask() & CustomPermission.READ.getMask()) == CustomPermission.READ.getMask());
        assertFalse(study.isOwner());
    }

    @Test
    public void testFindPhenotype() {
        SecurityUtils.setAnonymousUser();
        Study study = service.findStudy(1L);
        assertNotNull("couldn't find phenotype", study);
    }


    @Test
    public void checkVisiblePermissionsWhenAdmin() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        Study study = service.findStudy(1L);
        assertTrue(study.isOwner());
    }

    @Test
    public void testSaveStudy() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        Study study = new Study();
        study.setName("test");
        Set<Trait> traits = new HashSet<>(traitRepository.findAllByStudiesId(160L));
        Transformation transformation = transformationRepository.findOne(1L);
        study.setTransformation(transformation);
        AlleleAssay alleleAssay = alleleAssayRepository.findOne(1L);
        study.setAlleleAssay(alleleAssay);
        study.setTraits(traits);
        study = service.saveStudy(study);
        assertNotNull(study);
        assertEquals("test", study.getName());
        assertNotNull(study.getTraits());
        assertEquals(194L, study.getTraits().size());
        assertNotNull(study.getStudyDate());
        assertEquals("TEST null", study.getProducer());
        assertThat(study.getPseudoHeritability(), is(0.9017288538771943));
        assertThat(study.getShapiroWilkPvalue(), is(7.093110844098556E-11));
    }

    @Test(expected = RuntimeException.class)
    public void testSaveStudyExceptionWhenNoPhenotype() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        Study study = new Study();
        study.setName("test");
        study = service.saveStudy(study);
    }

    @Test(expected = AccessDeniedException.class)
    public void testSaveStudyAccessDeniedWhenNoAccessToAlleleAssay() {
        Collection<? extends GrantedAuthority> userAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_USER")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", userAuthorities);
        Study study = new Study();
        AlleleAssay alleleAssay = alleleAssayRepository.findOne(3L);
        study.setAlleleAssay(alleleAssay);
        service.saveStudy(study);
    }


    @Test(expected = AccessDeniedException.class)
    public void testSaveStudyAccessDenied() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        Study study = studyRepository.findOne(1L);
        SecurityUtils.setAnonymousUser();
        service.saveStudy(study);
    }

    @Test
    public void testFindStudiesByPassportId() {
        SecurityUtils.setAnonymousUser();
        List<Study> studies = service.findStudiesByPassportId(6964L);
        assertNotNull("no studies returned", studies);
        assertEquals("wrong number of studies", 2, studies.size());
        assertEquals("wrong study", 850, studies.get(0).getId().longValue());
        assertEquals("wrong study", 1, studies.get(1).getId().longValue());
    }

    @Test
    public void testFindAll() {
        SecurityUtils.setAnonymousUser();
        StudyPage page = service.findAll(null, null, 0, 50);
        assertEquals(2, page.getTotalElements());
    }

    @Test
    public void testFindTraitValues() {
        List<Trait> traits = service.findTraitValues(1L);
        assertNotNull(traits);
        assertEquals(334, traits.size());
    }

    @Test
    public void tesFindAlleleAssayWithStats() {
        List<AlleleAssay> alleleAssays = service.findAlleleAssaysWithStats(1L, 2L);
        assertNotNull(alleleAssays);
        assertEquals(3, alleleAssays.size());
        assertEquals(167, alleleAssays.get(0).getAvailableAllelesCount());
        assertEquals(167, alleleAssays.get(0).getTraitValuesCount());
        assertEquals(0, alleleAssays.get(1).getAvailableAllelesCount());
        assertEquals(167, alleleAssays.get(1).getTraitValuesCount());
    }


    @Test
    public void tesFindAlleleAssayWithAsAdmin() {
        Collection<? extends GrantedAuthority> userAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", userAuthorities);
        List<AlleleAssay> alleleAssays = service.findAlleleAssaysWithStats(1L, 2L);
        assertNotNull(alleleAssays);
        assertEquals(5, alleleAssays.size());
    }

    @Test
    public void tesFindAlleleAssayWithStatsCorrectlyFiltered() {
        Collection<? extends GrantedAuthority> userAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_USER")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", userAuthorities);
        List<AlleleAssay> alleleAssays = service.findAlleleAssaysWithStats(1L, 2L);
        assertNotNull(alleleAssays);
        assertEquals(
                4, alleleAssays.size());
    }


    @Test(expected = AccessDeniedException.class)
    public void tesFindAlleleAssayWithStatsAccessDenied() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class, 1L);
        List<Sid> authorities = Collections.singletonList((Sid) new GrantedAuthoritySid("ROLE_ANONYMOUS"));
        MutableAcl acl = (MutableAcl) aclService.readAclById(oid, authorities).getParentAcl();

        for (int i = 0; i < acl.getEntries().size(); i++) {
            if (acl.getEntries().get(i).getSid().equals(authorities.get(0))) {
                acl.deleteAce(i);
                break;
            }
        }
        aclService.updateAcl(acl);
        SecurityUtils.setAnonymousUser();
        service.findAlleleAssaysWithStats(1L, 2L);
    }


    @Test(expected = AccessDeniedException.class)
    public void testFindStudiesByPhenotypeIdAccessedDenied() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        ObjectIdentity oid = new ObjectIdentityImpl(Experiment.class, 1L);
        List<Sid> authorities = Collections.singletonList((Sid) new GrantedAuthoritySid("ROLE_ANONYMOUS"));
        MutableAcl acl = (MutableAcl) aclService.readAclById(oid, authorities);

        for (int i = 0; i < acl.getEntries().size(); i++) {
            if (acl.getEntries().get(i).getSid().equals(authorities.get(0))) {
                acl.deleteAce(i);
                break;
            }
        }
        aclService.updateAcl(acl);
        SecurityUtils.setAnonymousUser();
        service.findStudiesByPhenotypeId(1L, 0, 50);
    }

    @Test
    public void testCreateStudyJob() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        Study study = service.createStudyJob(1L);
        assertNotNull(study);
        assertNotNull(study.getJob());
        assertEquals("Queued", study.getJob().getStatus());
        assertEquals(1, study.getJob().getProgress().doubleValue(), 0);
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateStudyJobPermissionDenied() {
        SecurityUtils.setAnonymousUser();
        Study study = service.createStudyJob(1L);
    }


}
