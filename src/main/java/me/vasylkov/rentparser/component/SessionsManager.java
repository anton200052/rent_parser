package me.vasylkov.rentparser.component;

import lombok.RequiredArgsConstructor;
import me.vasylkov.rentparser.entity.User;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionsManager {
    private final SessionRegistry sessionRegistry;

    public void closeUserSession(User user) {
        String username = user.getUsername();
        for (Object p : sessionRegistry.getAllPrincipals()) {
            if (p instanceof User principal && principal.getUsername().equals(username)) {
                for (SessionInformation sessionInformation : sessionRegistry.getAllSessions(p, false)) {
                    sessionInformation.expireNow();
                }
            }
        }
    }
}
