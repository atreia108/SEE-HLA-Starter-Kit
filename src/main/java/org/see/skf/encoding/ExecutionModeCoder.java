package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAinteger16LE;
import org.see.skf.core.ExecutionConfiguration;

public final class ExecutionModeCoder implements Coder<ExecutionConfiguration.ExecutionMode> {

    private final HLAinteger16LE executionModeType;

    public ExecutionModeCoder(EncoderFactory encoderFactory) {
        this.executionModeType = encoderFactory.createHLAinteger16LE();
    }

    @Override
    public byte[] encode(ExecutionConfiguration.ExecutionMode data) {
        this.executionModeType.setValue(data.getValue());
        return this.executionModeType.toByteArray();
    }

    @Override
    public ExecutionConfiguration.ExecutionMode decode(byte[] data) throws DecoderException {
        this.executionModeType.decode(data);
        short value = this.executionModeType.getValue();

        return ExecutionConfiguration.ExecutionMode.query(value);
    }
}
