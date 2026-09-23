package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pomo.mypomo.R
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PakGreen

@Composable
fun AboutAppDialog(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp)
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
          }
        }

        Image(
          painter = painterResource(id = R.drawable.pakedrive_logo),
          contentDescription = "PAK E DRIVE Logo",
          modifier = Modifier
            .size(72.dp)
            .padding(4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "PAK E DRIVE",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = NavyPrimary
        )

        Text(
          text = "Rent A Car Pakistan • Chauffeur & Wapsi Cars",
          fontSize = 12.sp,
          color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFE8F5E9)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = PakGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Google Play Production Release v2.4.0",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = PakGreen
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Pakistan's premier chauffeur and car rental enterprise serving Karachi, Lahore, Islamabad, Rawalpindi, Peshawar, Multan, and all cities. Premium bulletproof B6+ SUVs, economy sedans, luxury wedding protocol, and intercity wapsi rates.",
          fontSize = 12.5.sp,
          color = Color(0xFF374151),
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+923152292493"))
            try { context.startActivity(intent) } catch (_: Exception) {}
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Call Helpline (+92 315 2292493)", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
