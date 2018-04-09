package com.videoweber.lib.app.service.hibernate_service.session_provider;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;
import org.hibernate.Session;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class SessionProvider {

    private final Supplier<Session> sessionSupplier;
    private Session session = null;
    private final ArrayList<SessionAcquirement> acquirements = new ArrayList<>();

    public SessionProvider(Supplier<Session> sessionSupplier) {
        Objects.requireNonNull(sessionSupplier);
        this.sessionSupplier = sessionSupplier;
    }

    public SessionAcquirement acquireSession() {
        if (session == null) {
            session = sessionSupplier.get();
        }
        SessionAcquirement acquirement = new SessionAcquirement(session, this);
        acquirements.add(acquirement);
        return acquirement;
    }

    void releaseSession(SessionAcquirement acquirement) {
        if (acquirements.isEmpty()) {
            throw new RuntimeException("No acquired sessions.");
        }
        SessionAcquirement lastAcquirement = acquirements.get(acquirements.size() - 1);
        if (!lastAcquirement.equals(acquirement)) {
            throw new RuntimeException("Unexpected acquirement to release.");
        }
        if (acquirements.size() == 1) {
            session.close();
            session = null;
        }
        acquirements.remove(acquirement);
    }

}
