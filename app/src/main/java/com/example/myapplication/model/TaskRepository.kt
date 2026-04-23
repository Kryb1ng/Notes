package com.example.myapplication.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tasks")

    fun getAllTasks(): Flow<List<Task>> = callbackFlow {
        val subscription = tasksCollection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val tasks = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                }
                trySend(tasks)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun insert(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    suspend fun update(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    suspend fun delete(task: Task) {
        tasksCollection.document(task.id).delete().await()
    }
}

