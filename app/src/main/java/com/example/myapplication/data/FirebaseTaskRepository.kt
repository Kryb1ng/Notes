package com.example.myapplication.data

import com.example.myapplication.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Реализация репозитория на Firebase Firestore.
 * Firestore принимается через конструктор → можно подменить/замокать.
 */
class FirebaseTaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TaskRepository {

    private val tasksCollection = firestore.collection(COLLECTION_NAME)

    override fun getAllTasks(): Flow<List<Task>> = callbackFlow {
        val listener = tasksCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val tasks = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(id = doc.id)
            }.orEmpty()
            trySend(tasks)
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