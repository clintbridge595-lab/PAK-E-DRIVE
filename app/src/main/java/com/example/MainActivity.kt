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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BuildLogAnalyzerDialog
import com.example.ui.components.PakEDriveTopBar
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
        var showSplash by remember { mutableStateOf(true) }
        val userProfile by viewModel.userProfile.collectAsState()

        if (showSplash) {
          SplashScreen(onTimeout = { showSplash = false })
        } else {
          PakEDriveApp(viewModel = viewModel)
        }
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
  val showNotifications by viewModel.showNotifications.collectAsState()
  val showFaqSupport by viewModel.showFaqSupport.collectAsState()
  val showBuildLogAnalyzer by viewModel.showBuildLogAnalyzer.collectAsState()
  val reviewBooking by viewModel.reviewBooking.collectAsState()
  val reviews by viewModel.allReviews.collectAsState()
  val currentLanguage by viewModel.currentLanguage.collectAsState()
  val unreadCount by viewModel.unreadNotificationCount.collectAsState()
  var showCitySelector by remember { mutableStateOf(false) }

  // Network state monitoring
  val networkMonitor = remember { NetworkMonitor(context) }
  val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
  val snackbarHostState = remember { SnackbarHostState() }
  var wasOffline by remember { mutableStateOf(false) }

  LaunchedEffect(isOnline) {
    if (!isOnline) {
      wasOffline = true
      snackbarHostState.showSnackbar(
        message = "⚠️ No Internet Connection — Please check mobile data or Wi-Fi to book cars & view live rates.",
        duration = SnackbarDuration.Indefinite,
        actionLabel = "Dismiss"
      )
    } else if (wasOffline) {
      wasOffline = false
      snackbarHostState.showSnackbar(
        message = "✅ Back Online — Internet connection restored.",
        duration = SnackbarDuration.Short
      )
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.width(310.dp)
      ) {
        // Drawer Header
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .statusBarsPadding()
            .padding(20.dp)
        ) {
          Column {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(4.dp),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.pakedrive_logo),
                contentDescription = "PAK E DRIVE Logo",
                modifier = Modifier.fillMaxSize()
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = userProfile.name,
              color = Color.White,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )

            Text(
              text = userProfile.phone,
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF25D366)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Verified Member", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
          icon = { Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366)) },
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
          icon = { Icon(Icons.Default.Login, contentDescription = null, tint = OrangeAccent) },
          label = { Text("Switch / Login Account", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OrangeAccent) },
          selected = false,
          onClick = {
            coroutineScope.launch { drawerState.close() }
            viewModel.setShowAuthDialog(true)
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
      topBar = {
        Column {
          PakEDriveTopBar(
            selectedTab = selectedTab,
            currentCity = selectedCity,
            userName = userProfile.name.split(" ").firstOrNull() ?: "Mehdi",
            unreadCount = unreadCount,
            onNotificationsClick = { viewModel.setShowNotifications(true) },
            onCityClick = { showCitySelector = true }
          )
          if (!isOnline) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFDC2626))
                .padding(horizontal = 16.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                Icons.Default.WifiOff,
                contentDescription = "Offline",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "No Internet Connection • Offline Mode",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
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
}
