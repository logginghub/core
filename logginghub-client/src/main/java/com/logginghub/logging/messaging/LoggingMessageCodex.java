package com.logginghub.logging.messaging;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventCollection;
import com.logginghub.logging.api.levelsetting.InstanceFilter;
import com.logginghub.logging.api.levelsetting.LevelSetting;
import com.logginghub.logging.api.levelsetting.LevelSettingsConfirmation;
import com.logginghub.logging.api.levelsetting.LevelSettingsGroup;
import com.logginghub.logging.api.levelsetting.LevelSettingsRequest;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.AggregationListRequest;
import com.logginghub.logging.api.patterns.AggregationListResponse;
import com.logginghub.logging.api.patterns.InstanceDetails;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.api.patterns.PatternListRequest;
import com.logginghub.logging.api.patterns.PatternListResponse;
import com.logginghub.logging.api.patterns.PingRequest;
import com.logginghub.logging.api.patterns.PingResponse;
import com.logginghub.logging.messages.BaseRequestResponseMessage;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.ChannelSubscriptionRequestMessage;
import com.logginghub.logging.messages.ChannelSubscriptionResponseMessage;
import com.logginghub.logging.messages.ClearEventsMessage;
import com.logginghub.logging.messages.CompressedBlock;
import com.logginghub.logging.messages.ConnectedMessage;
import com.logginghub.logging.messages.ConnectionTypeMessage;
import com.logginghub.logging.messages.EventSubscriptionRequestMessage;
import com.logginghub.logging.messages.EventSubscriptionResponseMessage;
import com.logginghub.logging.messages.FilterRequestMessage;
import com.logginghub.logging.messages.HealthCheckRequest;
import com.logginghub.logging.messages.HealthCheckResponse;
import com.logginghub.logging.messages.HistoricalAggregatedDataRequest;
import com.logginghub.logging.messages.HistoricalAggregatedDataResponse;
import com.logginghub.logging.messages.HistoricalDataJobKillRequest;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexRequest;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.messages.HistoricalPatternisedDataRequest;
import com.logginghub.logging.messages.HistoricalPatternisedDataResponse;
import com.logginghub.logging.messages.HistoricalStackDataJobKillRequest;
import com.logginghub.logging.messages.HistoricalStackDataRequest;
import com.logginghub.logging.messages.HistoricalStackDataResponse;
import com.logginghub.logging.messages.InstanceKey;
import com.logginghub.logging.messages.LogEventCollectionMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.MapMessage;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.logging.messages.ReportDetails;
import com.logginghub.logging.messages.ReportExecuteRequest;
import com.logginghub.logging.messages.ReportExecuteResponse;
import com.logginghub.logging.messages.ReportExecuteResult;
import com.logginghub.logging.messages.ReportListRequest;
import com.logginghub.logging.messages.ReportListResponse;
import com.logginghub.logging.messages.ResponseMessage;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.messages.StackTrace;
import com.logginghub.logging.messages.StackTraceItem;
import com.logginghub.logging.messages.SubscriptionRequestMessage;
import com.logginghub.logging.messages.SubscriptionResponseMessage;
import com.logginghub.logging.messages.UnsubscriptionRequestMessage;
import com.logginghub.logging.messages.UnsubscriptionResponseMessage;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.HexDump;
import com.logginghub.utils.LazyReference;
import com.logginghub.utils.Result;
import com.logginghub.utils.data.DataElement;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofExpandingBufferSerialiser;
import com.logginghub.utils.sof.SofPartialDecodeException;
import com.logginghub.utils.sof.SofUnknownTypeException;

import java.io.EOFException;
import java.io.Serializable;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.EnumSet;

public class LoggingMessageCodex {

    private static final Logger logger = Logger.getLoggerFor(LoggingMessageCodex.class);
    public final static byte LogEvent = 1;
    public final static byte LogEventCollection = 2;
    // public final static byte LogTelemetry = 3;
    // public final static byte SubscribeRequest = 4;
    // public final static byte SubscribeResponse = 5;
    // public final static byte UnsubscribeRequest = 6;
    // public final static byte UnsubscribeResponse = 7;

    public final static byte serialisableObject = 98;

    public final static byte JavaSerialised = 99;

    public final static byte Encrypted = 100;
    public final static byte Compressed = 101;

    public final static byte ExtendedType = -127;

    private boolean logUnknownSofTypes = !Boolean.getBoolean("loggingMessageCodex.suppressLoggingOfUnknownSofTypes");

    public enum Flags {
        Encrypted,
        Compressed
    }

    ;

    private CompressingCodex compressingCodex = new CompressingCodex();
    private LazyReference<EncryptingCodex> encryptingCodex = new LazyReference<EncryptingCodex>() {
        @Override protected EncryptingCodex instantiate() {
            return new EncryptingCodex();
        }
    };

    // jshaw - this is very important, dont change the IDs!!
    private SofConfiguration sofConfiguration = new SofConfiguration() {
        {
            // registerType(AggregatedPatternDataMessage.class, 1);
            // registerType(AggregatedPatternDataSubscriptionRequestMessage.class, 2);
            // registerType(AggregatedPatternDataSubscriptionResponseMessage.class, 3);
            // registerType(AggregationKey.class, 4);

            registerType(HistoricalIndexElement.class, 5);
            registerType(HistoricalIndexRequest.class, 6);
            registerType(HistoricalIndexResponse.class, 7);

            registerType(DefaultLogEvent.class, 8);
            registerType(HistoricalDataRequest.class, 9);
            registerType(HistoricalDataResponse.class, 10);

            registerType(CompressedBlock.class, 11);
            registerType(ChannelMessage.class, 12);

            registerType(LogEventMessage.class, 13);

            registerType(ChannelSubscriptionRequestMessage.class, 14);
            registerType(ChannelSubscriptionResponseMessage.class, 15);

            registerType(EventSubscriptionRequestMessage.class, 16);
            registerType(EventSubscriptionResponseMessage.class, 17);

            registerType(StackSnapshot.class, 18);
            registerType(StackTrace.class, 19);
            registerType(StackTraceItem.class, 20);
            registerType(StackStrobeRequest.class, 21);

            registerType(DataStructure.class, 22);
            registerType(DataElement.class, 23);

            // registerType(AggregatedPatternData.class, 24);

            registerType(SubscriptionRequestMessage.class, 25);
            registerType(SubscriptionResponseMessage.class, 26);

            registerType(UnsubscriptionRequestMessage.class, 27);
            registerType(UnsubscriptionResponseMessage.class, 28);

            registerType(FilterRequestMessage.class, 29);

            registerType(ResponseMessage.class, 31);

            registerType(PatternListRequest.class, 32);
            registerType(PatternListResponse.class, 33);

            registerType(MapMessage.class, 34);

            registerType(Pattern.class, 35);

            registerType(PingRequest.class, 36);
            registerType(PingResponse.class, 37);

            registerType(LevelSettingsConfirmation.class, 38);
            registerType(LevelSettingsRequest.class, 39);
            registerType(InstanceFilter.class, 40);
            registerType(LevelSettingsGroup.class, 41);
            registerType(LevelSetting.class, 42);
            registerType(InstanceDetails.class, 43);

            registerType(ConnectedMessage.class, 44);
            registerType(Result.class, 45);

            registerType(PatternisedLogEvent.class, 46);
            registerType(AggregatedLogEvent.class, 47);

            registerType(AggregationListRequest.class, 48);
            registerType(AggregationListResponse.class, 49);

            registerType(Aggregation.class, 50);

            registerType(HistoricalPatternisedDataRequest.class, 51);
            registerType(HistoricalPatternisedDataResponse.class, 52);

            registerType(HistoricalAggregatedDataRequest.class, 53);
            registerType(HistoricalAggregatedDataResponse.class, 54);

            registerType(HealthCheckRequest.class, 55);
            registerType(HealthCheckResponse.class, 56);

            registerType(ConnectionTypeMessage.class, 57);

            registerType(HistoricalDataJobKillRequest.class, 58);
            registerType(BaseRequestResponseMessage.class, 59);

            registerType(HistoricalStackDataRequest.class, 60);
            registerType(HistoricalStackDataResponse.class, 61);
            registerType(HistoricalStackDataJobKillRequest.class, 62);

            registerType(ReportListResponse.class, 63);
            registerType(ReportListRequest.class, 64);
            registerType(ReportDetails.class, 65);

            registerType(ReportExecuteRequest.class, 66);
            registerType(ReportExecuteResponse.class, 67);
            registerType(ReportExecuteResult.class, 68);

            registerType(InstanceKey.class, 69);

            registerType(ClearEventsMessage.class, 70);

        }
    };

    public LoggingMessage decode(ByteBuffer buffer) throws PartialMessageException {
        int type = buffer.get();

        logger.finest("Attempting to decode message with type '{}'", type);

        buffer.mark();
        try {
            switch (type) {
                case LogEvent: {
                    try {
                        LogEvent logEvent = LogEventCodex.decode(buffer);
                        LogEventMessage message = new LogEventMessage(logEvent);
                        return message;
                    } catch (RuntimeException t) {
                        int position = buffer.position();
                        buffer.reset();
                        logger.severe(t, "Log event decode failed at position '{}' : {}", position, t.getMessage());
                        if (buffer.remaining() < ByteUtils.kilobytes(100)) {
                            logger.severe("Log event buffer was : {}", HexDump.format(buffer));
                        } else {
                            logger.fine("Log event buffer was : {}", HexDump.format(buffer));
                        }
                        throw t;
                    }
                }
                // case LogTelemetry: {
                // try {
                // TelemetryStack telemetryStack = LogTelemetryCodex.decode(buffer);
                // LogTelemetryMessage message = new LogTelemetryMessage(telemetryStack);
                // return message;
                // }
                // catch (RuntimeException t) {
                // buffer.reset();
                // logger.severe("Log telemetry decode fail : {}", HexDump.format(buffer));
                // throw t;
                // }
                // }
                case LogEventCollection: {
                    LogEventCollection logEventCollection = LogEventCollectionCodex.decode(buffer);
                    LogEventCollectionMessage message = new LogEventCollectionMessage(logEventCollection);
                    return message;
                }
                // case SubscribeRequest: {
                // NewSubscriptionRequestMessage message = new
                // NewSubscriptionRequestMessage(LogEventCodex.decodeStringArray(buffer));
                // return message;
                // }
                // case SubscribeResponse: {
                // NewSubscriptionResponseMessage message = new
                // NewSubscriptionResponseMessage(LogEventCodex.decodeStringArray(buffer));
                // return message;
                // }
                // case UnsubscribeRequest: {
                // NewUnsubscribeRequestMessage message = new
                // NewUnsubscribeRequestMessage(LogEventCodex.decodeStringArray(buffer));
                // return message;
                // }
                // case UnsubscribeResponse: {
                // NewUnsubscribeResponseMessage message = new
                // NewUnsubscribeResponseMessage(LogEventCodex.decodeStringArray(buffer));
                // return message;
                // }
                case Encrypted: {
                    ByteBuffer decrypted = encryptingCodex.get().decrypt(buffer);
                    return decode(decrypted);
                }
                case Compressed: {
                    ByteBuffer uncompressed = compressingCodex.uncompress(buffer);
                    return decode(uncompressed);
                }

                case JavaSerialised: {
                    try {
                        LoggingMessage decode = JavaSerialisationCodex.decode(buffer);
                        return decode;
                    } catch (Exception e) {
                        // Someone has sent us something we can't interpret,
                        // ignore it
                        return null;
                    }
                }
                case serialisableObject: {
                    SerialisableObject message = decodeSof(buffer);
                    return (LoggingMessage) message;
                }
                default: {
                    throw new RuntimeException("Message type " + type + " isn't recognised");
                }
            }
        } catch (BufferUnderflowException bue) {
            // This is ok, it just means the entire event isn't in the buffer
            // yet.
            buffer.reset();
            throw new PartialMessageException();
        }
    }

    public SerialisableObject decodeSof(ByteBuffer buffer) throws PartialMessageException {
        SerialisableObject message = null;
        try {
            message = SofExpandingBufferSerialiser.read(buffer, sofConfiguration);
        }
        // jshaw - temp catch to debug a sof issue
        // catch(BufferUnderflowException e) {
        // e.printStackTrace();
        // throw new RuntimeException(e);
        // }
        catch (SofUnknownTypeException e) {
            // We've been sent an unknown type - for backwards compatibility reasons this might not
            // be a bad thing
            if (logUnknownSofTypes) {
                logger.warning("Recieved unknown sof message type - header details {}", e.getHeaderSummary());
            }
        } catch (SofPartialDecodeException e) {
            throw new PartialMessageException();
        } catch (SofException e) {
            throw new FormattedRuntimeException(e, "Failed to decode SOF serialisable object");
        } catch (EOFException e) {
            throw new FormattedRuntimeException(e, "End of file reported from sof serialiser");
        }
        return message;
    }

    public void encode(ExpandingByteBuffer expandingByteBuffer, LoggingMessage message) {
        if (message instanceof LogEventMessage) {
            LogEventMessage logEventMessage = (LogEventMessage) message;
            encode(expandingByteBuffer, logEventMessage.getLogEvent());
        } else if (message instanceof LogEventCollectionMessage) {
            LogEventCollectionMessage logEventCollectionMessage = (LogEventCollectionMessage) message;
            encode(expandingByteBuffer, logEventCollectionMessage.getLogEventCollection());
        }
        // else if (message instanceof LogTelemetryMessage) {
        // LogTelemetryMessage telemetryMessage = (LogTelemetryMessage) message;
        // encode(expandingByteBuffer, telemetryMessage.getTelemetryStack());
        // }
        // else if (message instanceof NewSubscriptionRequestMessage) {
        // NewSubscriptionRequestMessage actualMessage = (NewSubscriptionRequestMessage) message;
        // encode(expandingByteBuffer, actualMessage);
        // }
        // else if (message instanceof NewSubscriptionResponseMessage) {
        // NewSubscriptionResponseMessage actualMessage = (NewSubscriptionResponseMessage) message;
        // encode(expandingByteBuffer, actualMessage);
        // }
        // else if (message instanceof NewUnsubscribeResponseMessage) {
        // NewUnsubscribeResponseMessage actualMessage = (NewUnsubscribeResponseMessage) message;
        // encode(expandingByteBuffer, actualMessage);
        // }
        // else if (message instanceof NewUnsubscribeRequestMessage) {
        // NewUnsubscribeRequestMessage actualMessage = (NewUnsubscribeRequestMessage) message;
        // encode(expandingByteBuffer, actualMessage);
        // }
        // else if(message instanceof SerialisableObjectWrapper){
        // SerialisableObjectWrapper serialisableObjectWrapper = (SerialisableObjectWrapper)
        // message;
        // SerialisableObject serialisableObject =
        // serialisableObjectWrapper.getSerialisableObject();
        // encode(expandingByteBuffer, serialisableObject);
        // }
        else if (message instanceof SerialisableObject) {
            SerialisableObject serialisableObject = (SerialisableObject) message;
            try {
                encode(expandingByteBuffer, serialisableObject);
            } catch (RuntimeException t) {
                logger.warn(t, "Potential sof encoding issue for message '{}'", message);
                throw t;
            }
        } else if (message instanceof Serializable) {
            expandingByteBuffer.put(LoggingMessageCodex.JavaSerialised);
            JavaSerialisationCodex.encode(expandingByteBuffer, message);
        } else {
            throw new IllegalArgumentException("Dont know how to encode " + message);
        }
    }

    private void encode(ExpandingByteBuffer expandingByteBuffer, SerialisableObject object) {
        expandingByteBuffer.put(LoggingMessageCodex.serialisableObject);

        try {
            SofExpandingBufferSerialiser.write(expandingByteBuffer, object, sofConfiguration);
        } catch (SofException e) {
            throw new FormattedRuntimeException("Failed to encode SOF serialisable object", e);
        }
    }

    // public void encode(ExpandingByteBuffer expandingBuffer, NewSubscriptionRequestMessage
    // message) {
    // expandingBuffer.put(LoggingMessageCodex.SubscribeRequest);
    // LogEventCodex.encodeStringArray(expandingBuffer, message.getChannels());
    // }
    //
    // public void encode(ExpandingByteBuffer expandingBuffer, NewSubscriptionResponseMessage
    // message) {
    // expandingBuffer.put(LoggingMessageCodex.SubscribeResponse);
    // LogEventCodex.encodeStringArray(expandingBuffer, message.getChannels());
    // }

    // public void encode(ExpandingByteBuffer expandingBuffer, NewUnsubscribeRequestMessage message)
    // {
    // expandingBuffer.put(LoggingMessageCodex.SubscribeRequest);
    // LogEventCodex.encodeStringArray(expandingBuffer, message.getChannels());
    // }
    //
    // public void encode(ExpandingByteBuffer expandingBuffer, NewUnsubscribeResponseMessage
    // message) {
    // expandingBuffer.put(LoggingMessageCodex.SubscribeResponse);
    // LogEventCodex.encodeStringArray(expandingBuffer, message.getChannels());
    // }

    public void encode(ExpandingByteBuffer expandingBuffer, LogEvent logEvent) {
        encode(expandingBuffer, logEvent, EnumSet.noneOf(Flags.class));
    }

    // public void encode(ExpandingByteBuffer expandingBuffer, TelemetryStack telemetryStack) {
    // encode(expandingBuffer, telemetryStack, EnumSet.noneOf(Flags.class));
    // }

    public void encode(ExpandingByteBuffer expandingBuffer, LogEvent logEvent, EnumSet<Flags> flags) {
        logger.finest("Encoding event into buffer '{}'", expandingBuffer);
        int position = expandingBuffer.position();
        expandingBuffer.put(LoggingMessageCodex.LogEvent);

        logger.finest("Type byte written, buffer is now '{}'", expandingBuffer);

        LogEventCodex.encode(expandingBuffer, logEvent);

        logger.finest("Log event written, buffer is now '{}'", expandingBuffer);

        processFlags(expandingBuffer, flags, position);

        logger.finest("Flag processing complete, buffer is now '{}'", expandingBuffer);
    }

    // public void encode(ExpandingByteBuffer expandingBuffer, TelemetryStack telemetryStack,
    // EnumSet<Flags> flags) {
    // logger.finest("Encoding event into buffer '{}'", expandingBuffer);
    // int position = expandingBuffer.position();
    // expandingBuffer.put(LoggingMessageCodex.LogTelemetry);
    //
    // logger.finest("Type byte written, buffer is now '{}'", expandingBuffer);
    //
    // LogTelemetryCodex.encode(expandingBuffer, telemetryStack);
    //
    // logger.finest("Log event written, buffer is now '{}'", expandingBuffer);
    //
    // processFlags(expandingBuffer, flags, position);
    //
    // logger.finest("Flag processing complete, buffer is now '{}'", expandingBuffer);
    // }

    public void encode(ExpandingByteBuffer expandingBuffer, LogEventCollection logEventCollection) {
        encode(expandingBuffer, logEventCollection, EnumSet.noneOf(Flags.class));
    }

    public void encode(ExpandingByteBuffer expandingBuffer, LogEventCollection collection, EnumSet<Flags> flags) {
        int position = expandingBuffer.getBuffer().position();
        expandingBuffer.put(LoggingMessageCodex.LogEventCollection);
        LogEventCollectionCodex.encode(expandingBuffer, collection);

        processFlags(expandingBuffer, flags, position);
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    private void processFlags(ExpandingByteBuffer expandingBuffer, EnumSet<Flags> flags, int position) {
        if (flags.contains(Flags.Compressed)) {
            expandingBuffer.insertByte(position, (byte) LoggingMessageCodex.Compressed);
            compressingCodex.compress(expandingBuffer, position + 1);

            // The compression will hopefully make the message shorter =)
            // position = expandingBuffer.getBuffer().position();
        }

        if (flags.contains(Flags.Encrypted)) {
            expandingBuffer.insertByte(position, (byte) LoggingMessageCodex.Encrypted);
            encryptingCodex.get().encrypt(expandingBuffer, position + 1);

            // The encryption will probably make the message a bit longer
            // position = expandingBuffer.getBuffer().position();
        }

    }

    public SofConfiguration getSofConfiguration() {
        return sofConfiguration;
    }

    public void setLogUnknownSofTypes(boolean logUnknownSofTypes) {
        this.logUnknownSofTypes = logUnknownSofTypes;
    }

    public boolean isLogUnknownSofTypes() {
        return logUnknownSofTypes;
    }
}
