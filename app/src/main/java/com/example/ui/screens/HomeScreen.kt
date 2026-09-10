package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Car
import com.example.ui.components.CarCard
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

data class ShowcaseVehicle(
  val title: String,
  val imageRes: Int,
  val targetCarId: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  onOpenBooking: (Car) -> Unit,
  onOpenDetail: (Car) -> Unit,
  onOpenCitySelector: () -> Unit,
  modifier: Modifier = Modifier
) {
  val fleet = viewModel.fleet
  val selectedCity by viewModel.selectedCity.collectAsState()

  // Showcase vehicles for the top horizontal carousel - cleanly centered, Coil loaded, auto-cycling
  // Prominently features the certified B6+ bulletproof lineup along with client favorites
  val showcaseList = remember {
    listOf(
      ShowcaseVehicle("BULLET PROOF LANDCRUISER V8 B6+", R.drawable.car_landcruiser_v8, "bp_landcruiser_b6"),
      ShowcaseVehicle("BULLET PROOF RIVO B6+", R.drawable.car_revo_b6, "bp_revo_b6"),
      ShowcaseVehicle("BULLET PROOF FORTUNER B6+", R.drawable.car_fortuner, "bp_fortuner_b6"),
      ShowcaseVehicle("BULLET PROOF PRADO B6+", R.drawable.car_toyota_prado, "bp_prado_b6"),
      ShowcaseVehicle("Changan Oshan X7", R.drawable.car_changan_x7_black, "car_oshan_x7_black"),
      ShowcaseVehicle("Toyota Fortuner", R.drawable.car_fortuner, "car_fortuner_legender"),
      ShowcaseVehicle("Toyota Corolla Altis", R.drawable.car_corolla_altis, "wapsi_corolla_16"),
      ShowcaseVehicle("Toyota Hilux Revo", R.drawable.car_revo_b6, "car_hilux_revo"),
      ShowcaseVehicle("Honda Civic", R.drawable.car_civic_turbo, "wapsi_civic_15"),
      ShowcaseVehicle("Suzuki Alto", R.drawable.car_suzuki_alto, "wapsi_alto_multan"),
      ShowcaseVehicle("Toyota Yaris", R.drawable.car_toyota_yaris, "wapsi_corolla_16")
    )
  }

  val pagerState = rememberPagerState(pageCount = { showcaseList.size })

  // Smoother automatic horizontal scroll using LaunchedEffect timer with gentle transition speed
  LaunchedEffect(pagerState) {
    while (true) {
      delay(3800) // Comfortable reading pause for users browsing the fleet
      if (!pagerState.isScrollInProgress) {
        val next = (pagerState.currentPage + 1) % showcaseList.size
        pagerState.animateScrollToPage(
          page = next,
          animationSpec = tween(
            durationMillis = 850,
            easing = FastOutSlowInEasing
          )
        )
      }
    }
  }

  // Filter cars based on selected city (or all cars)
  val displayedCars = remember(fleet, selectedCity) {
    if (selectedCity == "All Cities") {
      fleet
    } else {
      fleet.filter { car ->
        car.fromCity.equals(selectedCity, ignoreCase = true) ||
          car.toCity.equals(selectedCity, ignoreCase = true) ||
          car.routeSnippet.contains(selectedCity, ignoreCase = true)
      }.ifEmpty { fleet }
    }
  }

  LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF9FAFB)),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {

    // 1. ALL CITIES SELECTOR DROPDOWN (matching video 00:26)
    item(span = { GridItemSpan(2) }) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onOpenCitySelector)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationCity,
              contentDescription = null,
              tint = Color(0xFF132238),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = selectedCity,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF111827)
            )
          }

          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Select City",
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // 2. "BOOK WITH US!" BANNER ROW (matching video 00:01)
    item(span = { GridItemSpan(2) }) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Book with us!",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF111827)
        )

        Button(
          onClick = {
            val firstCar = fleet.firstOrNull()
            if (firstCar != null) onOpenBooking(firstCar)
          },
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132238)),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          modifier = Modifier.height(36.dp)
        ) {
          Text(
            text = "Book Now",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // 3. HORIZONTALLY SCROLLING SHOWCASE CAROUSEL (uniform aspect ratio, natural look, high-res Coil rendering)
    item(span = { GridItemSpan(2) }) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White, RoundedCornerShape(14.dp))
          .padding(top = 14.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Vehicle Pager with uniform 16:9 aspect ratio container
        HorizontalPager(
          state = pagerState,
          modifier = Modifier.fillMaxWidth()
        ) { page ->
          val item = showcaseList[page]
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(16f / 9f)
              .clickable {
                val target = fleet.firstOrNull { it.id == item.targetCarId } ?: fleet.first()
                onOpenDetail(target)
              }
              .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = item.imageRes,
              contentDescription = item.title,
              contentScale = ContentScale.Fit, // Natural look, maintains authentic vehicle proportions without cropping or distortion
              modifier = Modifier.fillMaxSize()
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Centered Vehicle Name in clean bold typography
        val activeItem = showcaseList[pagerState.currentPage]
        Text(
          text = activeItem.title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF111827),
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Clean Dot Pagination Indicators
        Row(
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(showcaseList.size.coerceAtMost(8)) { index ->
            val isCurrent = pagerState.currentPage % 8 == index
            Box(
              modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(if (isCurrent) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(if (isCurrent) Color(0xFF132238) else Color(0xFFD1D5DB))
            )
          }
        }
      }
    }

    // 4. SECTION HEADER: "Wapsi Cars" (matching video 00:03)
    item(span = { GridItemSpan(2) }) {
      Text(
        text = "Wapsi Cars",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF111827),
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
      )
    }

    // 5. 2-COLUMN VEHICLE GRID (matching video 00:04 - 00:06 & 00:19 - 00:24)
    if (displayedCars.isEmpty()) {
      item(span = { GridItemSpan(2) }) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = null,
              tint = Color(0xFF9CA3AF),
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No cars found for $selectedCity",
              fontSize = 14.sp,
              color = Color(0xFF6B7280)
            )
          }
        }
      }
    } else {
      items(displayedCars, key = { it.id }) { car ->
        CarCard(
          car = car,
          onViewDetails = { onOpenDetail(car) },
          onBookNow = { onOpenBooking(car) }
        )
      }
    }

    // Bottom spacing padding
    item(span = { GridItemSpan(2) }) {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}
