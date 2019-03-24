package com.eternitywall.ots.op;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.Utils;

import java.util.Arrays;

/**
 * Prepend a prefix to a message.
 *
 * @see OpBinary
 */
public class OpPrepend extends OpBinary {

    public static final byte _TAG = (byte) 0xf1;

    byte[] arg;

    @Override
    public byte _TAG() {
        return OpPrepend._TAG;
    }

    @Override
    public String _TAG_NAME() {
        return "prepend";
    }

    public OpPrepend() {
        super();
        this.arg = new byte[]{};
    }

    public OpPrepend(byte[] arg) {
        super(arg);
        this.arg = arg;
    }

    @Override
    public byte[] call(byte[] msg) {
        return Utils.arraysConcat(this.arg, msg);
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        return OpBinary.deserializeFromTag(ctx, tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OpPrepend)) {
            return false;
        }

        return Arrays.equals(this.arg, ((OpPrepend) obj).arg);
    }

    @Override
    public int hashCode() {
        return _TAG ^ Arrays.hashCode(this.arg);
    }
}
