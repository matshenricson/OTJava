package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;

import java.util.Arrays;

/**
 * Placeholder for attestations that don't support
 *
 * @see TimeAttestation
 */
public class UnknownAttestation extends TimeAttestation {

    byte[] payload;
    public static byte[] _TAG = new byte[]{};     // TODO: Static ???

    @Override
    public byte[] _TAG() {
        return _TAG;
    }

    UnknownAttestation(byte[] tag, byte[] payload) {
        super();
        this._TAG = tag;
        this.payload = payload;
    }

    @Override
    public void serializePayload(StreamSerializationContext ctx) {
        ctx.writeBytes(this.payload);
    }

    public static UnknownAttestation deserialize(StreamDeserializationContext ctxPayload, byte[] tag) {
        byte[] payload = ctxPayload.readVarbytes(_MAX_PAYLOAD_SIZE);

        return new UnknownAttestation(tag, payload);
    }

    public String toString() {
        return "UnknownAttestation " + Utils.bytesToHex(this._TAG()) + ' ' + Utils.bytesToHex(this.payload);
    }

    @Override
    public int compareTo(TimeAttestation other) {
        if (!(other instanceof UnknownAttestation)) {
            return this.getClass().getName().compareTo(other.getClass().getName());   // This makes the comparison symmetric
        }

        UnknownAttestation that = (UnknownAttestation) other;

        return Utils.compare(this.payload, that.payload);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UnknownAttestation)) {
            return false;
        }

        UnknownAttestation that = (UnknownAttestation) other;

        if (!Arrays.equals(this._TAG(), that._TAG())) {
            return false;
        }

        return Arrays.equals(this.payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this._TAG()) ^ Arrays.hashCode(this.payload);
    }
}
