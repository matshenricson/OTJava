package com.eternitywall.ots.op;

import com.eternitywall.ots.Utils;

import java.util.Arrays;

/**
 * Append a suffix to a message.
 *
 * @see OpBinary
 */
public class OpAppend extends OpBinary {

    public static final byte _TAG = (byte) 0xf0;

    @Override
    public byte _TAG() {
        return OpAppend._TAG;
    }

    @Override
    public String _TAG_NAME() {
        return "append";
    }

    public OpAppend() {
        super();
    }

    public OpAppend(byte[] arg) {
        super(arg);
    }

    @Override
    public byte[] call(byte[] msg) {
        return Utils.arraysConcat(msg, this.arg);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof OpAppend)) {
            return false;
        }

        return Arrays.equals(this.arg, ((OpAppend) other).arg);
    }
}
