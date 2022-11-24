/*
 * Copyright (c) 2021 vitasystems GmbH and Hannover Medical School.
 *
 * This file is part of project EHRbase
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ehrbase.rest.openehr.audit;

import org.openehealth.ipf.commons.audit.types.EnumeratedCodedValue;
import org.openehealth.ipf.commons.audit.types.EventType;

/**
 * openEHR Event Type codes used in audit messages.
 */
public enum OpenEhrEventTypeCode implements EventType, EnumeratedCodedValue<EventType> {
    CREATE("249", "creation"),

    UPDATE("251", "modification"),

    DELETE("523", "deleted");

    private final EventType value;

    OpenEhrEventTypeCode(String code, String originalText) {
        this.value = EventType.of(code, "openehr", originalText);
    }

    @Override
    public EventType getValue() {
        return value;
    }
}