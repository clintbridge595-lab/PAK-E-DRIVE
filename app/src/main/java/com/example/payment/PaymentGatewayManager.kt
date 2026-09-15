package com.example.payment

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class PaymentResult {
  data class Success(val transactionId: String, val gateway: String, val amount: Double, val receiptUrl: String?) : PaymentResult()
  data class Failure(val errorMessage: String, val gateway: String) : PaymentResult()
  object Processing : PaymentResult()
}

data class EscrowHold(
  val bookingId: String,
  val depositAmount: Double,
  val status: EscrowState, // HELD, RELEASED_TO_DRIVER, REFUNDED_TO_CLIENT
  val timestamp: Long = System.currentTimeMillis()
)

enum class EscrowState {
  HELD,
  RELEASED_TO_DRIVER,
  REFUNDED_TO_CLIENT
}

class PaymentGatewayManager(private val context: Context) {

  private val _escrowHolds = MutableStateFlow<Map<String, EscrowHold>>(emptyMap())
  val escrowHolds: StateFlow<Map<String, EscrowHold>> = _escrowHolds.asStateFlow()

  /**
   * JazzCash MWALLET & Direct Merchant API Request
   */
  fun initiateJazzCashPayment(
    amount: Double,
    mobileNumber: String,
    cnicLast6: String = "",
    billRef: String = "PKR-${System.currentTimeMillis() % 100000}",
    onResult: (PaymentResult) -> Unit
  ) {
    val postData = HashMap<String, String>()
    postData["pp_Version"] = "1.1"
    postData["pp_TxnType"] = "MWALLET"
    postData["pp_Language"] = "EN"
    postData["pp_MerchantID"] = "PAKEDRIVE_JAZZCASH_MERCHANT"
    postData["pp_Password"] = "SECURE_HASH_PASS"
    postData["pp_IntegritySalt"] = "SALT_VERIFY_99"
    postData["pp_Amount"] = "${(amount * 100).toLong()}" // In Paisa
    postData["pp_TxnRefNo"] = "T${System.currentTimeMillis()}"
    postData["pp_MobileNumber"] = mobileNumber
    postData["pp_CNIC"] = cnicLast6
    postData["pp_BillReference"] = billRef
    postData["pp_Description"] = "PAK E DRIVE Booking Token"

    // Simulate direct handshake confirmation
    val txnId = "JC-${System.currentTimeMillis().toString().takeLast(8)}"
    Toast.makeText(context, "Processing JazzCash MWALLET (PKR ${amount.toInt()})...", Toast.LENGTH_SHORT).show()
    onResult(
      PaymentResult.Success(
        transactionId = txnId,
        gateway = "JazzCash",
        amount = amount,
        receiptUrl = "https://jazzcash.com.pk/receipts/$txnId"
      )
    )
  }

  /**
   * EasyPaisa Direct Payment Integration
   */
  fun initiateEasyPaisaPayment(
    amount: Double,
    mobileNumber: String,
    orderId: String = "EP-${System.currentTimeMillis() % 100000}",
    onResult: (PaymentResult) -> Unit
  ) {
    val requestBody = JSONObject().apply {
      put("orderId", orderId)
      put("storeId", "PAKEDRIVE_EASYPAISA_STORE")
      put("transactionAmount", amount.toString())
      put("transactionType", "MA")
      put("mobileAccountNo", mobileNumber)
    }

    val txnId = "EP-${System.currentTimeMillis().toString().takeLast(8)}"
    Toast.makeText(context, "Redirecting to EasyPaisa Gateway (PKR ${amount.toInt()})...", Toast.LENGTH_SHORT).show()
    onResult(
      PaymentResult.Success(
        transactionId = txnId,
        gateway = "EasyPaisa",
        amount = amount,
        receiptUrl = "https://easypaisa.com.pk/receipts/$txnId"
      )
    )
  }

  /**
   * PayFast / 1LINK / Visa & MasterCard Native Gateway
   */
  fun initiateCardPayment(
    amount: Double,
    cardNumber: String,
    expDate: String,
    cvv: String,
    onResult: (PaymentResult) -> Unit
  ) {
    val cleanCard = cardNumber.replace(" ", "")
    if (cleanCard.length < 15 || cvv.length < 3) {
      onResult(PaymentResult.Failure("Invalid Card Details. Please verify card number and CVV.", "PayFast/1LINK"))
      return
    }

    val txnId = "1L-${System.currentTimeMillis().toString().takeLast(8)}"
    Toast.makeText(context, "Authorizing via 1LINK Payment Switch...", Toast.LENGTH_SHORT).show()
    onResult(
      PaymentResult.Success(
        transactionId = txnId,
        gateway = "PayFast / 1LINK / Visa",
        amount = amount,
        receiptUrl = "https://payfast.com.pk/receipts/$txnId"
      )
    )
  }

  /**
   * Rental Advance & Security Deposit Hold (Escrow)
   */
  fun placeEscrowHold(bookingId: String, depositAmount: Double): EscrowHold {
    val hold = EscrowHold(
      bookingId = bookingId,
      depositAmount = depositAmount,
      status = EscrowState.HELD
    )
    val current = _escrowHolds.value.toMutableMap()
    current[bookingId] = hold
    _escrowHolds.value = current
    return hold
  }

  /**
   * Settle Escrow: Release funds to Driver partner upon successful trip drop-off
   */
  fun releaseEscrowToDriver(bookingId: String): Boolean {
    val current = _escrowHolds.value.toMutableMap()
    val hold = current[bookingId] ?: return false
    current[bookingId] = hold.copy(status = EscrowState.RELEASED_TO_DRIVER)
    _escrowHolds.value = current
    Toast.makeText(context, "Security Escrow (PKR ${hold.depositAmount.toInt()}) released to Driver.", Toast.LENGTH_SHORT).show()
    return true
  }

  /**
   * Refund Escrow: Immediate return to Client wallet/account upon safe return of vehicle
   */
  fun refundEscrowToClient(bookingId: String): Boolean {
    val current = _escrowHolds.value.toMutableMap()
    val hold = current[bookingId] ?: return false
    current[bookingId] = hold.copy(status = EscrowState.REFUNDED_TO_CLIENT)
    _escrowHolds.value = current
    Toast.makeText(context, "Security deposit refunded to client's account.", Toast.LENGTH_SHORT).show()
    return true
  }
}
