package controllers;

import static org.sagebionetworks.bridge.BridgeConstants.JSON_MIME_TYPE;

import java.util.List;

import org.sagebionetworks.bridge.BridgeConstants;
import org.sagebionetworks.bridge.cache.ViewCache;
import org.sagebionetworks.bridge.dynamodb.DynamoSurvey;
import org.sagebionetworks.bridge.exceptions.UnauthorizedException;
import org.sagebionetworks.bridge.json.DateUtils;
import org.sagebionetworks.bridge.models.GuidCreatedOnVersionHolder;
import org.sagebionetworks.bridge.models.GuidCreatedOnVersionHolderImpl;
import org.sagebionetworks.bridge.models.UserSession;
import org.sagebionetworks.bridge.models.studies.Study;
import org.sagebionetworks.bridge.models.surveys.Survey;
import org.sagebionetworks.bridge.services.SurveyService;

import com.google.common.base.Supplier;

import play.mvc.Result;

public class SurveyController extends BaseController {

    private static final String MOSTRECENT_KEY = "mostrecent";
    private static final String PUBLISHED_KEY = "published";

    private SurveyService surveyService;
    
    private ViewCache viewCache;
    
    public void setSurveyService(SurveyService surveyService) {
        this.surveyService = surveyService;
    }
    
    public void setViewCache(ViewCache viewCache) {
        this.viewCache = viewCache;
    }
    
    public Result getAllSurveysMostRecentVersion() throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);

        List<Survey> surveys = surveyService.getAllSurveysMostRecentVersion(study);
        verifySurveyIsInStudy(session, study, surveys);
        return okResult(surveys);
    }
    
    public Result getAllSurveysMostRecentVersion2() throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);

        List<Survey> surveys = surveyService.getAllSurveysMostRecentVersion(study);
        verifySurveyIsInStudy(session, study, surveys);
        return okResult(surveys);
    }
    
    public Result getAllSurveysMostRecentlyPublishedVersion() throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);

        List<Survey> surveys = surveyService.getAllSurveysMostRecentlyPublishedVersion(study);
        verifySurveyIsInStudy(session, study, surveys);
        return okResult(surveys);
    }
    
    public Result getSurveyForUser(final String surveyGuid, final String createdOnString) throws Exception {
        final UserSession session = getAuthenticatedAndConsentedSession();

        String cacheKey = getCacheKey(surveyGuid, createdOnString); 
        String json = viewCache.getView(Survey.class, cacheKey, new Supplier<Survey>() {
            @Override public Survey get() {
                Study study = getStudy();
                long createdOn = DateUtils.convertToMillisFromEpoch(createdOnString);
                GuidCreatedOnVersionHolder keys = new GuidCreatedOnVersionHolderImpl(surveyGuid, createdOn);

                Survey survey = surveyService.getSurvey(keys);
                verifySurveyIsInStudy(session, study, survey);
                return surveyService.getSurvey(keys);
            }
        });
        return ok(json).as(JSON_MIME_TYPE);
    }

    public Result getSurveyMostRecentlyPublishedVersionForUser(final String surveyGuid) throws Exception {
        final UserSession session = getAuthenticatedAndConsentedSession();
        
        String cacheKey = getCacheKey(surveyGuid, PUBLISHED_KEY);
        String json = viewCache.getView(Survey.class, cacheKey, new Supplier<Survey>() {
            @Override public Survey get() {
                Study study = getStudy();
                Survey survey = surveyService.getSurveyMostRecentlyPublishedVersion(study, surveyGuid);
                verifySurveyIsInStudy(session, study, survey);
                return survey;
            }
        });
        return ok(json).as(JSON_MIME_TYPE);
    }
    
    // Otherwise you don't need consent but you must be a researcher or an administrator
    public Result getSurvey(final String surveyGuid, final String createdOnString) throws Exception {
        final Study study = getStudy();
        final UserSession session = getAuthenticatedResearcherOrAdminSession(study);
        
        String cacheKey = getCacheKey(surveyGuid, createdOnString); 
        String json = viewCache.getView(Survey.class, cacheKey, new Supplier<Survey>() {
            @Override public Survey get() {
                long createdOn = DateUtils.convertToMillisFromEpoch(createdOnString);
                GuidCreatedOnVersionHolder keys = new GuidCreatedOnVersionHolderImpl(surveyGuid, createdOn);
                Survey survey = surveyService.getSurvey(keys);
                verifySurveyIsInStudy(session, study, survey);
                return survey;
            }
        });
        return ok(json).as(JSON_MIME_TYPE);
    }
    
    public Result getSurveyMostRecentVersion(final String surveyGuid) throws Exception {
        final Study study = getStudy();
        final UserSession session = getAuthenticatedResearcherOrAdminSession(study);
        
        String cacheKey = getCacheKey(surveyGuid, MOSTRECENT_KEY);
        String json = viewCache.getView(Survey.class, cacheKey, new Supplier<Survey>() {
            @Override public Survey get() {
                Survey survey = surveyService.getSurveyMostRecentVersion(study, surveyGuid);
                verifySurveyIsInStudy(session, study, survey);
                return survey;
            }
        });
        return ok(json).as(JSON_MIME_TYPE);
    }
    
    public Result getSurveyMostRecentlyPublishedVersion(final String surveyGuid) throws Exception {
        final Study study = getStudy();
        final UserSession session = getAuthenticatedResearcherOrAdminSession(study);
        
        String cacheKey = getCacheKey(surveyGuid, PUBLISHED_KEY);
        String json = viewCache.getView(Survey.class, cacheKey, new Supplier<Survey>() {
            @Override public Survey get() {
                Survey survey = surveyService.getSurveyMostRecentlyPublishedVersion(study, surveyGuid);
                verifySurveyIsInStudy(session, study, survey);
                return survey;
            }
        });
        return ok(json).as(JSON_MIME_TYPE);
    }
    
    public Result getMostRecentPublishedSurveyVersionByIdentifier(String identifier) throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);
        
        // Do not cache this. It's only used by researchers and without the GUID, you cannot
        // cache it properly.
        Survey survey = surveyService.getSurveyMostRecentlyPublishedVersionByIdentifier(study, identifier);
        verifySurveyIsInStudy(session, study, survey);
        return okResult(survey);
    }
    
    public Result deleteSurvey(String surveyGuid, String createdOnString) throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);
        
        long createdOn = DateUtils.convertToMillisFromEpoch(createdOnString);
        GuidCreatedOnVersionHolder keys = new GuidCreatedOnVersionHolderImpl(surveyGuid, createdOn);
        
        Survey survey = surveyService.getSurvey(keys);
        verifySurveyIsInStudy(session, study, survey);
        
        surveyService.deleteSurvey(study, survey);
        expireCache(surveyGuid, createdOnString);
        
        return okResult("Survey deleted.");
    }
    
    public Result getSurveyAllVersions(String surveyGuid) throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);
        
        List<Survey> surveys = surveyService.getSurveyAllVersions(study, surveyGuid);
        verifySurveyIsInStudy(session, study, surveys);
        return okResult(surveys);
    }
    
    public Result createSurvey() throws Exception {
        Study study = getStudy();
        getAuthenticatedResearcherOrAdminSession(study);
        
        Survey survey = DynamoSurvey.fromJson(requestToJSON(request()));
        survey.setStudyIdentifier(study.getIdentifier());
        
        survey = surveyService.createSurvey(survey);
        return createdResult(new GuidCreatedOnVersionHolderImpl(survey));
    }
    
    public Result versionSurvey(String surveyGuid, String createdOnString) throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);
        
        long createdOn = DateUtils.convertToMillisFromEpoch(createdOnString);
        GuidCreatedOnVersionHolder keys = new GuidCreatedOnVersionHolderImpl(surveyGuid, createdOn);
        
        Survey survey = surveyService.getSurvey(keys);
        verifySurveyIsInStudy(session, study, survey);

        // This creates a new version of the survey, and so, no cache clearing needs to occur
        survey = surveyService.versionSurvey(survey);
        return createdResult(new GuidCreatedOnVersionHolderImpl(survey));
    }
    
    public Result updateSurvey(String surveyGuid, String createdOnString) throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);
        
        long createdOn = DateUtils.convertToMillisFromEpoch(createdOnString);
        GuidCreatedOnVersionHolder keys = new GuidCreatedOnVersionHolderImpl(surveyGuid, createdOn);
        
        Survey survey = surveyService.getSurvey(keys);
        verifySurveyIsInStudy(session, study, survey);
        
        // The parameters in the URL take precedence over anything declared in 
        // the object itself.
        survey = DynamoSurvey.fromJson(requestToJSON(request()));
        survey.setGuid(surveyGuid);
        survey.setCreatedOn(createdOn);
        survey.setStudyIdentifier(study.getIdentifier());
        
        survey = surveyService.updateSurvey(survey);
        expireCache(surveyGuid, createdOnString);
        
        return okResult(new GuidCreatedOnVersionHolderImpl(survey));
    }
    
    public Result publishSurvey(String surveyGuid, String createdOnString) throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);
         
        long createdOn = DateUtils.convertToMillisFromEpoch(createdOnString);
        GuidCreatedOnVersionHolder keys = new GuidCreatedOnVersionHolderImpl(surveyGuid, createdOn);
        
        Survey survey = surveyService.getSurvey(keys);
        verifySurveyIsInStudy(session, study, survey);
        
        survey = surveyService.publishSurvey(survey);
        expireCache(surveyGuid, createdOnString);
        
        return okResult(new GuidCreatedOnVersionHolderImpl(survey));
    }
    
    public Result closeSurvey(String surveyGuid, String createdOnString) throws Exception {
        Study study = getStudy();
        UserSession session = getAuthenticatedResearcherOrAdminSession(study);
        
        long createdOn = DateUtils.convertToMillisFromEpoch(createdOnString);
        GuidCreatedOnVersionHolder keys = new GuidCreatedOnVersionHolderImpl(surveyGuid, createdOn);
        
        Survey survey = surveyService.getSurvey(keys);
        verifySurveyIsInStudy(session, study, survey);
        
        surveyService.closeSurvey(survey);
        expireCache(surveyGuid, createdOnString);
        
        return okResult("Survey closed.");
    }
    
    private void verifySurveyIsInStudy(UserSession session, Study study, List<Survey> surveys) {
        if (!surveys.isEmpty()) {
            verifySurveyIsInStudy(session, study, surveys.get(0));
        }
    }
    
    private void verifySurveyIsInStudy(UserSession session, Study study, Survey survey) {
        if (!session.getUser().isInRole(BridgeConstants.ADMIN_GROUP) && 
            !survey.getStudyIdentifier().equals(study.getIdentifier())) {
            throw new UnauthorizedException();
        }
    }

    private String getCacheKey(String surveyGuid, String createdOnString) {
        return String.format("%s:%s", surveyGuid, createdOnString);
    }
    
    private void expireCache(String surveyGuid, String createdOnString) {
        // Don't screw around trying to figure out if *this* survey instance is the same survey
        // as the most recent or published version, expire all versions in the cache
        viewCache.removeView(Survey.class, getCacheKey(surveyGuid, createdOnString));
        viewCache.removeView(Survey.class, getCacheKey(surveyGuid, PUBLISHED_KEY));
        viewCache.removeView(Survey.class, getCacheKey(surveyGuid, MOSTRECENT_KEY));
    }
    
}