package com.caramel.mercury.room_db

interface PrefetchableQuery<T> {
    fun queryKey() : String
    suspend fun execute() : T
}