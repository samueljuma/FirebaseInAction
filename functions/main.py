"""Cloud Functions for Notey.

M0 (scaffold): initialize the Admin SDK so functions added in later
milestones can read Firestore and send FCM messages. The per-note
reminder scheduler is added in M5 - no functions are exported yet.
"""
from firebase_admin import initialize_app

initialize_app()
