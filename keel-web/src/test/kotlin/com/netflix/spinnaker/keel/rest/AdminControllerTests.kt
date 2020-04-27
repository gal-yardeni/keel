package com.netflix.spinnaker.keel.rest

import com.netflix.spinnaker.keel.KeelApplication
import com.netflix.spinnaker.keel.api.DeliveryConfig
import com.netflix.spinnaker.keel.pause.ActuationPauser
import com.netflix.spinnaker.keel.persistence.KeelRepository
import com.netflix.spinnaker.keel.persistence.memory.InMemoryDeliveryConfigRepository
import com.netflix.spinnaker.keel.spring.test.MockEurekaConfiguration
import com.ninjasquad.springmockk.MockkBean
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import io.mockk.clearAllMocks
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AdminControllerTests {

  @ExtendWith(SpringExtension::class)
  @SpringBootTest(
    classes = [KeelApplication::class, MockEurekaConfiguration::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
  )
  @AutoConfigureMockMvc
  internal class AdminControllerTests : JUnit5Minutests {

    @MockkBean
    lateinit var authorizationSupport: AuthorizationSupport

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var deliveryConfigRepository: InMemoryDeliveryConfigRepository

    @Autowired
    lateinit var combinedRepository: KeelRepository

    @Autowired
    lateinit var actuationPauser: ActuationPauser

    companion object {
      const val application1 = "fnord"
      const val application2 = "fnord2"
    }

    fun tests() = rootContext {
      after {
        deliveryConfigRepository.dropAll()
        clearAllMocks()
      }

      context("return a single application") {
        before {
          val deliveryConfig = DeliveryConfig(
            name = "$application1-manifest",
            application = application1,
            serviceAccount = "keel@spinnaker",
            artifacts = emptySet(),
            environments = emptySet()
          )
          combinedRepository.upsertDeliveryConfig(deliveryConfig)
          authorizationSupport.allowAll()
        }

        test("can get basic summary of unpaused application") {
          val request = MockMvcRequestBuilders.get("/admin/applications/")
            .accept(MediaType.APPLICATION_JSON_VALUE)
          mvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().json(
              """
              [{
                "name": "fnord-manifest",
                "application": "fnord",
                "serviceAccount": "keel@spinnaker",
                "apiVersion": "delivery.config.spinnaker.netflix.com/v1",
                "isPaused":false
              }]
            """.trimIndent()
            ))
        }

        context("with paused application") {
          before {
            actuationPauser.pauseApplication(application1)
          }

//          after {
//            actuationPauser.resumeApplication(application)
//          }

          test("can get basic summary of a pause application") {
            val request = MockMvcRequestBuilders.get("/admin/applications/")
              .accept(MediaType.APPLICATION_JSON_VALUE)
            mvc
              .perform(request)
              .andExpect(MockMvcResultMatchers.status().isOk)
              .andExpect(MockMvcResultMatchers.content().json(
                """
              [{
                "name": "fnord-manifest",
                "application": "fnord",
                "serviceAccount": "keel@spinnaker",
                "apiVersion": "delivery.config.spinnaker.netflix.com/v1",
                "isPaused":true
              }]
            """.trimIndent()
              ))
          }
        }

        context("more than one applications") {
          before {
            val deliveryConfig2 = DeliveryConfig(
              name = "$application2-manifest",
              application = application2,
              serviceAccount = "keel@spinnaker",
              artifacts = emptySet(),
              environments = emptySet()
            )
            combinedRepository.upsertDeliveryConfig(deliveryConfig2)
            authorizationSupport.allowAll()
          }

          test("can get basic summary of 2 applications, one paused") {
            val request = MockMvcRequestBuilders.get("/admin/applications/")
              .accept(MediaType.APPLICATION_JSON_VALUE)
            mvc
              .perform(request)
              .andExpect(MockMvcResultMatchers.status().isOk)
              .andExpect(MockMvcResultMatchers.content().json(
                """
              [{
                "name": "fnord-manifest",
                "application": "fnord",
                "serviceAccount": "keel@spinnaker",
                "apiVersion": "delivery.config.spinnaker.netflix.com/v1",
                "isPaused":true
              },{
                "name": "fnord2-manifest",
                "application": "fnord2",
                "serviceAccount": "keel@spinnaker",
                "apiVersion": "delivery.config.spinnaker.netflix.com/v1",
                "isPaused":false
                }
              ]
            """.trimIndent()
              ))
          }
        }
      }

      context("no delivery config found") {
        test("return an empty list") {
          val request = MockMvcRequestBuilders.get("/admin/applications/")
            .accept(MediaType.APPLICATION_JSON_VALUE)
          mvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().json(
              """
              []
            """.trimIndent()
            ))
        }
      }
    }
  }
}
