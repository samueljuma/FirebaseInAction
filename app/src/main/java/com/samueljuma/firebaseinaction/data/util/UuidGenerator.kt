package com.samueljuma.firebaseinaction.data.util

import com.samueljuma.firebaseinaction.domain.util.IdGenerator
import java.util.UUID

class UuidGenerator : IdGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}