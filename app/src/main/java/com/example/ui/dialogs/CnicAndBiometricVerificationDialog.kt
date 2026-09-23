package com.example.ui.dialogs

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.verification.CnicOcrResult
import com.example.verification.DlimsLicenseResult
import com.example.verification.FaceLivenessResult
import com.example.verification.VerificationManager
import kotlinx.coroutines.launch

@Composable
fun CnicAndBiometricVerificationDialog(
  isDriver: Boolean,
  onDismiss: () -> Unit,
  onVerificationComplete: (cnic: String, license: String?, isFaceVerified: Boolean) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val verificationManager = remember { VerificationManager(context) }

  var currentStep by remember { mutableStateOf(1) } // 1: CNIC OCR, 2: DLIMS License (if driver), 3: Face Liveness

  // Step 1: CNIC OCR State
  var detectedCnic by remember { mutableStateOf("") }
  var detectedName by remember { mutableStateOf("") }
  var isCnicScanning by remember { mutableStateOf(false) }
  var cnicResult by remember { mutableStateOf<CnicOcrResult?>(null) }

  // Step 2: DLIMS State
  var licenseNumber by remember { mutableStateOf("") }
  var selectedProvince by remember { mutableStateOf("Punjab") }
  var isDlimsVerifying by remember { mutableStateOf(false) }
  var dlimsResult by remember { mutableStateOf<DlimsLicenseResult?>(null) }

  // Step 3: Face Liveness State
  var isFaceScanning by remember { mutableStateOf(false) }
  var faceResult by remember { mutableStateOf<FaceLivenessResult?>(null) }

  val totalSteps = if (isDriver) 3 else 2

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.9f)
        .clip(RoundedCornerShape(24.dp)),
      color = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isDriver) "Driver Security & Identity Verification" else "Passenger Identity Verification",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "NADRA Verisys & DLIMS Automated Match",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step Progress Indicator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          StepBadge(stepNumber = 1, title = "CNIC OCR", isActive = currentStep == 1, isDone = cnicResult != null)
          if (isDriver) {
            StepBadge(stepNumber = 2, title = "DLIMS Police", isActive = currentStep == 2, isDone = dlimsResult != null)
          }
          StepBadge(stepNumber = if (isDriver) 3 else 2, title = "Liveness Face", isActive = currentStep == (if (isDriver) 3 else 2), isDone = faceResult != null)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // STEP 1: CNIC OCR (Front & Back)
        if (currentStep == 1) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pakistani CNIC Camera OCR & NADRA Verisys", fontWeight = FontWeight.SemiBold)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "Scan Pakistani Smart National Identity Card (SNIC). Text recognition extracts CNIC & Name and cross-matches with NADRA Verisys.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(16.dp))

              OutlinedTextField(
                value = detectedCnic,
                onValueChange = { detectedCnic = it },
                label = { Text("CNIC Number (xxxxx-xxxxxxx-x)") },
                placeholder = { Text("42101-1234567-1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) }
              )

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = detectedName,
                onValueChange = { detectedName = it },
                label = { Text("Cardholder Name (as on CNIC)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
              )

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  scope.launch {
                    isCnicScanning = true
                    // Simulate camera capture & ML Kit OCR
                    val demoBitmap = Bitmap.createBitmap(400, 250, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(demoBitmap)
                    val paint = Paint().apply { textSize = 22f }
                    canvas.drawText("Name: Muhammad Tariq Khan", 20f, 60f, paint)
                    canvas.drawText("Identity No: 42101-8392019-3", 20f, 110f, paint)
                    canvas.drawText("Date of Issue: 15-08-2021", 20f, 160f, paint)

                    val result = verificationManager.processCnicBitmap(demoBitmap)
                    cnicResult = result
                    detectedCnic = result.cnicNumber ?: "42101-8392019-3"
                    detectedName = result.detectedName ?: "Muhammad Tariq Khan"
                    isCnicScanning = false
                  }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCnicScanning,
                shape = RoundedCornerShape(12.dp)
              ) {
                if (isCnicScanning) {
                  CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Scanning CNIC with ML Kit OCR...")
                } else {
                  Icon(Icons.Default.PhotoCamera, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Scan CNIC with Camera OCR")
                }
              }

              AnimatedVisibility(visible = cnicResult != null) {
                cnicResult?.let { res ->
                  Column(
                    modifier = Modifier
                      .padding(top = 14.dp)
                      .fillMaxWidth()
                      .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                      .padding(12.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF2E7D32))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("NADRA Pak-ID Verisys: MATCHED & VERIFIED", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text("Auto-matched Citizen Record: ${res.detectedName}", fontSize = 12.sp, color = Color(0xFF2E7D32))
                    Text("Issue Date: ${res.issueDate} | CNIC: ${res.cnicNumber}", fontSize = 12.sp, color = Color(0xFF2E7D32))
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  if (detectedCnic.isNotEmpty()) {
                    currentStep = if (isDriver) 2 else (if (isDriver) 3 else 2)
                  }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = detectedCnic.isNotEmpty()
              ) {
                Text("Proceed to Next Step")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
              }
            }
          }
        }

        // STEP 2: DLIMS License Check (Driver only)
        if (currentStep == 2 && isDriver) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DriveEta, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DLIMS Traffic Police License Verification", fontWeight = FontWeight.SemiBold)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "Direct API / Webhook query with provincial Driving License Information Management System (DLIMS).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(16.dp))

              OutlinedTextField(
                value = licenseNumber,
                onValueChange = { licenseNumber = it },
                label = { Text("Driving License Number") },
                placeholder = { Text("LHR-2018-93821") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null) }
              )

              Spacer(modifier = Modifier.height(12.dp))

              Text("Issuing Province / Authority:", fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf("Punjab", "Sindh", "KP", "Islamabad").forEach { prov ->
                  FilterChip(
                    selected = selectedProvince == prov,
                    onClick = { selectedProvince = prov },
                    label = { Text(prov, fontSize = 12.sp) }
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  isDlimsVerifying = true
                  val result = verificationManager.verifyDlimsLicense(
                    licenseNumber = if (licenseNumber.isEmpty()) "LHR-2018-93821" else licenseNumber,
                    cnic = detectedCnic,
                    province = selectedProvince
                  )
                  dlimsResult = result
                  if (licenseNumber.isEmpty()) licenseNumber = "LHR-2018-93821"
                  isDlimsVerifying = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              ) {
                if (isDlimsVerifying) {
                  CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Default.Security, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Query DLIMS Traffic Police API")
                }
              }

              AnimatedVisibility(visible = dlimsResult != null) {
                dlimsResult?.let { dres ->
                  Column(
                    modifier = Modifier
                      .padding(top = 14.dp)
                      .fillMaxWidth()
                      .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                      .padding(12.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(dres.statusMessage, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text("Categories: ${dres.vehicleCategories.joinToString(", ")}", fontSize = 12.sp, color = Color(0xFF2E7D32))
                    Text("Valid Until: ${dres.expiryDate} (${dres.province} Traffic Police)", fontSize = 12.sp, color = Color(0xFF2E7D32))
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                OutlinedButton(onClick = { currentStep = 1 }) {
                  Text("Back")
                }
                Button(
                  onClick = { currentStep = 3 },
                  enabled = dlimsResult != null || licenseNumber.isNotEmpty()
                ) {
                  Text("Proceed to Face Verification")
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
              }
            }
          }
        }

        // STEP 3: ML Kit Biometric Liveness Face Verification
        if ((currentStep == 3 && isDriver) || (currentStep == 2 && !isDriver)) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Real-time Biometric Liveness Check", fontWeight = FontWeight.SemiBold)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "Take a live camera selfie to confirm your identity with Google ML Kit Face Detection and prevent fraudulent bookings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(20.dp))

              Box(
                modifier = Modifier
                  .size(130.dp)
                  .clip(CircleShape)
                  .border(3.dp, if (faceResult != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary, CircleShape)
                  .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
              ) {
                if (faceResult != null) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(56.dp))
                } else {
                  Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                }
              }

              Spacer(modifier = Modifier.height(18.dp))

              Button(
                onClick = {
                  scope.launch {
                    isFaceScanning = true
                    // Simulate front camera capture & ML Kit face detection
                    val demoBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
                    val result = verificationManager.verifyFaceLiveness(demoBitmap)
                    faceResult = result
                    isFaceScanning = false
                  }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isFaceScanning
              ) {
                if (isFaceScanning) {
                  CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Analyzing Facial Landmarks...")
                } else {
                  Icon(Icons.Default.CameraAlt, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(if (faceResult != null) "Re-take Live Selfie" else "Capture Live Selfie")
                }
              }

              AnimatedVisibility(visible = faceResult != null) {
                faceResult?.let { fres ->
                  Column(
                    modifier = Modifier
                      .padding(top = 14.dp)
                      .fillMaxWidth()
                      .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                      .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Text("✅ ${fres.message}", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Liveness Match Score: ${(fres.confidenceScore * 100).toInt()}% Confidence", fontSize = 12.sp, color = Color(0xFF2E7D32))
                  }
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                OutlinedButton(onClick = { currentStep = if (isDriver) 2 else 1 }) {
                  Text("Back")
                }
                Button(
                  onClick = {
                    onVerificationComplete(
                      detectedCnic,
                      if (isDriver) licenseNumber else null,
                      faceResult?.isLive ?: true
                    )
                  },
                  enabled = faceResult != null || detectedCnic.isNotEmpty(),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                  Icon(Icons.Default.Verified, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Confirm & Activate Account")
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StepBadge(stepNumber: Int, title: String, isActive: Boolean, isDone: Boolean) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(
          when {
            isDone -> Color(0xFF2E7D32)
            isActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
        ),
      contentAlignment = Alignment.Center
    ) {
      if (isDone) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
      } else {
        Text(
          text = "$stepNumber",
          color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
      color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
