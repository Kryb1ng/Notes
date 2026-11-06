package com.example.myapplication.data

import com.example.myapplication.model.AppEvent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val eventsCollection = firestore.collection("events")

    fun getAllEvents(): Flow<List<AppEvent>> = callbackFlow {
        val listener = eventsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val events = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(AppEvent::class.java)?.copy(id = doc.id)
                }
                trySend(events)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun insert(event: AppEvent) {
        eventsCollection.document(event.id).set(event).await()
    }

    suspend fun delete(event: AppEvent) {
        eventsCollection.document(event.id).delete().await()
    }
}