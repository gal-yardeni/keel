package com.netflix.spinnaker.keel.core.api

import com.netflix.spinnaker.keel.api.artifacts.DeliveryArtifact

/**
 * The request body of an artifact veto
 */
data class EnvironmentArtifactVeto(
  val targetEnvironment: String,
  val reference: String,
  val version: String,
  val vetoedBy: String?,
  val comment: String?
) {
  init {
    if (comment != null) {
      require(comment.length <= 255) {
        "Comment length should be 255 characters max."
      }
    }
  }
}

data class EnvironmentArtifactVetoes(
  val deliveryConfigName: String,
  val targetEnvironment: String,
  val artifact: DeliveryArtifact,
  val versions: MutableSet<String>
)
