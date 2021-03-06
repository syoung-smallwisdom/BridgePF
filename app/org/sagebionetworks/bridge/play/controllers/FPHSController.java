package org.sagebionetworks.bridge.play.controllers;

import static org.sagebionetworks.bridge.Roles.ADMIN;

import java.util.List;

import org.sagebionetworks.bridge.json.BridgeObjectMapper;
import org.sagebionetworks.bridge.models.accounts.ExternalIdentifier;
import org.sagebionetworks.bridge.models.accounts.FPHSExternalIdentifier;
import org.sagebionetworks.bridge.models.accounts.UserSession;
import org.sagebionetworks.bridge.services.FPHSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.type.TypeReference;

import play.mvc.Result;

@Controller("fphsController")
public class FPHSController extends BaseController {
    
    private static final TypeReference<List<FPHSExternalIdentifier>> EXTERNAL_ID_TYPE_REF = 
            new TypeReference<List<FPHSExternalIdentifier>>() {};

    private static final BridgeObjectMapper MAPPER = BridgeObjectMapper.get();

    private FPHSService fphsService;
    
    @Autowired
    public final void setFPHSService(FPHSService service) {
        this.fphsService = service; 
    }
    
    public Result verifyExternalIdentifier(String identifier) throws Exception {
        // public API, no restrictions. externalId can be null so we can create a 400 error in the service.
        ExternalIdentifier externalId = new ExternalIdentifier(identifier);
        fphsService.verifyExternalIdentifier(externalId);
        
        return okResult(externalId);
    }
    public Result registerExternalIdentifier() throws Exception {
        UserSession session = getAuthenticatedAndConsentedSession();
        
        ExternalIdentifier externalId = parseJson(request(), ExternalIdentifier.class);
        fphsService.registerExternalIdentifier(session.getStudyIdentifier(), session.getUser().getHealthCode(), externalId);
        return okResult("External identifier added to user profile.");
    }
    public Result getExternalIdentifiers() throws Exception {
        getAuthenticatedSession(ADMIN);
        
        List<FPHSExternalIdentifier> identifiers = fphsService.getExternalIdentifiers();
        
        return okResult(identifiers);
    }
    public Result addExternalIdentifiers() throws Exception {
        getAuthenticatedSession(ADMIN);
        
        List<FPHSExternalIdentifier> externalIds = MAPPER.convertValue(requestToJSON(request()), EXTERNAL_ID_TYPE_REF);
        fphsService.addExternalIdentifiers(externalIds);
        
        return okResult("External identifiers added.");
    }
}
