package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MessagesScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val messages by viewModel.chatMessages.collectAsState()
  var inputQuery by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val quickPrompts = listOf(
    "Civic RS Turbo Rates?",
    "Fortuner Legender for Murree?",
    "Wedding Audi A6 Decor?",
    "Chauffeur Food & Night Policy?",
    "HiAce 14 Seater to Gorakh?"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
  ) {
    // Header
    Surface(
      color = Color.White,
      modifier = Modifier.fillMaxWidth(),
      shadowElevation = 1.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(NavyPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SmartToy,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "PAK E DRIVE AI Assistant",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(StatusGreen)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Online • Instant Quotation",
                fontSize = 11.sp,
                color = StatusGreen,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // WhatsApp Direct Button
        Button(
          onClick = {
            val url = "https://wa.me/923152292493?text=Assalam-o-Alaikum%20PAK%20E%20DRIVE,%20I%20need%20car%20rental%20rates."
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try { context.startActivity(intent) } catch (_: Exception) {}
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Chat Messages List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        ChatBubbleItem(message = msg)
      }
    }

    // Quick Prompts Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .background(Color.White)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      quickPrompts.forEach { prompt ->
        SuggestionChip(
          onClick = {
            viewModel.sendChatMessage(prompt)
          },
          label = {
            Text(text = prompt, fontSize = 11.sp, color = NavyPrimary)
          },
          colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFEFF3FA)),
          border = SuggestionChipDefaults.suggestionChipBorder(enabled = false)
        )
      }
    }

    // Message Input Bar (CRITICAL: User Text is strictly BLACK Color(0xFF0A0F1D) on white background)
    Surface(
      color = Color.White,
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 80.dp), // Clear bottom nav
      shadowElevation = 4.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = inputQuery,
          onValueChange = { inputQuery = it },
          placeholder = {
            Text("Ask about car rates, routes, or wedding cars...", color = TextSecondaryMuted, fontSize = 12.sp)
          },
          singleLine = true,
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF0A0F1D),
            unfocusedTextColor = Color(0xFF0A0F1D),
            cursorColor = NavyPrimary,
            focusedContainerColor = Color(0xFFF7F9FC),
            unfocusedContainerColor = Color(0xFFF7F9FC),
            focusedBorderColor = NavyPrimary,
            unfocusedBorderColor = BorderStroke
          ),
          modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            if (inputQuery.isNotBlank()) {
              viewModel.sendChatMessage(inputQuery.trim())
              inputQuery = ""
            }
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (inputQuery.isNotBlank()) OrangeAccent else NavyPrimary)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send message",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ChatBubbleItem(message: ChatMessage) {
  val isUser = message.sender == "user"

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .background(NavyPrimary)
          .align(Alignment.Bottom),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Card(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 2.dp,
        bottomEnd = if (isUser) 2.dp else 16.dp
      ),
      colors = CardDefaults.cardColors(
        containerColor = if (isUser) NavyPrimary else Color.White
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier
        .widthIn(max = 280.dp)
        .border(
          width = 1.dp,
          color = if (isUser) Color.Transparent else BorderStroke,
          shape = RoundedCornerShape(16.dp)
        )
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
          text = message.text,
          color = if (isUser) Color.White else Color(0xFF0A0F1D),
          fontSize = 13.sp,
          lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = message.timestamp,
          color = if (isUser) Color.White.copy(alpha = 0.6f) else TextSecondaryMuted,
          fontSize = 9.sp,
          modifier = Modifier.align(Alignment.End)
        )
      }
    }
  }
}
