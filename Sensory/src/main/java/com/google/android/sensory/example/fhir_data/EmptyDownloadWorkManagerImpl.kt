/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.sensory.example.fhir_data

import com.google.android.fhir.sync.DownloadWorkManager
import com.google.android.fhir.sync.Request
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType

class EmptyDownloadWorkManagerImpl : DownloadWorkManager {
  override suspend fun getNextRequest(): Request? {
    return null
  }

  override suspend fun getSummaryRequestUrls(): Map<ResourceType, String> {
    return emptyMap()
  }

  override suspend fun processResponse(response: Resource): Collection<Resource> {
    return emptyList()
  }
}