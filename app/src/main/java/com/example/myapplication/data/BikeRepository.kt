package com.example.myapplication.data

import com.example.myapplication.model.Bike
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BikeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val bikesCollection = firestore.collection("bikes")

    fun getAllBikes(): Flow<List<Bike>> = callbackFlow {
        val listener = bikesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val bikes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Bike::class.java)?.copy(id = doc.id)
                }
                trySend(bikes)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun insert(bike: Bike) {
        bikesCollection.document(bike.id).set(bike).await()
    }

    suspend fun update(bike: Bike) {
        bikesCollection.document(bike.id).set(bike.copy(updatedAt = System.currentTimeMillis())).await()
    }

    suspend fun delete(bike: Bike) {
        bikesCollection.document(bike.id).delete().await()
    }
}