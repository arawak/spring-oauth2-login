package com.mitchwood.sol.user;

import java.util.Set;

public interface UserService {

    /**
     * Lookup a user by IdP and subject.
     * 
     * @param registrationId identifies the IdP
     * @param subClaim unique user id for that IdP
     * @return null if it doesn exist
     */
    User getUserByRegistrationAndSubject(String registrationId, String subClaim);

    Set<String> getRolesForUser(User user);
    
}
