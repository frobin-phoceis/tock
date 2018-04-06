/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.front.shared.config

import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.EntityValue
import fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue
import org.litote.kmongo.Data

/**
 * A classification for an entity.
 */
@Data
data class ClassifiedEntity(
    /**
     * The entity type.
     */
    val type: String,
    /**
     * The entity role.
     */
    val role: String,
    /**
     * Start index of the entity in the text.
     */
    val start: Int,
    /**
     * End index (exclusive) of the entity in the text.
     */
    val end: Int,
    /**
     * The sub entities of the entity.
     */
    val subEntities: List<ClassifiedEntity> = emptyList()
) {

    constructor(value: ParsedEntityValue) : this(
        value.entity.entityType.name,
        value.entity.role,
        value.start,
        value.end,
        value.subEntities.map { ClassifiedEntity(it) })

    constructor(value: EntityValue) : this(
        value.entity.entityType.name,
        value.entity.role,
        value.start,
        value.end,
        value.subEntities.map { ClassifiedEntity(it.value) }
    )

    fun toEntityValue(entityProvider: (String, String) -> Entity?): EntityValue? =
        toEntity(entityProvider)
            ?.run {
                EntityValue(
                    start,
                    end,
                    this,
                    subEntities = subEntities.mapNotNull {
                        it.toEntityRecognition(entityProvider)
                    }
                )
            }

    fun toEntityRecognition(entityProvider: (String, String) -> Entity?): EntityRecognition? =
        toEntityValue(entityProvider)?.run { EntityRecognition(this, 1.0) }

    fun toEntity(entityProvider: (String, String) -> Entity?): Entity? = entityProvider.invoke(type, role)
}