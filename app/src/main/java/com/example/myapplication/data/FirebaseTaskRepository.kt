package com.example.myapplication.data

import com.example.myapplication.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseTaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TaskRepository {

    private val tasksCollection = firestore.collection(COLLECTION_NAME)

    override fun getAllTasks(): Flow<TasksSnapshot> = callbackFlow {
        val listener = tasksCollection.addSnapshotListener(
            MetadataChanges.INCLUDE,
        ) { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val tasks = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                }
                val isOffline = snapshot.metadata.isFromCache
                trySend(TasksSnapshot(tasks, isOffline))
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun insert(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    override suspend fun update(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    override suspend fun delete(task: Task) {
        tasksCollection.document(task.id).delete().await()
    }

    private companion object {
        const val COLLECTION_NAME = "tasks"
    }
}