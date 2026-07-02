package com.samueljuma.firebaseinaction.data.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.core.utils.firestoreSafeCall
import com.samueljuma.firebaseinaction.data.notifications.local.NotificationDao
import com.samueljuma.firebaseinaction.data.notifications.local.NotificationEntity
import com.samueljuma.firebaseinaction.data.notifications.remote.NotificationDto
import com.samueljuma.firebaseinaction.data.notifications.remote.toEntity as dtoToEntity
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.notifications.NotificationRepository
import com.samueljuma.firebaseinaction.domain.notifications.mapper.toDomain
import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao,
    private val firestore: FirebaseFirestore,
    private val sessionStorage: SessionStorage
) : NotificationRepository {

    private suspend fun getCurrentUserId(): String =
        sessionStorage.get()?.uid ?: error("No authenticated user")

    private fun notificationsCollection(userId: String) =
        firestore.collection("users/$userId/notifications")

    override fun getNotifications(): Flow<List<AppNotification>> =
        notificationDao.getNotifications()
            .map { entities -> entities.map { it.toDomain() } }

    override fun getUnreadCount(): Flow<Int> =
        notificationDao.getUnreadCount()

    override suspend fun markAsRead(notificationId: String): Result<Unit, DataError> =
        firestoreSafeCall {
            notificationDao.markAsRead(notificationId)
            val userId = getCurrentUserId()
            notificationsCollection(userId).document(notificationId)
                .update("read", true)
                .await()
        }

    override suspend fun deleteNotification(notificationId: String): Result<Unit, DataError> =
        firestoreSafeCall {
            notificationDao.deleteById(notificationId)
            val userId = getCurrentUserId()
            notificationsCollection(userId).document(notificationId)
                .delete()
                .await()
        }

    override fun startRemoteSync(): Flow<Unit> = flow {
        val userId = getCurrentUserId()
        val remoteEntities = callbackFlow {
            val listener = notificationsCollection(userId)
                .orderBy("receivedAt", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.tag(TAG).e(error, "Firestore listener error")
                        return@addSnapshotListener
                    }
                    val entities = snapshot?.documents
                        ?.mapNotNull { it.toObject(NotificationDto::class.java)?.dtoToEntity() }
                        .orEmpty()
                    trySend(entities)
                }
            awaitClose {
                Timber.tag(TAG).d("Stopping notifications listener")
                listener.remove()
            }
        }
        // Persist within the collecting coroutine — no detached launch.
        emitAll(
            remoteEntities.map { entities ->
                notificationDao.upsertNotifications(entities)
                Timber.tag(TAG).d("Synced ${entities.size} notifications")
            }
        )
    }

    override suspend fun clearLocalData() {
        notificationDao.clearAll()
    }

    companion object {
        private const val TAG = "NotificationRepo"
    }
}
