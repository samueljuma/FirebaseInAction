package com.samueljuma.firebaseinaction.data.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.core.utils.firestoreSafeCall
import com.samueljuma.firebaseinaction.data.notifications.local.NotificationDao
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.notifications.NotificationRepository
import com.samueljuma.firebaseinaction.domain.notifications.mapper.toDomain
import com.samueljuma.firebaseinaction.domain.notifications.mapper.toEntity
import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao,
    private val firestore: FirebaseFirestore,
    private val sessionStorage: SessionStorage
) : NotificationRepository {

    private suspend fun getCurrentUserId(): String =
        sessionStorage.get()?.uid ?: error("No authenticated user")

    override fun getNotifications(): Flow<List<AppNotification>> =
        notificationDao.getNotifications()
            .map { entities -> entities.map { it.toDomain() } }

    override fun getUnreadCount(): Flow<Int> =
        notificationDao.getUnreadCount()

    override suspend fun saveNotification(notification: AppNotification): Result<Unit, DataError> {
        return try {
            notificationDao.upsertNotification(notification.toEntity())
            firestoreSafeCall {
                val userId = getCurrentUserId()
                firestore.document("users/$userId/notifications/${notification.id}")
                    .set(notification)
                    .await()
            }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to save notification")
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit, DataError> {
        return try {
            notificationDao.markAsRead(notificationId)
            firestoreSafeCall {
                val userId = getCurrentUserId()
                firestore.document("users/$userId/notifications/$notificationId")
                    .update("read", true)
                    .await()
            }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to mark notification as read")
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override fun startRemoteSync(): Flow<Unit> = flow {
        val userId = getCurrentUserId()
        emitAll(
            callbackFlow {
                val listener = firestore
                    .collection("users/$userId/notifications")
                    .orderBy("receivedAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Timber.tag(TAG).e(error, "Firestore listener error")
                            return@addSnapshotListener
                        }
                        snapshot?.documents?.let { docs ->
                            launch(Dispatchers.IO) {
                                val entities = docs.mapNotNull { doc ->
                                    doc.toObject(AppNotification::class.java)?.toEntity()
                                }
                                notificationDao.upsertNotifications(entities)
                                Timber.tag(TAG).d("Synced ${entities.size} notifications")
                            }
                        }
                        trySend(Unit)
                    }
                awaitClose {
                    Timber.tag(TAG).d("Stopping notifications listener")
                    listener.remove()
                }
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