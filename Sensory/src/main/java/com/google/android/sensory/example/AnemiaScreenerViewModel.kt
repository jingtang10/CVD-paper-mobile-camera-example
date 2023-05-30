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

package com.google.android.sensory.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.mapping.StructureMapExtractionContext
import com.google.android.fhir.testing.jsonParser
import com.google.android.sensory.sensing_sdk.SensingEngine
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Attachment
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.DocumentReference
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.utils.StructureMapUtilities

/** ViewModel for screener questionnaire screen {@link ScreenerEncounterFragment}. */
class AnemiaScreenerViewModel(application: Application, private val state: SavedStateHandle) :
  AndroidViewModel(application) {

  val questionnaireFragment =
    QuestionnaireFragment.builder()
      .setQuestionnaire(questionnaire)
      .setCustomQuestionnaireItemViewHolderFactoryMatchersProvider(
        SensingApplication.CUSTOM_VIEW_HOLDER_FACTORY_TAG
      )
      .setShowSubmitButton(false)
      .build()
  val questionnaire: String
    get() = getQuestionnaireJson()
  val isResourcesSaved = MutableLiveData<Boolean>()

  private val questionnaireResource: Questionnaire
    get() =
      FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire)
        as Questionnaire
  private var questionnaireJson: String? = null
  private var fhirEngine: FhirEngine = SensingApplication.fhirEngine(application.applicationContext)

  private var sensingEngine: SensingEngine =
    SensingApplication.sensingEngine(application.applicationContext)

  /**
   * Saves screener encounter questionnaire response into the application database.
   *
   * @param questionnaireResponse screener encounter questionnaire response
   */
  fun saveScreenerEncounter(questionnaireResponse: QuestionnaireResponse, patientId: String) {
    viewModelScope.launch {
      val mapping =
        """
          map "http://example.org/fhir/StructureMap/SensorCapture" = 'SensorCapture'
          uses "http://hl7.org/fhir/StructureDefinition/QuestionnaireReponse" as source
          uses "http://hl7.org/fhir/StructureDefinition/Bundle" as target
 
          group SensorCapture(source src : QuestionnaireResponse, target bundle: Bundle) {
            src -> bundle.id = uuid() "rule_bundle_id";
            src -> bundle.type = 'collection' "rule_bundle_type";
            src -> bundle.entry as entry, entry.resource = create('DocumentReference') as ppgdocref then
              ExtractPPGDocumentReference(src, ppgdocref) "rule_extract_document_reference";
            src -> bundle.entry as entry, entry.resource = create('DocumentReference') as photofingernailscloseddocref then
              ExtractFingernailsClosedPhotoDocumentReference(src, photofingernailscloseddocref) "rule_extract_photo_fingernails_closed_document_reference";
            src -> bundle.entry as entry, entry.resource = create('DocumentReference') as photofingernailsopendocref then
              ExtractFingernailsOpenPhotoDocumentReference(src, photofingernailsopendocref) "rule_extract_photo_fingernails_open_document_reference";
            src -> bundle.entry as entry, entry.resource = create('DocumentReference') as photoconjunctivadocref then
              ExtractConjunctivaPhotoDocumentReference(src, photoconjunctivadocref) "rule_extract_photo_conjunctiva_document_reference";
          }
 
          group ExtractPPGDocumentReference(source src : QuestionnaireResponse, target tgt : Patient) {
            src.item as item where(linkId = 'sensing-capture-group-ppg') then {
              item.item as inner_item where (linkId = 'ppg-capture-api-call') then {
                inner_item.answer first as ans then {
                  ans.value as coding then {
                    coding.code as val -> tgt.type = val "rule_ppg_capture_id";
                  };
                };
              };
          };
        }
          
          group ExtractFingernailsOpenPhotoDocumentReference(source src : QuestionnaireResponse, target tgt : Patient) {
            src.item as item where(linkId = 'sensing-capture-group-fingernails-open') then {
              item.item as inner_item where (linkId = 'photo-capture-fingernails-open-api-call') then {
                    inner_item.answer first as ans then {
                      ans.value as coding then {
                        coding.code as val -> tgt.type = val "rule_photo_capture_id";
                      };
                    };
                  };
            };
          }
          
          group ExtractFingernailsClosedPhotoDocumentReference(source src : QuestionnaireResponse, target tgt : Patient) {
            src.item as item where(linkId = 'sensing-capture-group-fingernails-closed') then {
              item.item as inner_item where (linkId = 'photo-capture-fingernails-closed-api-call') then {
                    inner_item.answer first as ans then {
                      ans.value as coding then {
                        coding.code as val -> tgt.type = val "rule_photo_capture_id";
                      };
                    };
                  };
            };
          }
          
          group ExtractConjunctivaPhotoDocumentReference(source src : QuestionnaireResponse, target tgt : Patient) {
            src.item as item where(linkId = 'sensing-capture-group-conjunctiva') then {
              item.item as inner_item where (linkId = 'photo-capture-conjunctiva-api-call') then {
                    inner_item.answer first as ans then {
                      ans.value as coding then {
                        coding.code as val -> tgt.type = val "rule_photo_capture_id";
                      };
                    };
                  };
            };
          }
                """.trimIndent()

      val iParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()

      val uriTestQuestionnaire =
        iParser.parseResource(Questionnaire::class.java, questionnaireJson) as Questionnaire

      val uriTestQuestionnaireResponse =
        iParser.parseResource(
          QuestionnaireResponse::class.java,
          iParser.encodeResourceToString(questionnaireResponse)
        ) as QuestionnaireResponse

      val bundle =
        ResourceMapper.extract(
          uriTestQuestionnaire,
          uriTestQuestionnaireResponse,
          StructureMapExtractionContext(
            context = getApplication<Application>().applicationContext
          ) { _, worker -> StructureMapUtilities(worker).parse(mapping, "") },
        )

      val subjectReference = Reference("Patient/$patientId")
      val encounterId = generateUuid()
      if (isRequiredFieldMissing(bundle)) {
        isResourcesSaved.value = false
        return@launch
      }
      saveResources(bundle, subjectReference, encounterId)
      isResourcesSaved.value = true
    }
  }

  private suspend fun saveResources(
    bundle: Bundle,
    subjectReference: Reference,
    encounterId: String,
  ) {
    val encounterReference = Reference("Encounter/$encounterId")
    bundle.entry.forEach {
      when (val resource = it.resource) {
        is DocumentReference -> {
          val captureId =
            if (resource.type.coding.isNullOrEmpty()) "" else resource.type.coding[0].code
          val resourceInfo = sensingEngine.listResourceInfoInCapture(captureId!!)[0]
          resource.id = generateUuid()
          resource.status = Enumerations.DocumentReferenceStatus.CURRENT
          resource.subject = subjectReference
          resource.date = Date()

          // modify data based on the nature of the capture (using resourceInfo obtained from
          // captureId)
          val data =
            Attachment().apply {
              contentType = "application/gzip" // Sensing SDK uploads only in zip for now
              url = resourceInfo.uploadURL
              title = resourceInfo.title
              creation = Date()
            }

          val dataList: MutableList<DocumentReference.DocumentReferenceContentComponent> =
            mutableListOf(DocumentReference.DocumentReferenceContentComponent(data))
          resource.content = dataList
          resource.description = ""
          saveResourceToDatabase(resource)
        }
        is Observation -> {
          if (resource.hasCode()) {
            resource.id = generateUuid()
            resource.subject = subjectReference
            resource.encounter = encounterReference
            saveResourceToDatabase(resource)
          }
        }
        is Condition -> {
          if (resource.hasCode()) {
            resource.id = generateUuid()
            resource.subject = subjectReference
            resource.encounter = encounterReference
            saveResourceToDatabase(resource)
          }
        }
        is Encounter -> {
          resource.subject = subjectReference
          resource.id = encounterId
          saveResourceToDatabase(resource)
        }
      }
    }
  }

  private fun isRequiredFieldMissing(bundle: Bundle): Boolean {
    bundle.entry.forEach {
      when (val resource = it.resource) {
        is Observation -> {
          if (resource.hasValueQuantity() && !resource.valueQuantity.hasValueElement()) {
            return true
          }
        }
      // TODO check other resources inputs
      }
    }
    return false
  }

  private suspend fun saveResourceToDatabase(resource: Resource) {
    fhirEngine.create(resource)
  }

  private fun getQuestionnaireJson(): String {
    questionnaireJson?.let {
      return it
    }
    questionnaireJson =
      readFileFromAssets(state[AnemiaScreenerFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
    val questionnaire = jsonParser.parseResource(questionnaireJson) as Questionnaire
    return questionnaireJson!!
  }

  private fun readFileFromAssets(filename: String): String {
    return getApplication<Application>().assets.open(filename).bufferedReader().use {
      it.readText()
    }
  }

  private fun generateUuid(): String {
    return UUID.randomUUID().toString()
  }
}