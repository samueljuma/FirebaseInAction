package com.samueljuma.firebaseinaction.data.util

import com.google.firebase.firestore.FirebaseFirestore
import com.samueljuma.firebaseinaction.domain.util.IdGenerator

class FirestoreIdGenerator : IdGenerator {
    private val firestore = FirebaseFirestore.getInstance()
    override fun generate(): String =
        firestore.collection("_").document().id
}