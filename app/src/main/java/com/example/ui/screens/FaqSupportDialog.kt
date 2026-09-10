package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.LocaleStrings

data class FaqItem(
  val id: Int,
  val questionEn: String,
  val questionUr: String,
  val answerEn: String,
  val answerUr: String,
  val category: String = "General"
)

@Composable
fun FaqSupportDialog(
  currentLanguage: AppLanguage,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var expandedFaqId by remember { mutableStateOf<Int?>(1) }

  val faqs = remember {
    listOf(
      FaqItem(
        id = 1,
        questionEn = "What is a 'Wapsi Car' and why are the rates so low?",
        questionUr = "واپسی کار کیا ہے اور اس کے ریٹس کم کیوں ہیں؟",
        answerEn = "Wapsi cars are premium vehicles returning back to their base city (e.g., Karachi to Hyderabad, Lahore to Faisalabad). Since the car is returning empty anyway, PAK E DRIVE offers up to 40% lump sum discounts on that route.",
        answerUr = "واپسی کار سے مراد وہ گاڑیاں ہیں جو اپنے اصل شہر واپس جا رہی ہوتی ہیں (مثلاً کراچی سے حیدرآباد یا لاہور سے فیصل آباد)۔ چونکہ گاڑی نے واپس جانا ہوتا ہے، اس لیے ہم اس روٹ پر 40 فیصد تک رعایت دیتے ہیں۔"
      ),
      FaqItem(
        id = 2,
        questionEn = "Is a professional chauffeur included with all cars?",
        questionUr = "کیا تمام گاڑیوں کے ساتھ ڈرائیور فراہم کیا جاتا ہے؟",
        answerEn = "Yes! All PAK E DRIVE rentals are 100% chauffeur-driven by licensed, verified, polite drivers who have extensive knowledge of Pakistani motorways and city routes.",
        answerUr = "جی ہاں! پاک ای ڈرائیو کی تمام گاڑیاں تجربہ کار اور تصدیق شدہ ڈرائیور کے ساتھ فراہم کی جاتی ہیں جنہیں تمام موٹرویز اور شہروں کے راستوں کا مکمل تجربہ ہوتا ہے۔"
      ),
      FaqItem(
        id = 3,
        questionEn = "What are the fuel and motorway toll tax rules?",
        questionUr = "پیٹرول اور موٹروے ٹول ٹیکس کا کیا طریقہ کار ہے؟",
        answerEn = "For daily rentals, fuel and M-Tag motorway toll taxes are paid by the customer based on actual usage. For 'Lump Sum Wapsi Deals', tolls can be pre-included as indicated in the booking summary.",
        answerUr = "روزانہ کے کرائے میں پیٹرول اور ایم ٹیگ ٹول ٹیکس کسٹمر خود ادا کرتے ہیں۔ جبکہ یکمشت واپسی ڈیلز میں ٹول ٹیکس کرائے میں شامل ہوتا ہے۔"
      ),
      FaqItem(
        id = 4,
        questionEn = "What documents are required for booking?",
        questionUr = "گاڑی بک کرنے کے لیے کن دستاویزات کی ضرورت ہوتی ہے؟",
        answerEn = "A valid Pakistani CNIC (or Passport for overseas Pakistanis) and an active mobile number for live driver coordinates and booking confirmation.",
        answerUr = "صرف ایک اصلی شناختی کارڈ (یا اوورسیز پاکستانیوں کے لیے پاسپورٹ) اور ایک موبائل نمبر ڈرائیور کی رابطہ کاری کے لیے درکار ہے۔"
      ),
      FaqItem(
        id = 5,
        questionEn = "Can I cancel or reschedule my booking?",
        questionUr = "کیا میں اپنی بکنگ منسوخ یا تبدیل کر سکتا ہوں؟",
        answerEn = "Yes! Bookings can be cancelled or rescheduled free of charge up to 4 hours before the scheduled departure time directly inside the Bookings tab or via WhatsApp.",
        answerUr = "جی ہاں! بکنگ روانگی کے وقت سے 4 گھنٹے پہلے تک بکنگز اسکرین یا واٹس ایپ کے ذریعے بغیر کسی فیس کے منسوخ یا ری شیڈول کی جا سکتی ہے۔"
      ),
      FaqItem(
        id = 6,
        questionEn = "Do you provide luxury cars for weddings and VIP protocol?",
        questionUr = "کیا شادی بیاہ اور وی آئی پی پروٹوکول کے لیے لگژری گاڑیاں دستیاب ہیں؟",
        answerEn = "Yes, we provide decorated Audi A6, Toyota Prado, and Land Cruiser V8 with suited chauffeurs for weddings, corporate executives, and delegations.",
        answerUr = "جی بالکل، شادی بیاہ اور وی آئی پی پروٹوکول کے لیے آڈی اے 6، ٹویوٹا پراڈو اور وی 8 لینڈ کروزر مع سجے ہوئے پھول اور ڈرائیور دستیاب ہیں۔"
      ),
      FaqItem(
        id = 7,
        questionEn = "Are tours to Northern Areas (Swat, Hunza, Skardu) supported?",
        questionUr = "کیا شمالی علاقہ جات (سوات، ہنزہ، سکردو) کے ٹورز دستیاب ہیں؟",
        answerEn = "Yes! Our Toyota Fortuner 4x4, Prado, and HiAce Grand Cabin are fully equipped for high-altitude mountain journeys with specialized hill drivers.",
        answerUr = "جی ہاں! ہماری ٹویوٹا فارچونر 4x4، پراڈو اور ہائی ایس گرینڈ کیبن شمالی علاقہ جات کے لیے خصوصی پہاڑی ڈرائیورز کے ساتھ مکمل تیار ہیں۔"
      )
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.9f)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(18.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = LocaleStrings.get("faq_support", currentLanguage),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "PAK E DRIVE 24/7 Help Desk",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Quick Direct Contact Buttons
          item {
            Text(
              text = if (currentLanguage == AppLanguage.URDU) "فوری کسٹمر سپورٹ چینلز" else "Quick Customer Care Channels",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // WhatsApp button
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF25D366),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  val url = "https://wa.me/923152292493?text=${Uri.encode("Assalam-o-Alaikum PAK E DRIVE! I need assistance with car rental booking.")}"
                  try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
                }
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text("WhatsApp Official Support", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text("+92 315 2292493 (Instant Fleet Manager)", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                }
                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phone Helplines Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Helpline 1
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:03152292493"))) } catch (_: Exception) {}
                  }
              ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Phone, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text("Helpline 1", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                    Text("0315-2292493", fontSize = 11.sp, color = TextSecondaryMuted)
                  }
                }
              }

              // Helpline 2
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:03152398490"))) } catch (_: Exception) {}
                  }
              ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text("Helpline 2", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                    Text("0315-2398490", fontSize = 11.sp, color = TextSecondaryMuted)
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email Row
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFF1F5F9),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@pakedrive.pk")).apply {
                    putExtra(Intent.EXTRA_SUBJECT, "Customer Inquiry - PAK E DRIVE")
                  }
                  try { context.startActivity(intent) } catch (_: Exception) {}
                }
            ) {
              Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text("Email Support", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                  Text("support@pakedrive.pk (Response within 2 hours)", fontSize = 11.sp, color = TextSecondaryMuted)
                }
              }
            }
          }

          // FAQ Accordion Title
          item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (currentLanguage == AppLanguage.URDU) "اکثر پوچھے جانے والے سوالات (FAQ)" else "Frequently Asked Questions",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
          }

          // FAQ Items
          items(faqs, key = { it.id }) { faq ->
            val isExpanded = expandedFaqId == faq.id
            val question = if (currentLanguage == AppLanguage.URDU) faq.questionUr else faq.questionEn
            val answer = if (currentLanguage == AppLanguage.URDU) faq.answerUr else faq.answerEn

            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = if (isExpanded) Color(0xFFF8FAFC) else Color.White),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isExpanded) NavyPrimary.copy(alpha = 0.4f) else BorderStroke, RoundedCornerShape(12.dp))
                .clickable {
                  expandedFaqId = if (isExpanded) null else faq.id
                }
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${faq.id}. $question",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpanded) NavyPrimary else TextPrimaryDark,
                    modifier = Modifier.weight(1f)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                }

                AnimatedVisibility(
                  visible = isExpanded,
                  enter = fadeIn() + expandVertically(),
                  exit = fadeOut() + shrinkVertically()
                ) {
                  Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = BorderStroke, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = answer,
                      fontSize = 12.sp,
                      color = TextSecondaryMuted,
                      lineHeight = 18.sp
                    )
                  }
                }
              }
            }
          }

          // Office Locations Card
          item {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (currentLanguage == AppLanguage.URDU) "مرکزی دفاتر کے پتے" else "Regional Head Offices",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF15803D)
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "📍 Karachi: Main Shahrah-e-Faisal / DHA Phase 6\n📍 Islamabad: G-8 Markaz & Islamabad International Airport\n📍 Lahore: DHA Phase 5 Commercial & Allama Iqbal Airport",
                  fontSize = 11.sp,
                  color = Color(0xFF166534),
                  lineHeight = 16.sp
                )
              }
            }
          }
        }
      }
    }
  }
}
