/*
 * Copyright (c) 2019 vitasystems GmbH and Hannover Medical School.
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
package org.ehrbase.api.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.ehrbase.api.exception.InternalServerException;
import org.ehrbase.openehr.sdk.response.dto.ehrscape.CompositionFormat;
import org.ehrbase.openehr.sdk.response.dto.ehrscape.ContributionDto;

/**
 * Interface for contribution service roughly based on openEHR SM "I_EHR_CONTRIBUTION Interface",
 * see: https://specifications.openehr.org/releases/SM/latest/openehr_platform.html#_i_ehr_contribution_interface
 */
public interface ContributionService extends BaseService {

    /**
     * Check if given contribution exists and is part of given EHR.
     * @param ehrId ID of EHR
     * @param contributionId ID of contribution
     * @return True if exists and part of EHR, false if not
     */
    boolean hasContribution(UUID ehrId, UUID contributionId);

    /**
     * Return the Contribution with given id in given EHR.
     * @param ehrId ID of EHR
     * @param contributionId ID of contribution
     * @return {@link Optional} containing a {@link ContributionDto} if successful, empty if not
     */
    Optional<ContributionDto> getContribution(UUID ehrId, UUID contributionId);

    /**
     * Commit a CONTRIBUTION containing any number of serialized VERSION<Type> objects.
     * @param ehrId ID of EHR
     * @param content serialized content, containing version objects and audit object in given format
     * @param format format of serialized versions
     * @return ID of successfully committed contribution
     * @throws IllegalArgumentException when input can't be processed
     * @throws InternalServerException when DB is inconsistent
     */
    UUID commitContribution(UUID ehrId, String content, CompositionFormat format);

    /**
     * Admin method to delete a Contribution from the DB. See EHRbase Admin API specification for details.
     * @param contributionId Contribution to delete
     */
    void adminDelete(UUID contributionId);

    /**
     * Extracts set of used templates in payload's compositions.
     * @param contribution Contribution request content
     * @param format Format of that content
     * @return Set of templates used by compositions
     */
    Set<String> getListOfTemplates(String contribution, CompositionFormat format);
}
