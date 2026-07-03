package com.samueljuma.firebaseinaction.domain.notes.validator

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result

class NoteValidator {

    fun validate(
        title: String,
        content: String
    ): Result<Unit, DataError.Validation> {
        if (title.isBlank())
            return Result.Error(DataError.Validation.TITLE_EMPTY)
        if (title.length > TITLE_MAX_LENGTH)
            return Result.Error(DataError.Validation.TITLE_TOO_LONG)
        if (content.length > CONTENT_MAX_LENGTH)
            return Result.Error(DataError.Validation.CONTENT_TOO_LONG)
        return Result.Success(Unit)
    }

    companion object {
        const val TITLE_MAX_LENGTH = 500
        const val CONTENT_MAX_LENGTH = 50_000
    }
}