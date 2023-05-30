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

package com.google.android.sensory.sensing_sdk.capture

import com.google.android.sensory.sensing_sdk.model.SensorType

data class CaptureSettings(
  /** The file format captured sensor data should be stored in. TODO: Need to have some defaults. */
  val fileTypeMap: Map<SensorType, String>,
  val metaDataTypeMap: Map<SensorType, String>,
  val title: String,
)