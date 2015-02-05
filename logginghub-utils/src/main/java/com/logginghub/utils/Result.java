package com.logginghub.utils;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class Result<T> implements SerialisableObject {

    public enum State {
        Successful,
        Unsuccessful,
        Failed,
        Timedout
    }

    private State state = State.Successful;
    private T value;

    private String internalReason;
    private String externalReason;

    public Result(T instance) {
        value = instance;
        state = State.Successful;

    }

    public Result() {}

    public Result(State state, Throwable e) {
        this.state = state;
        this.value = null;
        this.externalReason = e.getMessage();
        this.internalReason = StacktraceUtils.getStackTraceAsString(e);
    }

    public static <T> Result<T> unsuccessful(String message, Object... params) {
        Result<T> result = new Result<T>();
        String formatted = StringUtils.format(message, params);
        result.setInternalReason(formatted);
        result.setExternalReason(formatted);
        result.setState(State.Unsuccessful);
        result.setValue(null);
        return result;
    }

    public static <T> Result<T> failed(Throwable t, String message, Object... params) {
        Result<T> result = new Result<T>();
        String formatted = StringUtils.format(message, params);
        result.internalReason = StacktraceUtils.getStackTraceAsString(t);
        result.setExternalReason(formatted);
        result.setState(State.Failed);
        result.setValue(null);
        return result;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public T getValue() {
        return value;
    }

    public String getExternalReason() {
        return externalReason;
    }

    public String getInternalReason() {
        return internalReason;
    }

    public void setExternalReason(String externalReason, Object... params) {
        this.externalReason = StringUtils.format(externalReason, params);
    }

    public void setInternalReason(String internalReason, Object... params) {
        this.internalReason = StringUtils.format(internalReason, params);
    }

    @Override public String toString() {
        return "Result [state=" + state + ", value=" + value + ", internalReason=" + internalReason + ", externalReason=" + externalReason + "]";
    }

    public boolean isSuccessful() {
        return state == State.Successful;
    }

    public void fail(String externalReason, String internalReason) {
        this.state = State.Failed;
        this.externalReason = externalReason;
        this.internalReason = internalReason;
    }

    public void setUnsuccessfulReasons(String externalReason, String internalReason) {
        this.state = State.Unsuccessful;
        this.externalReason = externalReason;
        this.internalReason = internalReason;
    }

    public void unsuccessful(String reason) {
        unsuccessful(reason, reason);
    }

    public void failFormat(String string, Object... params) {
        String formatted = StringUtils.format(string, params);
        fail(formatted, formatted);
    }

    public void timedOut() {
        this.state = State.Timedout;
    }

    public void success(T result) {
        this.state = State.Successful;
        this.value = result;
    }

    public void failed(Throwable t) {
        this.state = State.Failed;
        this.value = null;
        this.externalReason = t.getMessage();
        this.internalReason = StacktraceUtils.getStackTraceAsString(t);
    }

    public void failed(String reason) {
        this.state = State.Failed;
        this.value = null;
        this.externalReason = reason;
        this.internalReason = reason;
    }

    public boolean isUnsuccessful() {
        return state == State.Unsuccessful;
    }

    public boolean isTimeout() {
        return state == State.Timedout;
    }

    public boolean isFailure() {
        return state == State.Failed;
    }

    @SuppressWarnings("unchecked") @Override public void read(SofReader reader) throws SofException {
        this.externalReason = reader.readString(0);
        this.internalReason = reader.readString(1);
        this.state = State.valueOf(reader.readString(2));
        this.value = (T) reader.readObject(3);
    }

    @Override public void write(SofWriter writer) throws SofException {
        if (value != null) {
            Is.instanceOf(value, SerialisableObject.class, "Unable to encode a Result object if the payload does not implement SerialisableObject. Value class is '{}'", value.getClass().getName());
        }

        writer.write(0, externalReason);
        writer.write(1, internalReason);
        writer.write(2, state.toString());
        writer.write(3, (SerialisableObject) value);
    }

    public static <T> Result<T> successful(T t) {
        return new Result<T>(t);
    }


}
