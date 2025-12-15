package org.therismos.job;

import java.util.UUID;
import org.bson.Document;

/**
 * Suggested by Open AI, best practice for async job management
 * @author cp_liu
 */
public class JobInfo {
    public enum Status {
        RUNNING, SUCCESS, FAILED, EXPIRED
    }

    private final UUID id;
    private final String type;
    private final long expiresAt;

    private volatile Status status = Status.RUNNING;
    private volatile Document result;
    private volatile Throwable error;

    JobInfo(UUID id, String type, long expiresAt) {
        this.id = id;
        this.type = type;
        this.expiresAt = expiresAt;
    }

    void markSuccess(Document result) {
        this.result = result;
        this.status = Status.SUCCESS;
    }

    void markFailure(Throwable error) {
        this.error = error;
        this.status = Status.FAILED;
    }

    void markExpired() {
        this.status = Status.EXPIRED;
    }

    // public getters only
    public UUID getId() { return id; }
    public String getType() { return type; }
    public long getExpiresAt() { return expiresAt; }
    public Status getStatus() { return status; }
    public Document getResult() { return result; }
    public Throwable getError() { return error; }
}
