package org.sagebionetworks.bridge;

import java.util.EnumSet;
import java.util.Set;

public enum Roles {
    DEVELOPER,
    RESEARCHER,
    ADMIN,
    TEST_USERS;
    
    public static final Set<Roles> ADMINISTRATIVE_ROLES = EnumSet.of(Roles.DEVELOPER, Roles.RESEARCHER, Roles.ADMIN);
}
