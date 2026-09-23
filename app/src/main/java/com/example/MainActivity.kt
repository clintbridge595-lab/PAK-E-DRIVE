package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pomo.mypomo.R
import com.example.data.repository.CrashLogRepository
import com.example.ui.components.AiCodeFixDrawer
import com.example.ui.components.BuildLogAnalyzerDialog
import com.example.ui.components.NetworkTimeoutErrorDialog
import com.example.ui.components.PakEDriveTopBar
import androidx.compose.material.icons.automirrored.filled.Chat
import com.example.ui.dialogs.AboutAppDialog
import com.example.ui.dialogs.PrivacyPolicyDialog
import com.example.ui.dialogs.CnicAndBiometricVerificationDialog
import com.example.ui.dialogs.PakistaniPaymentDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.util.NetworkMonitor
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()
  private val authViewModel: AuthViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        PakEDriveApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun PakEDriveApp(viewModel: MainViewModel) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  val selectedTab by viewModel.selectedTab.collectAsState()
  val selectedCity by viewModel.selectedCity.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val detailCar by viewModel.detailCar.collectAsState()
  val bookingCar by viewModel.bookingCar.collectAsState()
  val confirmedBooking by viewModel.confirmedBooking.collectAsState()
  val showAuthDialog by viewModel.showAuthDialog.collectAsState()
  val showDriverPartnerDialog by viewModel.showDriverPartnerDialog.collectAsState()
  val showVerificationDialog by viewModel.showVerificationDialog.collectAsState()
  val showLiveTracking by viewModel.showLiveTracking.collectAsState()
  val showPaymentDialog by viewModel.showPaymentDialog.collectAsState()
  val showTimeoutError by viewModel.showTimeoutError.collectAsState()
  val showNotifications by viewModel.showNotifications.collectAsState()
  val showFaqSupport by viewModel.showFaqSupport.collectAsState()
  val showBuildLogAnalyzer by viewModel.showBuildLogAnalyzer.collectAsState()
  val reviewBooking by viewModel.reviewBooking.collectAsState()
  val reviews by viewModel.allReviews.collectAsState()
  val currentLanguage by viewModel.currentLanguage.collectAsState()
  val unreadCount by viewModel.unreadNotificationCount.collectAsState()
  var showCitySelector by remember { mutableStateOf(false) }
  var showAiDrawer by remember { mutableStateOf(false) }
  var showPrivacyPolicy by remember { mutableStateOf(false) }
  var showAboutApp by remember { mutableStateOf(false) }

  // Crash Log & Offline Sync
  val crashLogRepo = remember { CrashLogRepository.getInstance(context) }
  val unsyncedCrashCount by crashLogRepo.getUnsyncedCount().collectAsState(initial = 0)

  val snackbarHostState = remember { SnackbarHostState() }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.width(310.dp)
      ) {
        // Drawer Header (Realistic Off-White, Compact Brand Identity)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
            .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)))
            .statusBarsPadding()
            .padding(16.dp)
        ) {
          Column {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                .padding(3.dp),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.pakedrive_logo),
                contentDescription = "PAK E DRIVE Logo",
                modifier = Modifier.fillMaxSize()
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = userProfile.name,
              color = Color(0xFF111827),
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Bold
            )

            Text(
              text = if (userProfile.phone.isNotBlank()) userProfile.phone else "+92 315 2292493",
              color = Color(0xFF6B7280),
              fontSize = 11.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFE6F4EA)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF137333), modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (userProfile.accountType == "DRIVER" || userProfile.isDriverPartner)
                    "Driver Partner (DLIMS & NADRA)"
                  else
                    "Client Account (NADRA Verified)",
                  color = Color(0xFF137333),
                  fontSize = 9.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Items
        NavigationDrawerItem(
          icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = NavyPrimary) },
          label = { Text("Home / Wapsi Cars", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
          selected = selectedTab == 0,
          onClick = {
            viewModel.setTab(0)
            coroutineScope.launch { drawerState.close() }
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.TimeToLeave, contentDescription = null, tint = NavyPrimary) },
          label = { Text("All Fleet Vehicles", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
          selected = selectedTab == 2,
          onClick = {
            viewModel.setTab(2)
            coroutineScope.launch { drawerState.close() }
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = NavyPrimary) },
          label = { Text("My Bookings", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
          selected = selectedTab == 1,
          onClick = {
            viewModel.setTab(1)
            coroutineScope.launch { drawerState.close() }
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color(0xFF25D366)) },
          label = { Text("WhatsApp Helpline", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
          selected = false,
          onClick = {
            coroutineScope.launch { drawerState.close() }
            val intent = Intent(
              Intent.ACTION_VIEW,
              Uri.parse("https://wa.me/923152292493?text=Assalam-o-Alaikum%20PAK%20E%20DRIVE!%20I%20need%20assistance.")
            )
            try { context.startActivity(intent) } catch (_: Exception) {}
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
          label = { Text("My Profile & Security", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
          selected = selectedTab == 4,
          onClick = {
            viewModel.setTab(4)
            coroutineScope.launch { drawerState.close() }
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE5E7EB))

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.DriveEta, contentDescription = null, tint = Color(0xFF111827)) },
          label = { Text("Driver Partner Portal (List Car)", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF111827)) },
          selected = false,
          onClick = {
            coroutineScope.launch { drawerState.close() }
            viewModel.setShowDriverPartnerDialog(true)
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF111827)) },
          label = { Text("Client Sign Up / Sign In", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF111827)) },
          selected = false,
          onClick = {
            coroutineScope.launch { drawerState.close() }
            viewModel.setShowAuthDialog(true)
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFF1E88E5)) },
          label = { Text("Live GPS Ride Tracking", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF111827)) },
          selected = false,
          onClick = {
            coroutineScope.launch { drawerState.close() }
            viewModel.setLiveTrackingVisible(true)
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF2E7D32)) },
          label = { Text("NADRA & DLIMS Verification", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF111827)) },
          selected = false,
          onClick = {
            coroutineScope.launch { drawerState.close() }
            viewModel.setVerificationDialogVisible(true)
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFD97706)) },
          label = { Text("Advance Payment & Escrow", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF111827)) },
          selected = false,
          onClick = {
            coroutineScope.launch { drawerState.close() }
            viewModel.setPaymentDialogCar(viewModel.fleet.firstOrNull())
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.Policy, contentDescription = null, tint = NavyPrimary) },
          label = { Text("Privacy Policy & Terms", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF111827)) },
          selected = false,
          onClick = {
            coroutineScope.launch { drawerState.close() }
            showPrivacyPolicy = true
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        NavigationDrawerItem(
          icon = { Icon(Icons.Default.Info, contentDescription = null, tint = NavyPrimary) },
          label = { Text("About PAK E DRIVE", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color(0xFF111827)) },
          selected = false,
          onClick = {
            coroutineScope.launch { drawerState.close() }
            showAboutApp = true
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Drawer Footer
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text("PAK E DRIVE - Rent A Car", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
          Text("Helpline: +92 315 2292493 • Karachi, Pakistan", fontSize = 11.sp, color = Color(0xFF9CA3AF))
          Text("Version 2.4.0 (Enterprise Fleet)", fontSize = 10.sp, color = Color(0xFF9CA3AF))
        }
      }
    }
  ) {
    Scaffold(
      snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
      },
      floatingActionButton = {
        FloatingActionButton(
          onClick = {
            val intent = Intent(
              Intent.ACTION_VIEW,
              Uri.parse("https://wa.me/923152292493?text=Assalam-o-Alaikum%20PAK%20E%20DRIVE!%20I%20want%20to%20inquire%20about%20car%20rental.")
            )
            try { context.startActivity(intent) } catch (_: Exception) {}
          },
          containerColor = Color(0xFF25D366),
          contentColor = Color.White,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.testTag("fab_whatsapp_support")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Chat,
              contentDescription = "WhatsApp Helpline",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "WhatsApp",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color.White
            )
          }
        }
      },
      floatingActionButtonPosition = FabPosition.End,
      topBar = {
        PakEDriveTopBar(
          selectedTab = selectedTab,
          currentCity = selectedCity,
          userName = userProfile.name.split(" ").firstOrNull()?.ifBlank { "Mehdi" } ?: "Mehdi",
          unreadCount = unreadCount,
          onNotificationsClick = { viewModel.setShowNotifications(true) },
          onCityClick = { showCitySelector = true }
        )
      },
      bottomBar = {
        NavigationBar(
          containerColor = Color.White,
          tonalElevation = 4.dp,
          modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
          val navItems = listOf(
            Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
            Triple("My Bookings", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
            Triple("Search", Icons.Filled.Search, Icons.Outlined.Search),
            Triple("Messages", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
            Triple("Profile", Icons.Filled.Person, Icons.Outlined.Person)
          )

          navItems.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            NavigationBarItem(
              selected = isSelected,
              onClick = {
                viewModel.setTab(index)
                PakEDriveApplication.logUserEngagement("tab_selected", item.first)
              },
              icon = {
                Icon(
                  imageVector = if (isSelected) item.second else item.third,
                  contentDescription = item.first,
                  tint = if (isSelected) Color(0xFF132238) else Color(0xFF6B7280)
                )
              },
              label = {
                Text(
                  text = item.first,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color(0xFF132238) else Color(0xFF6B7280)
                )
              },
              colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFFE2E8F0)
              )
            )
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (selectedTab) {
          0 -> HomeScreen(
            viewModel = viewModel,
            onOpenBooking = { car -> viewModel.startBookingFlow(car) },
            onOpenDetail = { car -> viewModel.openCarDetail(car) },
            onOpenCitySelector = { showCitySelector = true }
          )
          1 -> BookingsScreen(
            viewModel = viewModel,
            onBrowseCarsClick = { viewModel.setTab(2) }
          )
          2 -> SearchScreen(
            viewModel = viewModel,
            onOpenBooking = { car -> viewModel.startBookingFlow(car) },
            onOpenDetail = { car -> viewModel.openCarDetail(car) }
          )
          3 -> MessagesScreen(viewModel = viewModel)
          4 -> ProfileScreen(
            viewModel = viewModel,
            onOpenLogin = { viewModel.setShowAuthDialog(true) }
          )
        }
      }
    }
  }

  // Dialogs & Modals
  detailCar?.let { car ->
    VehicleDetailDialog(
      car = car,
      reviews = reviews,
      onDismiss = { viewModel.closeCarDetail() },
      onBookNow = {
        viewModel.closeCarDetail()
        viewModel.startBookingFlow(car)
      }
    )
  }

  bookingCar?.let { car ->
    BookingFlowDialog(
      car = car,
      viewModel = viewModel,
      onDismiss = { viewModel.closeBookingFlow() }
    )
  }

  confirmedBooking?.let { booking ->
    BookingSuccessDialog(
      booking = booking,
      onDismiss = { viewModel.closeConfirmedModal() },
      onGoToBookings = {
        viewModel.closeConfirmedModal()
        viewModel.setTab(1)
      }
    )
  }

  if (showAuthDialog) {
    AuthDialog(
      viewModel = viewModel,
      onDismiss = { viewModel.setShowAuthDialog(false) }
    )
  }

  if (showDriverPartnerDialog) {
    DriverPartnerDialog(
      viewModel = viewModel,
      onDismiss = { viewModel.setShowDriverPartnerDialog(false) }
    )
  }

  if (showNotifications) {
    NotificationsDialog(
      notifications = notifications,
      currentLanguage = currentLanguage,
      onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
      onClearAll = { viewModel.clearAllNotifications() },
      onDeleteNotification = { id -> viewModel.deleteNotification(id) },
      onDismiss = { viewModel.setShowNotifications(false) }
    )
  }

  if (showFaqSupport) {
    FaqSupportDialog(
      currentLanguage = currentLanguage,
      onDismiss = { viewModel.setShowFaqSupport(false) }
    )
  }

  if (showBuildLogAnalyzer) {
    BuildLogAnalyzerDialog(
      onDismiss = { viewModel.setShowBuildLogAnalyzer(false) }
    )
  }

  reviewBooking?.let { booking ->
    ReviewDialog(
      booking = booking,
      currentLanguage = currentLanguage,
      onDismiss = { viewModel.closeReviewDialog() },
      onSubmitReview = { carRating, driverRating, comment ->
        viewModel.submitReview(carRating, driverRating, comment, booking)
      }
    )
  }

  if (showCitySelector) {
    CitySelectionDialog(
      cities = viewModel.cities,
      selectedCity = selectedCity,
      onCitySelected = { city -> viewModel.selectCity(city) },
      onDismiss = { showCitySelector = false }
    )
  }

  if (showLiveTracking) {
    GoogleLiveTrackingScreen(
      onBack = { viewModel.setLiveTrackingVisible(false) }
    )
  }

  if (showVerificationDialog) {
    CnicAndBiometricVerificationDialog(
      isDriver = userProfile.accountType == "DRIVER",
      onDismiss = { viewModel.setVerificationDialogVisible(false) },
      onVerificationComplete = { cnic, license, isFaceVerified ->
        viewModel.completeFullIdentityVerification(cnic, license, isFaceVerified)
      }
    )
  }

  showPaymentDialog?.let { car ->
    PakistaniPaymentDialog(
      bookingFare = car.dailyRate.toDouble(),
      carName = car.name,
      onDismiss = { viewModel.setPaymentDialogCar(null) },
      onPaymentSuccess = { txnId, gateway, amount ->
        viewModel.onPaymentCompleted(txnId, gateway, amount, car)
      }
    )
  }

  if (showTimeoutError) {
    NetworkTimeoutErrorDialog(
      errorCode = "504 Gateway Timeout",
      errorMessage = "PAK E DRIVE High-Speed Cloud Server connection timed out. Android WorkManager offline engine will synchronize queued bookings automatically.",
      onRetry = {
        viewModel.setTimeoutErrorVisible(false)
      },
      onDismiss = {
        viewModel.setTimeoutErrorVisible(false)
      }
    )
  }

  if (showAiDrawer) {
    AiCodeFixDrawer(
      onDismiss = { showAiDrawer = false }
    )
  }

  if (showPrivacyPolicy) {
    PrivacyPolicyDialog(
      onDismiss = { showPrivacyPolicy = false }
    )
  }

  if (showAboutApp) {
    AboutAppDialog(
      onDismiss = { showAboutApp = false }
    )
  }
}
