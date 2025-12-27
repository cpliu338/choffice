package org.therismos.job;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

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

    public JobInfo(UUID id, String type, long expiresAt) {
        this.id = id;
        this.type = type;
        this.expiresAt = expiresAt;
    }

    public void markSuccess(Document result) {
        this.result = result;
        if (result.containsKey("download-path")) {
            this.result.put("filename", new File(result.getString("download-path")).getName());
            this.result.remove("download-path");
        }
        this.status = Status.SUCCESS;
    }

    public void markFailure(Throwable error) {
        this.error = error;
        this.status = Status.FAILED;
    }

    public void markExpired() {
        this.status = Status.EXPIRED;
    }

    // public getters only
    public UUID getId() { return id; }
    public String getType() { return type; }
    public long getExpiresAt() { return expiresAt; }
    public Status getStatus() { return status; }
    public Document getResult() { return result; }
    public Throwable getError() { return error; }
    
    public String toJson() {
        Document d = new Document("id", id.toString());
        d.append("type", type);
        d.append("status", status.toString());
        d.append("error", error == null ? "null" : error.getMessage());
        if (error != null) {
            if (error.getCause() == null) {
                d.append("exception-class", error.getClass().getName());
            }
            else {
                Throwable cause = error.getCause();
                d.append("exception-class", cause.getClass().getName());
                d.append("exception-message", cause.getMessage());
            }
        }
        d.append("result", result);
        d.append("expires", java.time.Instant.ofEpochMilli(expiresAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        Encoder encoder = new Encoder<Document>(){
            @Override
            public void encode(BsonWriter writer, Document document, EncoderContext encoderContext) {
                writer.writeStartDocument();
                for (Map.Entry<String, Object> entry : document.entrySet()) {
                    writer.writeName(entry.getKey());
                    writeValue(writer, entry.getValue());
                }
                writer.writeEndDocument();
            }

            private void writeValue(BsonWriter writer, Object value) {
                if (value == null) {
                    writer.writeNull();
                } else if (value instanceof String) {
                    writer.writeString((String) value);
                } else if (value instanceof Integer) {
                    writer.writeInt32((Integer) value);
                } else if (value instanceof Long) {
                    writer.writeInt64((Long) value);
                } else if (value instanceof Boolean) {
                    writer.writeBoolean((Boolean) value);
                } else if (value instanceof Double) {
                    writer.writeDouble((Double) value);
                } else if (value instanceof ObjectId) {
                    writer.writeObjectId((ObjectId) value);
                } else if (value instanceof LocalDate) {
                    writer.writeString(((LocalDate) value).toString()); // ISO-8601 format
                } else if (value instanceof YearMonth) {
                    writer.writeString(((YearMonth) value).toString()); // ISO-8601 format
                } else if (value instanceof Map) {
                    writer.writeStartDocument();
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                        writer.writeName(entry.getKey().toString());
                        writeValue(writer, entry.getValue()); // Recursive call
                    }
                    writer.writeEndDocument();
                } else if (value instanceof Iterable) {
                    writer.writeStartArray();
                    for (Object item : (Iterable<?>) value) {
                        writeValue(writer, item); // Recursive call for nested items
                    }
                    writer.writeEndArray();
               } else if (value instanceof BigDecimal) {
                    writer.writeDecimal128(new Decimal128((BigDecimal) value));
                } else {
                    throw new CodecConfigurationException("Unsupported type: " + value.getClass());
                }
            }

            @Override
            public Class<Document> getEncoderClass() {
                return Document.class;
            }
        };
        return d.toJson(encoder);
    }
    
}
