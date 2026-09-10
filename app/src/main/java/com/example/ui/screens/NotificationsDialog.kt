package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNotification
import com.example.util.AppLanguage
import com.example.util.LocaleStrings

@Composable
fun NotificationsDialog(
  notifications: List<AppNotification>,
  currentLanguage: AppLanguage = AppLanguage.ENGLISH,
  onMarkAllRead: () -> Unit = {},
  onClearAll: () -> Unit = {},
  onDeleteNotification: (String) -> Unit = {},
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.75f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = NavyPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = LocaleStrings.get("alerts_notifications", currentLanguage),
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryMuted)
          }
        }

        // Action buttons bar: Mark all read & Clear all
        if (notifications.isNotEmpty()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextButton(
              onClick = onMarkAllRead,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
              Spacer(modifier = Modifier.width(4.dp))
              Text(LocaleStrings.get("mark_all_read", currentLanguage), fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
            }

            TextButton(
              onClick = onClearAll,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondaryMuted)
              Spacer(modifier = Modifier.width(4.dp))
              Text(LocaleStrings.get("clear_all", currentLanguage), fontSize = 11.sp, color = TextSecondaryMuted)
            }
          }
        } else {
          Spacer(modifier = Modifier.height(12.dp))
        }

        if (notifications.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = TextSecondaryMuted)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(LocaleStrings.get("no_alerts", currentLanguage), color = TextSecondaryMuted, fontSize = 13.sp)
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(notifications, key = { it.id }) { notif ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (!notif.isRead) Color(0xFFEFF6FF) else Color(0xFFF7F9FC),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.Top
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(if (!notif.isRead) NavyPrimary.copy(alpha = 0.15f) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      Icons.Default.Info,
                      contentDescription = null,
                      tint = if (!notif.isRead) NavyPrimary else TextSecondaryMuted,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(text = notif.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                      if (!notif.isRead) {
                        Box(
                          modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary)
                        )
                      }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = notif.message, fontSize = 11.sp, color = TextSecondaryMuted, lineHeight = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(text = notif.time, fontSize = 9.sp, color = OrangeAccent, fontWeight = FontWeight.Medium)
                      IconButton(
                        onClick = { onDeleteNotification(notif.id) },
                        modifier = Modifier.size(20.dp)
                      ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                      }
                    }
                  }
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
fun CitySelectionDialog(
  cities: List<String>,
  selectedCity: String,
  onCitySelected: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var filterText by remember { mutableStateOf("") }
  val displayedCities = remember(cities, filterText) {
    if (filterText.isBlank()) cities
    else cities.filter { it.contains(filterText, ignoreCase = true) }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.8f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Select City", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
          IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF6B7280))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search city field
        OutlinedTextField(
          value = filterText,
          onValueChange = { filterText = it },
          placeholder = { Text("Search 48+ Pakistani cities...", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            cursorColor = NavyPrimary,
            focusedContainerColor = Color(0xFFF9FAFB),
            unfocusedContainerColor = Color(0xFFF9FAFB),
            focusedBorderColor = NavyPrimary,
            unfocusedBorderColor = Color(0xFFE5E7EB)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(displayedCities) { city ->
            val isCurrent = city == selectedCity
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCurrent) Color(0xFFEFF6FF) else Color.Transparent)
                .clickable {
                  onCitySelected(city)
                  onDismiss()
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = city,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) NavyPrimary else Color(0xFF111827)
              )
              if (isCurrent) {
                Icon(Icons.Default.Check, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }
}
