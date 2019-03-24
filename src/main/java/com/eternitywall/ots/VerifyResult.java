package com.eternitywall.ots;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Class that lets us compare, sort, store and print timestamps.
 */
public class VerifyResult implements Comparable<VerifyResult> {
    private static final String DATE_PATTERN = "YYYY-MM-dd z";
    private static final DateFormatSymbols DATE_FORMAT_SYMBOLS = new DateFormatSymbols(new Locale("en", "UK"));
    public static enum Chains {
        BITCOIN, LITECOIN, ETHEREUM
    }

    public Long timestamp;
    public int height;

    public VerifyResult(Long timestamp, int height) {
        this.timestamp = timestamp;
        this.height = height;
    }

    /**
     * Returns, if existing, a string representation describing the existence of a block attest
     */
    public String toString() {
        if (height == 0 || timestamp == null) {
            return "";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN, DATE_FORMAT_SYMBOLS);
        String string = simpleDateFormat.format(new Date(timestamp * 1000));

        return "block " + String.valueOf(height) + " attests data existed as of " + string;
    }

    @Override
    public int compareTo(VerifyResult vr) {
        return this.height - vr.height;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VerifyResult)) {
            return false;
        }

        VerifyResult that = (VerifyResult) other;

        return Objects.equals(this.timestamp, that.timestamp) && this.height == that.height;
    }

    @Override
    public int hashCode() {
        return ((int) (long) (this.timestamp)) ^ this.height;
    }
}
