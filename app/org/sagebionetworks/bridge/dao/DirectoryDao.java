package org.sagebionetworks.bridge.dao;

import org.sagebionetworks.bridge.config.Environment;

public interface DirectoryDao {

    public String createDirectory(Environment env, String identifier);
    
    public void renameStudyIdentifier(Environment env, String oldIdentifier, String newIdentifier);
    
    public void deleteDirectory(Environment env, String directoryHref);
    
}