const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();

// Automated Verification Webhook for DLIMS & NADRA
exports.verifyDriverCredentials = functions.https.onCall(async (data, context) => {
    const { cnic, licenseNumber, province } = data;

    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "User must be logged in.");
    }

    try {
        // Step 1: DLIMS License Verification Query
        const dlimsResponse = await axios.post("https://dlims.punjab.gov.pk/api/verify", {
            cnic: cnic,
            license_no: licenseNumber,
            province: province
        });

        const isLicenseValid = dlimsResponse.data.status === "VALID";

        // Step 2: Store Verification Result in Firestore
        await admin.firestore().collection("drivers").doc(context.auth.uid).set({
            verified: isLicenseValid,
            cnic: cnic,
            licenseNumber: licenseNumber,
            verificationTimestamp: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });

        return { success: true, verified: isLicenseValid };
    } catch (error) {
        throw new functions.https.HttpsError("internal", "Verification service down.");
    }
});
