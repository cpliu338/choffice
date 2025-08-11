package org.therismos.codec;

import java.time.YearMonth;
import org.bson.*;
import org.bson.codecs.*;

/**
 *
 * @author cp_liu
 */
public class YearMonthCodec implements Codec<YearMonth> {

    @Override
    public void encode(BsonWriter writer, YearMonth value, EncoderContext encoderContext) {
        // store as a string "YYYY-MM"
        writer.writeString(value.toString());
    }

    @Override
    public YearMonth decode(BsonReader reader, DecoderContext decoderContext) {
        String s = reader.readString();
        return YearMonth.parse(s); // expects "YYYY-MM"
    }

    @Override
    public Class<YearMonth> getEncoderClass() {
        return YearMonth.class;
    }
}
