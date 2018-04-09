package com.videoweber.lib.app.service.hibernate_service.session_provider;

import java.util.Objects;
import org.hibernate.Session;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class SessionAcquirement {

    private final SessionProvider provider;
    private final Session session;

    SessionAcquirement(Session session, SessionProvider provider) {
        Objects.requireNonNull(session);
        Objects.requireNonNull(provider);
        this.session = session;
        this.provider = provider;
    }

    public Session getSession() {
        return session;
    }
    
    public void release() {
        provider.releaseSession(this);
    }

    public SessionProvider getProvider() {
        return provider;
    }

}
