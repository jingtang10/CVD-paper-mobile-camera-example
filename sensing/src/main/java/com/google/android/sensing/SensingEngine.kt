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

package com.google.android.sensing

import android.content.Intent
import com.google.android.sensing.capture.SensorCaptureResult
import com.google.android.sensing.model.CaptureInfo
import com.google.android.sensing.model.ResourceInfo
import com.google.android.sensing.model.UploadRequest
import com.google.android.sensing.model.UploadResult
import com.google.android.sensing.upload.SyncUploadProgress
import com.google.android.sensing.upload.SyncUploadState
import kotlinx.coroutines.flow.Flow

/**
 * The Sensing Engine interface that handles the local storage of captured resources. TODO: CRUD
 * APIs for UploadRequest to use only the upload mechanism this Engine provides.
 */
interface SensingEngine {

  /** Get CaptureInfo record given @param captureId */
  suspend fun getCaptureInfo(captureId: String): CaptureInfo

  /** Delete all data associated with [captureId] */
  suspend fun deleteDataInCapture(captureId: String): Boolean

  /**
   * Responsible for creating resource records for captured data and completing upload setup. This
   * API is triggered by [CaptureViewModel] after completion of capturing. Limitation: All captured
   * data and metadata are stored in the same folder and zipped for uploading.
   * 1. Save [CaptureInfo] in the database
   * 2. read map for a capture type, for each sensor type:-
   * ```
   *      a. create [ResourceInfo] for it and save it in the database.
   *      b. emit [SensorCaptureResult.StateChange].
   *      c. zip the [captureInfo.captureFolder]/[sensorType] folder.
   *      d. create [UploadRequest] for it and save it in the database.
   * ```
   * TODO: Support uploading of any mime type.
   */
  suspend fun onCaptureCompleteCallback(captureInfo: CaptureInfo): Flow<SensorCaptureResult>

  /**
   * Lists all ResourceInfo given a participantId. This will return all ResourceInfo across multiple
   * capturings.
   */
  suspend fun listResourceInfoForParticipant(participantId: String): List<ResourceInfo>

  /**
   * Lists all ResourceInfo given a captureId. This will return all ResourceInfo for a single
   * capture.
   */
  suspend fun listResourceInfoInCapture(captureId: String): List<ResourceInfo>

  /** To support 3P apps */
  suspend fun captureSensorData(pendingIntent: Intent)

  /**
   * Upload API that orchestrates record fetching, invoking upload, collecting [UploadResult]s,
   * updating records, emitting of [SyncUploadState]es. Additionally, it also deletes captured files
   * and folders after successful upload.
   *
   * [SensorDataUploadWorker] invokes this API.
   */
  suspend fun syncUpload(
    upload: (suspend (List<UploadRequest>) -> Flow<UploadResult>)
  ): Flow<SyncUploadProgress>

  /**
   * Get [UploadRequest] corresponding to the [ResourceInfo] given [ResourceInfo.resourceInfoId].
   * Application developers can use this API to monitor not just upload status but also progress.
   */
  suspend fun getUploadRequest(resourceInfoId: String): UploadRequest?

  /** Delete data stored in blobstore */
  suspend fun deleteSensorData(uploadURL: String)
  suspend fun deleteSensorMetaData(uploadURL: String)
}
