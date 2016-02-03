package com.logginghub.logging.filters

import com.logginghub.logging.LogEvent
import spock.lang.Specification

import static com.logginghub.logging.LogEventBuilder.logEvent
import static com.logginghub.logging.filters.MultiValueOrFieldFilter.*

/**
 * Created by james on 03/02/2016.
 */
class MultiValueOrFieldFilterTest extends Specification {

    def "test regular fields"(Field field, Type type, String value, boolean caseSensitive, LogEvent input, boolean expected) {

        when:
        MultiValueOrFieldFilter filter = new MultiValueOrFieldFilter(field, type, value, caseSensitive);

        then:
        filter.passes(input) == expected;

        where:
        field         | type           | value   | caseSensitive | input                                | expected
        Field.Message |  Type.Contains | "a,b,c" | false         | logEvent().message("a").toLogEvent() | true
        Field.Message |  Type.Contains | "a,b,c" | false         | logEvent().message("b").toLogEvent() | true
        Field.Message |  Type.Contains | "a,b,c" | false         | logEvent().message("c").toLogEvent() | true
        Field.Message |  Type.Contains | "a,b,c" | false         | logEvent().message("d").toLogEvent() | false


    }

    def "test metadata fields"(String field, Type type, String value, boolean caseSensitive, LogEvent input, boolean expected) {

        when:
        MultiValueOrFieldFilter filter = new MultiValueOrFieldFilter(field, type, value, caseSensitive);

        then:
        filter.passes(input) == expected;

        where:
        field | type          | value   | caseSensitive | input                                        | expected
        "key" | Type.Contains | "a,b,c" | false         | logEvent().metadata("key", "a").toLogEvent() | true
        "key" | Type.Contains | "a,b,c" | false         | logEvent().metadata("key", "b").toLogEvent() | true
        "key" | Type.Contains | "a,b,c" | false         | logEvent().metadata("key", "c").toLogEvent() | true
        "key" | Type.Contains | "a,b,c" | false         | logEvent().metadata("key", "d").toLogEvent() | false
    }

}
