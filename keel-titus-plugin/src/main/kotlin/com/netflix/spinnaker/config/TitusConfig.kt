/*
 *
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.netflix.spinnaker.config

import com.netflix.spinnaker.keel.api.actuation.TaskLauncher
import com.netflix.spinnaker.keel.api.plugins.Resolver
import com.netflix.spinnaker.keel.api.titus.cluster.TitusClusterHandler
import com.netflix.spinnaker.keel.clouddriver.CloudDriverCache
import com.netflix.spinnaker.keel.clouddriver.CloudDriverService
import com.netflix.spinnaker.keel.orca.OrcaService
import java.time.Clock
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty("keel.plugins.titus.enabled")
class TitusConfig {
  @Bean
  fun titusClusterHandler(
    cloudDriverService: CloudDriverService,
    cloudDriverCache: CloudDriverCache,
    orcaService: OrcaService,
    clock: Clock,
    taskLauncher: TaskLauncher,
    publisher: ApplicationEventPublisher,
    resolvers: List<Resolver<*>>
  ): TitusClusterHandler = TitusClusterHandler(
    cloudDriverService,
    cloudDriverCache,
    orcaService,
    clock,
    taskLauncher,
    publisher,
    resolvers
  )
}
