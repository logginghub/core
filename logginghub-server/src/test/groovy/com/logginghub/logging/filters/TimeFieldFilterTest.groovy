package com.logginghub.logging.filters
import com.logginghub.logging.LogEvent
import spock.lang.Specification

import static com.logginghub.logging.LogEventBuilder.logEvent
/**
 * Created by james on 27/01/2016.
 */
class TimeFieldFilterTest extends Specification {

    def "test regular fields" (TimeFieldFilter.Field field, TimeFieldFilter.Type type, long value, LogEvent input, boolean expected) {

        when:
        TimeFieldFilter filter = new TimeFieldFilter(field, type, value);

        then:
        filter.passes(input) == expected;

        where:

        field                                | type                                     | value | input                               | expected

        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().hubTime(1).toLogEvent()  | false
        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().hubTime(10).toLogEvent() | false
        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().hubTime(11).toLogEvent() | true

        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().hubTime(1).toLogEvent()  | false
        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().hubTime(10).toLogEvent() | true
        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().hubTime(11).toLogEvent() | true

        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.LessThan            | 10    | logEvent().hubTime(1).toLogEvent()  | true
        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.LessThan            | 10    | logEvent().hubTime(10).toLogEvent() | false
        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.LessThan            | 10    | logEvent().hubTime(11).toLogEvent() | false

        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().hubTime(1).toLogEvent()  | true
        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().hubTime(10).toLogEvent() | true
        TimeFieldFilter.Field.HubTime        | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().hubTime(11).toLogEvent() | false

        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().originTime(1).toLogEvent()  | false
        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().originTime(10).toLogEvent() | false
        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().originTime(11).toLogEvent() | true

        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().originTime(1).toLogEvent()  | false
        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().originTime(10).toLogEvent() | true
        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().originTime(11).toLogEvent() | true

        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.LessThan            | 10    | logEvent().originTime(1).toLogEvent()  | true
        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.LessThan            | 10    | logEvent().originTime(10).toLogEvent() | false
        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.LessThan            | 10    | logEvent().originTime(11).toLogEvent() | false

        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().originTime(1).toLogEvent()  | true
        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().originTime(10).toLogEvent() | true
        TimeFieldFilter.Field.OriginTime     | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().originTime(11).toLogEvent() | false

        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().sequence(1).toLogEvent()  | false
        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().sequence(10).toLogEvent() | false
        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().sequence(11).toLogEvent() | true

        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().sequence(1).toLogEvent()  | false
        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().sequence(10).toLogEvent() | true
        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().sequence(11).toLogEvent() | true

        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.LessThan            | 10    | logEvent().sequence(1).toLogEvent()  | true
        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.LessThan            | 10    | logEvent().sequence(10).toLogEvent() | false
        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.LessThan            | 10    | logEvent().sequence(11).toLogEvent() | false

        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().sequence(1).toLogEvent()  | true
        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().sequence(10).toLogEvent() | true
        TimeFieldFilter.Field.SequenceNumber | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().sequence(11).toLogEvent() | false

    }

    def "test metadata fields" (String field, TimeFieldFilter.Type type, long value, LogEvent input, boolean expected) {

        when:
        TimeFieldFilter filter = new TimeFieldFilter(field, type, value);

        then:
        filter.passes(input) == expected;

        where:

        field | type                                     | value | input                                       | expected

        "a"   | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().metadata("a", "1").toLogEvent()  | false
        "a"   | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().metadata("a", "10").toLogEvent() | false
        "a"   | TimeFieldFilter.Type.GreaterThan         | 10    | logEvent().metadata("a", "11").toLogEvent() | true

        "a"   | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().metadata("a", "1").toLogEvent()  | false
        "a"   | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().metadata("a", "10").toLogEvent() | true
        "a"   | TimeFieldFilter.Type.GreaterThanOrEquals | 10    | logEvent().metadata("a", "11").toLogEvent() | true

        "a"   | TimeFieldFilter.Type.LessThan            | 10    | logEvent().metadata("a", "1").toLogEvent()  | true
        "a"   | TimeFieldFilter.Type.LessThan            | 10    | logEvent().metadata("a", "10").toLogEvent() | false
        "a"   | TimeFieldFilter.Type.LessThan            | 10    | logEvent().metadata("a", "11").toLogEvent() | false

        "a"   | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().metadata("a", "1").toLogEvent()  | true
        "a"   | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().metadata("a", "10").toLogEvent() | true
        "a"   | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().metadata("a", "11").toLogEvent() | false

        "b"   | TimeFieldFilter.Type.LessThanOrEquals    | 10    | logEvent().metadata("a", "1").toLogEvent()  | false

    }

}
