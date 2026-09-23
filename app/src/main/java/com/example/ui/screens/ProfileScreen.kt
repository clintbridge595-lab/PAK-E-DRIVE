package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.AppLanguage
import com.example.util.CnicValidator

@Composable
fun ProfileScreen(
  viewModel: MainViewModel,
  onOpenLogin: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val profile by viewModel.userProfile.collectAsState()
  val bookings by viewModel.bookings.collectAsState()
  val currentLanguage by viewModel.currentLanguage.collectAsState()

  var isEditing by remember { mutableStateOf(false) }
  var editName by remember(profile) { mutableStateOf(profile.name) }
  var editPhone by remember(profile) { mutableStateOf(profile.phone) }
  var editEmail by remember(profile) { mutableStateOf(profile.email) }
  var editCity by remember(profile) { mutableStateOf(profile.city) }
  var editAddress by remember(profile) { mutableStateOf(profile.address) }
  var editCnic by remember(profile) { mutableStateOf(profile.cnic) }

  var showDeleteConfirmDialog by remember { mutableStateOf(false) }
  var showLogoutConfirmDialog by remember { mutableStateOf(false) }
  var deletionSuccessMessage by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
      .verticalScroll(rememberScrollState())
      .padding(bottom = 90.dp)
  ) {
    // Top Profile Header (Realistic Off-White, Compact Identity)
    Surface(
      color = Color(0xFFF8F9FA),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F172A))
            .border(1.5.dp, Color(0xFFCBD5E1), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = profile.name.take(2).uppercase().ifBlank { "PD" },
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (profile.isLoggedIn) profile.name else "Guest User",
          color = Color(0xFF111827),
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = if (profile.isLoggedIn) profile.phone else "Sign in to manage bookings",
          color = Color(0xFF4B5563),
          fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (profile.isLoggedIn) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE6F4EA)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF137333), modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (profile.accountType == "DRIVER" || profile.isDriverPartner)
                  "Verified Driver Partner • DLIMS & NADRA Active"
                else
                  "Verified Client / Passenger Account • ${bookings.size} Bookings",
                color = Color(0xFF137333),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        } else {
          Button(
            onClick = onOpenLogin,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Login / Register via OTP", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Success Banner on Delete or Action
    deletionSuccessMessage?.let { msg ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatusGreenLight),
        shape = RoundedCornerShape(8.dp)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = msg, color = StatusGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    // Personal Information Section
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Personal Profile",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )

          if (!isEditing) {
            TextButton(
              onClick = { isEditing = true },
              colors = ButtonDefaults.textButtonColors(contentColor = OrangeAccent)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isEditing) {
          ProfileInfoRow(
            icon = Icons.Default.AccountCircle,
            label = "Account Type",
            value = if (profile.accountType == "DRIVER" || profile.isDriverPartner) "Driver Partner & Car Owner" else "Client & Passenger Account"
          )
          ProfileInfoRow(icon = Icons.Default.Person, label = "Full Name", value = profile.name)
          ProfileInfoRow(icon = Icons.Default.Phone, label = "Phone", value = profile.phone)
          ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = profile.email)
          ProfileInfoRow(icon = Icons.Default.Badge, label = "NADRA CNIC", value = if (profile.cnic.isNotBlank()) profile.cnic else "Not Provided")
          if (profile.accountType == "DRIVER" || profile.isDriverPartner) {
            ProfileInfoRow(
              icon = Icons.Default.DriveEta,
              label = "DLIMS Driving License",
              value = if (profile.driverLicenseNumber.isNotBlank()) "${profile.driverLicenseNumber} (Verified)" else "Required for fleet driving"
            )
            ProfileInfoRow(
              icon = Icons.Default.Security,
              label = "License Authority",
              value = profile.licenseIssuingAuthority.ifBlank { "DLIMS Traffic Police Pakistan" }
            )
          }
          ProfileInfoRow(icon = Icons.Default.LocationCity, label = "City", value = profile.city)
          ProfileInfoRow(icon = Icons.Default.Home, label = "Pickup Address", value = profile.address)
        } else {
          // Edit Fields - CRITICAL: All typed text is BLACK Color(0xFF0A0F1D) on white
          OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Full Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0A0F1D),
              unfocusedTextColor = Color(0xFF0A0F1D),
              cursorColor = NavyPrimary,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = BorderStroke
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editPhone,
            onValueChange = { editPhone = it },
            label = { Text("Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0A0F1D),
              unfocusedTextColor = Color(0xFF0A0F1D),
              cursorColor = NavyPrimary,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = BorderStroke
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editEmail,
            onValueChange = { editEmail = it },
            label = { Text("Email Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0A0F1D),
              unfocusedTextColor = Color(0xFF0A0F1D),
              cursorColor = NavyPrimary,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = BorderStroke
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editCnic,
            onValueChange = { editCnic = CnicValidator.formatCnic(it) },
            label = { Text("NADRA CNIC (13 Digits)") },
            placeholder = { Text("42101-1234567-1") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0A0F1D),
              unfocusedTextColor = Color(0xFF0A0F1D),
              cursorColor = NavyPrimary,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = BorderStroke
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editCity,
            onValueChange = { editCity = it },
            label = { Text("Default City") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0A0F1D),
              unfocusedTextColor = Color(0xFF0A0F1D),
              cursorColor = NavyPrimary,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = BorderStroke
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editAddress,
            onValueChange = { editAddress = it },
            label = { Text("Pickup Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color(0xFF0A0F1D),
              unfocusedTextColor = Color(0xFF0A0F1D),
              cursorColor = NavyPrimary,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = BorderStroke
            )
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            OutlinedButton(
              onClick = { isEditing = false },
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = {
                viewModel.updateProfile(editName, editPhone, editEmail, editCity, editAddress, editCnic)
                isEditing = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Driver Partner Portal Card (Realistic Off-White)
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Driver & Fleet Partner Portal",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "NADRA & DLIMS verified driver network across Pakistan",
            fontSize = 11.5.sp,
            color = Color(0xFF4B5563)
          )
        }

        Button(
          onClick = { viewModel.setShowDriverPartnerDialog(true) },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Open Portal", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Support & Central Helpline Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "24/7 Car Rental Support",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Call Helpline 1
        SupportActionRow(
          icon = Icons.Default.Phone,
          title = "UAN Helpline 1",
          subtitle = "0315-2292493 (Call / WhatsApp)",
          onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:03152292493"))
            try { context.startActivity(intent) } catch (_: Exception) {}
          }
        )

        HorizontalDivider(color = BorderStroke, modifier = Modifier.padding(vertical = 8.dp))

        // Call Helpline 2
        SupportActionRow(
          icon = Icons.Default.HeadsetMic,
          title = "UAN Helpline 2",
          subtitle = "0315-2398490 (Customer Care)",
          onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:03152398490"))
            try { context.startActivity(intent) } catch (_: Exception) {}
          }
        )

        HorizontalDivider(color = BorderStroke, modifier = Modifier.padding(vertical = 8.dp))

        // WhatsApp Direct
        SupportActionRow(
          icon = Icons.AutoMirrored.Filled.Chat,
          title = "WhatsApp Fleet Manager",
          subtitle = "+92 315 2292493",
          onClick = {
            val url = "https://wa.me/923152292493"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try { context.startActivity(intent) } catch (_: Exception) {}
          }
        )

        HorizontalDivider(color = BorderStroke, modifier = Modifier.padding(vertical = 8.dp))

        // FAQ & Support Center Dialog
        SupportActionRow(
          icon = Icons.AutoMirrored.Filled.HelpOutline,
          title = "Rental FAQs & Policy Guide",
          subtitle = "Chauffeur rules, fuel policy & cancellation terms",
          onClick = {
            viewModel.setShowFaqSupport(true)
          }
        )
      }
    }

    // App Preferences & Admin Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "App Settings",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Language toggle row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Translate, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Language", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
              Text(
                text = "English (Active)",
                fontSize = 11.sp,
                color = TextSecondaryMuted
              )
            }
          }

          Surface(
            color = NavyPrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = "English",
              color = NavyPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }

        HorizontalDivider(color = BorderStroke, modifier = Modifier.padding(vertical = 10.dp))

        // Admin Portal
        SupportActionRow(
          icon = Icons.Default.AdminPanelSettings,
          title = "Admin Portal",
          subtitle = "Internal system management & diagnostics",
          onClick = {
            viewModel.setShowBuildLogAnalyzer(true)
          }
        )
      }
    }

    // Account Management (Logout & Delete Account)
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Account Security & Privacy",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Logout
        if (profile.isLoggedIn) {
          OutlinedButton(
            onClick = { showLogoutConfirmDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
          ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out of App")
          }

          Spacer(modifier = Modifier.height(10.dp))
        }

        // Real Working Delete Account Button (As requested by user)
        Button(
          onClick = { showDeleteConfirmDialog = true },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = StatusRedLight, contentColor = StatusRed)
        ) {
          Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusRed, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Delete Account & Clear Data", color = StatusRed, fontWeight = FontWeight.Bold)
        }
      }
    }
  }

  // Delete Account Dialog Confirmation
  if (showDeleteConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Delete Account?")
        }
      },
      text = {
        Text(
          "Are you sure you want to delete your PAK E DRIVE account? This will permanently wipe all your active and past booking records, profile preferences, and trip receipts from this device.",
          fontSize = 13.sp,
          color = TextPrimaryDark
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteAccount()
            showDeleteConfirmDialog = false
            deletionSuccessMessage = "Your account and all booking data have been successfully deleted."
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
        ) {
          Text("Yes, Delete Everything", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Logout Dialog Confirmation
  if (showLogoutConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirmDialog = false },
      title = { Text("Log Out?") },
      text = { Text("You will need to verify your phone number via OTP when you log in again.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.logout()
            showLogoutConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
          Text("Log Out", color = Color.White)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ProfileInfoRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(Color(0xFFF1F4F9)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = label, fontSize = 11.sp, color = TextSecondaryMuted)
      Text(
        text = value.ifBlank { "Not provided" },
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimaryDark
      )
    }
  }
}

@Composable
fun SupportActionRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(StatusGreenLight),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
      Text(text = subtitle, fontSize = 11.sp, color = TextSecondaryMuted)
    }
    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondaryMuted, modifier = Modifier.size(18.dp))
  }
}
