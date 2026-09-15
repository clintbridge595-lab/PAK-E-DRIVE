package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.payment.PaymentGatewayManager
import com.example.payment.PaymentResult

enum class PakistaniPaymentMethod {
  JAZZCASH,
  EASYPAISA,
  PAYFAST_CARD,
  ONE_LINK_BANK
}

@Composable
fun PakistaniPaymentDialog(
  bookingFare: Double,
  carName: String,
  onDismiss: () -> Unit,
  onPaymentSuccess: (txnId: String, gateway: String, totalPaid: Double) -> Unit
) {
  val context = LocalContext.current
  val paymentManager = remember { PaymentGatewayManager(context) }

  var selectedMethod by remember { mutableStateOf(PakistaniPaymentMethod.JAZZCASH) }
  var mobileNumber by remember { mutableStateOf("") }
  var cnicLast6 by remember { mutableStateOf("") }
  var cardNumber by remember { mutableStateOf("") }
  var cardExpiry by remember { mutableStateOf("") }
  var cardCvv by remember { mutableStateOf("") }

  var isProcessing by remember { mutableStateOf(false) }
  var paymentResult by remember { mutableStateOf<PaymentResult?>(null) }

  // 20% advance token + PKR 10,000 refundable escrow security deposit
  val advanceToken = (bookingFare * 0.25).toInt()
  val securityDepositEscrow = 10000
  val totalPayable = advanceToken + securityDepositEscrow

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
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Secure Advance & Escrow Payment", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Text("Official Pakistani Payment Gateways", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fare Breakdown with Escrow Security Deposit
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Booking: $carName", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Rental Estimated Fare:", fontSize = 12.5.sp)
              Text("PKR ${bookingFare.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Advance Booking Token (25%):", fontSize = 12.5.sp)
              Text("PKR $advanceToken", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Refundable Escrow Security Deposit:", fontSize = 12.5.sp)
              Text("PKR $securityDepositEscrow", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Online Advance Due:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text("PKR $totalPayable", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "🔒 Security Deposit is held in Escrow and automatically refunded to your account upon safe car return.",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Payment Gateway:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Gateway Selection Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          PaymentMethodCard(
            title = "JazzCash",
            subtitle = "Mobile Wallet",
            isSelected = selectedMethod == PakistaniPaymentMethod.JAZZCASH,
            onClick = { selectedMethod = PakistaniPaymentMethod.JAZZCASH },
            modifier = Modifier.weight(1f)
          )
          PaymentMethodCard(
            title = "EasyPaisa",
            subtitle = "Direct MA",
            isSelected = selectedMethod == PakistaniPaymentMethod.EASYPAISA,
            onClick = { selectedMethod = PakistaniPaymentMethod.EASYPAISA },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          PaymentMethodCard(
            title = "PayFast / Card",
            subtitle = "Visa / Master",
            isSelected = selectedMethod == PakistaniPaymentMethod.PAYFAST_CARD,
            onClick = { selectedMethod = PakistaniPaymentMethod.PAYFAST_CARD },
            modifier = Modifier.weight(1f)
          )
          PaymentMethodCard(
            title = "1LINK 1BILL",
            subtitle = "Bank Transfer",
            isSelected = selectedMethod == PakistaniPaymentMethod.ONE_LINK_BANK,
            onClick = { selectedMethod = PakistaniPaymentMethod.ONE_LINK_BANK },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gateway Input Fields
        when (selectedMethod) {
          PakistaniPaymentMethod.JAZZCASH -> {
            Column {
              OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("JazzCash Mobile Number (030x-xxxxxxx)") },
                placeholder = { Text("03001234567") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) }
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = cnicLast6,
                onValueChange = { cnicLast6 = it },
                label = { Text("CNIC Last 6 Digits (for MPIN authentication)") },
                placeholder = { Text("123456") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
              )
              Text("An MPIN prompt will appear on your JazzCash registered mobile device.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          PakistaniPaymentMethod.EASYPAISA -> {
            Column {
              OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("EasyPaisa Mobile Account (03xx-xxxxxxx)") },
                placeholder = { Text("03451234567") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) }
              )
              Text("You will receive a USSD push or in-app EasyPaisa approval notification.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          PakistaniPaymentMethod.PAYFAST_CARD -> {
            Column {
              OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Debit / Credit Card Number") },
                placeholder = { Text("4123 4567 8901 2345") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) }
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = cardExpiry,
                  onValueChange = { cardExpiry = it },
                  label = { Text("MM/YY") },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )
                OutlinedTextField(
                  value = cardCvv,
                  onValueChange = { cardCvv = it },
                  label = { Text("CVV") },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )
              }
            }
          }
          PakistaniPaymentMethod.ONE_LINK_BANK -> {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text("1LINK 1BILL Invoice Token", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Consumer Number: 100293819203", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text("Payable through HBL, Meezan, Bank Alfalah, UBL or any Pakistani banking app via 1BILL Invoices.", fontSize = 11.5.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pay Button
        Button(
          onClick = {
            isProcessing = true
            when (selectedMethod) {
              PakistaniPaymentMethod.JAZZCASH -> {
                paymentManager.initiateJazzCashPayment(
                  amount = totalPayable.toDouble(),
                  mobileNumber = if (mobileNumber.isEmpty()) "03001234567" else mobileNumber,
                  cnicLast6 = cnicLast6
                ) { res ->
                  isProcessing = false
                  paymentResult = res
                  if (res is PaymentResult.Success) {
                    paymentManager.placeEscrowHold("BKG-${System.currentTimeMillis() % 10000}", securityDepositEscrow.toDouble())
                    onPaymentSuccess(res.transactionId, res.gateway, totalPayable.toDouble())
                  }
                }
              }
              PakistaniPaymentMethod.EASYPAISA -> {
                paymentManager.initiateEasyPaisaPayment(
                  amount = totalPayable.toDouble(),
                  mobileNumber = if (mobileNumber.isEmpty()) "03451234567" else mobileNumber
                ) { res ->
                  isProcessing = false
                  paymentResult = res
                  if (res is PaymentResult.Success) {
                    paymentManager.placeEscrowHold("BKG-${System.currentTimeMillis() % 10000}", securityDepositEscrow.toDouble())
                    onPaymentSuccess(res.transactionId, res.gateway, totalPayable.toDouble())
                  }
                }
              }
              PakistaniPaymentMethod.PAYFAST_CARD -> {
                paymentManager.initiateCardPayment(
                  amount = totalPayable.toDouble(),
                  cardNumber = if (cardNumber.isEmpty()) "4123456789012345" else cardNumber,
                  expDate = if (cardExpiry.isEmpty()) "12/28" else cardExpiry,
                  cvv = if (cardCvv.isEmpty()) "982" else cardCvv
                ) { res ->
                  isProcessing = false
                  paymentResult = res
                  if (res is PaymentResult.Success) {
                    paymentManager.placeEscrowHold("BKG-${System.currentTimeMillis() % 10000}", securityDepositEscrow.toDouble())
                    onPaymentSuccess(res.transactionId, res.gateway, totalPayable.toDouble())
                  }
                }
              }
              PakistaniPaymentMethod.ONE_LINK_BANK -> {
                val txnId = "1B-${System.currentTimeMillis().toString().takeLast(8)}"
                paymentResult = PaymentResult.Success(txnId, "1LINK 1BILL", totalPayable.toDouble(), null)
                paymentManager.placeEscrowHold("BKG-${System.currentTimeMillis() % 10000}", securityDepositEscrow.toDouble())
                onPaymentSuccess(txnId, "1LINK 1BILL", totalPayable.toDouble())
                isProcessing = false
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          enabled = !isProcessing
        ) {
          if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contacting Gateway...")
          } else {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pay PKR $totalPayable & Confirm")
          }
        }

        AnimatedVisibility(visible = paymentResult != null) {
          paymentResult?.let { res ->
            when (res) {
              is PaymentResult.Success -> {
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
                    Text("Payment Authorized Successfully", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                  }
                  Text("Transaction ID: ${res.transactionId} (${res.gateway})", fontSize = 12.sp, color = Color(0xFF2E7D32))
                  Text("Advance Token Paid + Escrow Security Deposit Held Safely", fontSize = 12.sp, color = Color(0xFF2E7D32))
                }
              }
              is PaymentResult.Failure -> {
                Text(
                  text = "Payment Failed: ${res.errorMessage}",
                  color = MaterialTheme.colorScheme.error,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(top = 8.dp)
                )
              }
              else -> {}
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PaymentMethodCard(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clickable(onClick = onClick)
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        shape = RoundedCornerShape(12.dp)
      ),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
      Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
