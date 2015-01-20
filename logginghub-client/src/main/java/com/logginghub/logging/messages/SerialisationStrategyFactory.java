package com.logginghub.logging.messages;

import com.logginghub.utils.FormattedRuntimeException;


public class SerialisationStrategyFactory {

    public final static byte serialisation_java = 0;   
    public final static byte serialisation_sof_headers_and_fields = 1;
    public final static byte serialisation_sof_headers_and_no_fields = 2;
    public final static byte serialisation_sof_no_headers_and_fields = 3;
    public final static byte serialisation_sof_no_headers_and_no_fields = 4;
    public final static byte serialisation_kryo = 5;

    public static SerialisationStrategy createStrategy(byte strategy) {
        switch (strategy) {
            case serialisation_java: return new JavaSerialisationStrategy();
            case serialisation_sof_headers_and_fields : return new SofSerialisationStrategy(true, false);
            case serialisation_sof_headers_and_no_fields : return new SofSerialisationStrategy(true, true);
            case serialisation_sof_no_headers_and_fields : return new SofSerialisationStrategy(false, false);
            case serialisation_sof_no_headers_and_no_fields : return new SofSerialisationStrategy(false, true);
            case serialisation_kryo: return new KryoSerialisationStrategy();
            default:
                throw new FormattedRuntimeException("We dont recognise serialisation strategy '{}'", strategy);
        }
    }
    
}
