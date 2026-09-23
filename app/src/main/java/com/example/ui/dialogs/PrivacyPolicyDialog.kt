package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PakGreen

@Composable
fun PrivacyPolicyDialog(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.85f)
        .padding(vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // Top Header
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Privacy Policy & Rental Terms",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Google Play Compliant • PAK E DRIVE Pakistan",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.5.sp
              )
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
              )
            }
          }
        }

        // Scrollable Body
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
        ) {
          PolicySection(
            title = "1. Introduction & Enterprise Overview",
            icon = Icons.Default.Policy,
            content = "PAK E DRIVE (Rent A Car Pakistan) provides chauffeur-driven and luxury car rental, intercity drop-off, and wapsi car services across Karachi, Lahore, Islamabad, Rawalpindi, and all major cities of Pakistan. We are committed to protecting user privacy, identity data, and ensuring fair, transparent rental operations."
          )

          PolicySection(
            title = "2. User Data Collected & Purpose",
            icon = Icons.Default.VerifiedUser,
            content = "To confirm bookings and meet safety regulations under Pakistani transport law, we collect:\n" +
                "• Full Name & Contact Phone Number: For ride confirmations, driver assignment, and trip dispatch.\n" +
                "• CNIC (Computerized National Identity Card): Stored locally and encrypted solely for vehicle security verification with NADRA standards.\n" +
                "• Approximate/Fine Location: Used when tracking driver pickup progress or finding nearby available fleet vehicles.\n" +
                "• Trip Booking History: Maintained locally on your device in offline Room Database."
          )

          PolicySection(
            title = "3. Zero Third-Party Sharing",
            icon = Icons.Default.Security,
            content = "We NEVER sell, trade, or share your personal data, CNIC numbers, or contact phone numbers with third-party advertisers or data brokers. All credentials remain encrypted."
          )

          PolicySection(
            title = "4. Vehicle Rental Rules & Chauffeur Policy",
            icon = Icons.Default.Policy,
            content = "• Chauffeur Duty: All rentals include verified professional chauffeurs with valid driving licenses.\n" +
                "• Fuel & Toll Taxes: Intercity motorway tolls and outstation driver allowances are calculated clearly before trip departure without hidden surcharges.\n" +
                "• Advance & Escrow: Bookings are reserved with standard token advance payable via JazzCash, EasyPaisa, Raast, or direct bank transfer."
          )

          PolicySection(
            title = "5. User Rights & Data Deletion",
            icon = Icons.Default.VerifiedUser,
            content = "Under Google Play developer guidelines, you have the right to request deletion of your account and trip logs at any time. Simply contact our support desk or delete your profile within the app settings."
          )

          PolicySection(
            title = "6. Official Helpline & Contact",
            icon = Icons.Default.Security,
            content = "PAK E DRIVE Official Helpline: +92 315 2292493\n" +
                "Support Email: support@pakedrive.com\n" +
                "Head Office: Main Shahrah-e-Faisal / Airport Road, Karachi, Pakistan."
          )

          Spacer(modifier = Modifier.height(12.dp))

          // WhatsApp Support Button
          Button(
            onClick = {
              val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/923152292493?text=Assalam-o-Alaikum%20PAK%20E%20DRIVE!%20I%20have%20a%20question%20regarding%20Privacy%20Policy%20or%20Rental%20Terms.")
              )
              try { context.startActivity(intent) } catch (_: Exception) {}
            },
            colors = ButtonDefaults.buttonColors(containerColor = PakGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Contact 24/7 Legal & Support Desk",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
          }
        }

        // Bottom Dismiss Bar
        Surface(
          tonalElevation = 2.dp,
          color = Color(0xFFF9FAFB),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 12.dp)
          ) {
            Button(
              onClick = onDismiss,
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("I Understand & Accept", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PolicySection(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  content: String
) {
  Column(modifier = Modifier.padding(bottom = 16.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = NavyPrimary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1F2937)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = content,
      fontSize = 12.5.sp,
      lineHeight = 18.sp,
      color = Color(0xFF4B5563)
    )
  }
}
