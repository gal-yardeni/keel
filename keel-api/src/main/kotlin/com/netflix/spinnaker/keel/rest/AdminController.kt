package com.netflix.spinnaker.keel.rest

import com.netflix.spinnaker.keel.persistence.DeliveryConfigRepository
import com.netflix.spinnaker.keel.persistence.NoSuchResourceException
import com.netflix.spinnaker.keel.persistence.ResourceRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.ExceptionHandler

@RestController
@RequestMapping(path = ["/admin"])
class AdminController(
  private val deliveryConfigRepository: DeliveryConfigRepository,
  private val resourceRepository: ResourceRepository
) {
  private val log by lazy { getLogger(javaClass) }

  @DeleteMapping(
      path = ["/applications/{application}"]
  )
  @ResponseStatus(ACCEPTED)
  fun deleteApplicationData(
    @PathVariable("application") application: String
  ) {
    log.debug("Deleting all data for application: $application")
    resourceRepository.deleteByApplication(application)
    deliveryConfigRepository.deleteByApplication(application)
  }

  @ExceptionHandler(NoSuchResourceException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun onNotFound(e: NoSuchResourceException) {
    log.error(e.message)
  }
}
