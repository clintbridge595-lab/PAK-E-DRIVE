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

@Composable
fun ProfileScreen(
  viewModel: MainViewModel,
  onOpenLogin: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val profile by viewModel.userProfile.collectAsState()
  val bookings by viewModel.bookings.collectAsState()

  var isEditing by remember { mutableStateOf(false) }
  var editName by remember(profile) { mutableStateOf(profile.name) }
  var editPhone by remember(profile) { mutableStateOf(profile.phone) }
  var editEmail by remember(profile) { mutableStateOf(profile.email) }
  var editCity by remember(profile) { mutableStateOf(profile.city) }
  var editAddress by remember(profile) { mutableStateOf(profile.address) }

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
    // Top Profile Header
    Surface(
      color = NavyPrimary,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, OrangeAccent, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = profile.name.take(2).uppercase().ifBlank { "HC" },
            color = NavyPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = if (profile.isLoggedIn) profile.name else "Guest User",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = if (profile.isLoggedIn) profile.phone else "Sign in to manage bookings",
          color = Color.White.copy(alpha = 0.8f),
          fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (profile.isLoggedIn) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.15f)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Verified Customer • ${bookings.size} Trips",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        } else {
          Button(
            onClick = onOpenLogin,
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
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
          ProfileInfoRow(icon = Icons.Default.Person, label = "Full Name", value = profile.name)
          ProfileInfoRow(icon = Icons.Default.Phone, label = "Phone", value = profile.phone)
          ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = profile.email)
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
                viewModel.updateProfile(editName, editPhone, editEmail, editCity, editAddress)
                isEditing = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
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
          icon = Icons.Default.Chat,
          title = "WhatsApp Fleet Manager",
          subtitle = "+92 315 2292493",
          onClick = {
            val url = "https://wa.me/923152292493"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try { context.startActivity(intent) } catch (_: Exception) {}
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
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
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
          "Are you sure you want to delete your Hat Cab account? This will permanently wipe all your active and past booking records, profile preferences, and trip receipts from this device.",
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
