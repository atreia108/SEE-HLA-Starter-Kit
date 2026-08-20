package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAinteger16LE;
import org.see.skf.core.ModeTransitionRequest;

public final class MTRModeCoder implements Coder<ModeTransitionRequest.MTRMode> {

    private final HLAinteger16LE executionModeType;

    public MTRModeCoder(EncoderFactory encoderFactory) {
        this.executionModeType = encoderFactory.createHLAinteger16LE();
    }

    @Override
    public ModeTransitionRequest.MTRMode decode(byte[] buffer) throws DecoderException {
        this.executionModeType.decode(buffer);
        return ModeTransitionRequest.MTRMode.query(this.executionModeType.getValue());
    }

    @Override
    public byte[] encode(ModeTransitionRequest.MTRMode element) {
        this.executionModeType.setValue(element.getValue());
        return this.executionModeType.toByteArray();
    }
}
