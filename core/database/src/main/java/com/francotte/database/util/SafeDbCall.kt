package com.francotte.database.util

import android.database.sqlite.SQLiteException
import com.francotte.common.utils.AppError
import com.francotte.common.utils.DataResult
import java.sql.SQLException

inline fun <T> safeDbCall(block: () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (t: Throwable) {
        val isDb = t is SQLiteException || t is SQLException || t is IllegalStateException
        if (isDb) DataResult.Failure(AppError.Database(t, t.message))
        else DataResult.Failure(AppError.Unknown(t, t.message))
    }
