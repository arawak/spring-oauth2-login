package com.mitchwood.sol.user;

import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class DummyUserService implements UserService {

    @Override
    public User getUserByRegistrationAndSubject(String registrationId, String subClaim) {
        return User.builder()
            .username("Phreddie")
            .build();
    }

    @Override
    public Set<String> getRolesForUser(User user) {
        return Set.of("USER", "FORUM_1");
    }
    
}
