package org.sagebionetworks.bridge.play.controllers;

import static org.sagebionetworks.bridge.Roles.ADMIN;
import static org.sagebionetworks.bridge.Roles.DEVELOPER;
import static org.sagebionetworks.bridge.Roles.RESEARCHER;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.bridge.config.BridgeConfigFactory;
import org.sagebionetworks.bridge.models.CmsPublicKey;
import org.sagebionetworks.bridge.models.ResourceList;
import org.sagebionetworks.bridge.models.VersionHolder;
import org.sagebionetworks.bridge.models.accounts.UserSession;
import org.sagebionetworks.bridge.models.studies.Study;
import org.sagebionetworks.bridge.models.studies.StudyIdentifier;
import org.sagebionetworks.bridge.services.UploadCertificateService;
import org.sagebionetworks.bridge.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import play.libs.Json;
import play.mvc.Result;

@Controller
public class StudyController extends BaseController {

    private final Comparator<Study> STUDY_COMPARATOR = new Comparator<Study>() {
        public int compare(Study study1, Study study2) {
            return study1.getName().compareToIgnoreCase(study2.getName());
        }
    };

    private final Set<String> studyWhitelist = Collections
            .unmodifiableSet(new HashSet<>(BridgeConfigFactory.getConfig().getPropertyAsList("study.whitelist")));

    private UserProfileService userProfileService;

    private UploadCertificateService uploadCertificateService;

    @Autowired
    public final void setUserProfileService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Autowired
    public final void setUploadCertificateService(UploadCertificateService uploadCertificateService) {
        this.uploadCertificateService = uploadCertificateService;
    }

    @Deprecated
    public Result getStudyList() throws Exception {
        List<Study> studies = studyService.getStudies();

        return ok(Study.STUDY_LIST_WRITER.writeValueAsString(new ResourceList<Study>(studies)));
    }

    public Result getStudyForDeveloper() throws Exception {
        UserSession session = getAuthenticatedSession(DEVELOPER);
        Study study = studyService.getStudy(session.getStudyIdentifier());

        return ok(Study.STUDY_WRITER.writeValueAsString(study));
    }

    public Result sendStudyParticipantsRoster() throws Exception {
        UserSession session = getAuthenticatedSession(RESEARCHER);
        Study study = studyService.getStudy(session.getStudyIdentifier());

        userProfileService.sendStudyParticipantRoster(study);
        
        return acceptedResult("A roster of study participants will be emailed to the study's consent notification contact.");
    }

    public Result updateStudyForDeveloper() throws Exception {
        UserSession session = getAuthenticatedSession(DEVELOPER);
        StudyIdentifier studyId = session.getStudyIdentifier();

        Study studyUpdate = parseJson(request(), Study.class);
        studyUpdate.setIdentifier(studyId.getIdentifier());
        studyUpdate = studyService.updateStudy(studyUpdate, false);
        return okResult(new VersionHolder(studyUpdate.getVersion()));
    }

    public Result updateStudy(String identifier) throws Exception {
        getAuthenticatedSession(ADMIN);

        Study studyUpdate = parseJson(request(), Study.class);
        studyUpdate.setIdentifier(identifier);
        studyUpdate = studyService.updateStudy(studyUpdate, true);
        return okResult(new VersionHolder(studyUpdate.getVersion()));
    }

    public Result getStudy(String identifier) throws Exception {
        getAuthenticatedSession(ADMIN);

        Study study = studyService.getStudy(identifier);
        return ok(Study.STUDY_WRITER.writeValueAsString(study));
    }

    public Result getAllStudies(String format) throws Exception {
        List<Study> studies = studyService.getStudies();
        if ("summary".equals(format)) {
            Collections.sort(studies, STUDY_COMPARATOR);
            return ok(Study.STUDY_LIST_WRITER.writeValueAsString(new ResourceList<Study>(studies)));
        }
        getAuthenticatedSession(ADMIN);

        return ok(Study.STUDY_WRITER.writeValueAsString(new ResourceList<Study>(studies)));
    }

    public Result createStudy() throws Exception {
        getAuthenticatedSession(ADMIN);

        Study study = parseJson(request(), Study.class);
        study = studyService.createStudy(study);
        return okResult(new VersionHolder(study.getVersion()));
    }

    public Result deleteStudy(String identifier) throws Exception {
        getAuthenticatedSession(ADMIN);
        if (studyWhitelist.contains(identifier)) {
            return forbidden(Json.toJson(identifier + " is protected by whitelist."));
        }
        studyService.deleteStudy(identifier);
        return okResult("Study deleted.");
    }

    public Result getStudyPublicKeyAsPem() throws Exception {
        UserSession session = getAuthenticatedSession(DEVELOPER);

        String pem = uploadCertificateService.getPublicKeyAsPem(session.getStudyIdentifier());

        return okResult(new CmsPublicKey(pem));
    }
}
