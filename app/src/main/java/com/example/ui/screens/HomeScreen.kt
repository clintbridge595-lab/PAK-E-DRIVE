package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Car
import com.example.ui.components.CarCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

data class ShowcaseCar(
  val title: String,
  val category: String,
  val price: String,
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
  val context = LocalContext.current
  val fleet = viewModel.fleet
  val selectedCity by viewModel.selectedCity.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()

  // 3D Showcase cars for the top interactive swiper
  val showcaseCars = remember {
    listOf(
      ShowcaseCar("Toyota Yaris ATIV", "Sedan", "Rs. 7000/day", R.drawable.img_car_yaris_studio, "car_yaris_ativ"),
      ShowcaseCar("Honda Civic RS", "Sedan", "Rs. 8000/day", R.drawable.img_car_civic_white, "wapsi_civic_15"),
      ShowcaseCar("Toyota Corolla Altis", "Sedan", "Rs. 9000/day", R.drawable.img_car_corolla_white, "wapsi_corolla_16"),
      ShowcaseCar("Toyota Fortuner Legender", "4x4 SUV", "Rs. 18000/day", R.drawable.img_car_fortuner_white, "car_fortuner_legender"),
      ShowcaseCar("Changan Oshan X7", "7-Seater", "Rs. 14000/day", R.drawable.car_oshan_x7, "car_oshan_x7")
    )
  }

  val pagerState = rememberPagerState(pageCount = { showcaseCars.size })

  val filteredFleet = remember(fleet, selectedCity, selectedCategory, searchQuery) {
    fleet.filter { car ->
      val matchesCity = selectedCity == "All Cities" ||
        car.fromCity.equals(selectedCity, ignoreCase = true) ||
        car.toCity.equals(selectedCity, ignoreCase = true) ||
        car.routeSnippet.contains(selectedCity, ignoreCase = true)

      val matchesCategory = selectedCategory == "All" || car.category.equals(selectedCategory, ignoreCase = true)

      val matchesQuery = searchQuery.isBlank() ||
        car.name.contains(searchQuery, ignoreCase = true) ||
        car.variant.contains(searchQuery, ignoreCase = true) ||
        car.make.contains(searchQuery, ignoreCase = true) ||
        car.routeSnippet.contains(searchQuery, ignoreCase = true)

      matchesCity && matchesCategory && matchesQuery
    }
  }

  val categories = listOf("All", "Sedan", "SUV", "Van", "Hatchback", "Luxury Wedding")

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF9FAFB)),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {

    // 1. TOP 3D CAR SHOWCASE SLIDER (Swipable Interactive Studio Showcase)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White)
          .padding(top = 10.dp, bottom = 14.dp)
      ) {
        HorizontalPager(
          state = pagerState,
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
        ) { page ->
          val item = showcaseCars[page]
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight()
              .clickable {
                val found = fleet.firstOrNull { it.id == item.targetCarId } ?: fleet.first()
                onOpenDetail(found)
              }
              .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = item.imageRes),
              contentDescription = item.title,
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize()
            )
          }
        }

        // Dots Indicator
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(showcaseCars.size) { index ->
            val isSelected = pagerState.currentPage == index
            Box(
              modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(if (isSelected) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(if (isSelected) NavyPrimary else Color(0xFFD1D5DB))
            )
          }
        }

        // Active Car Title & Price
        val currentCar = showcaseCars[pagerState.currentPage]
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = currentCar.title,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF111827)
            )
            Text(
              text = currentCar.category,
              fontSize = 12.sp,
              color = Color(0xFF6B7280)
            )
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF3F4F6)
          ) {
            Text(
              text = currentCar.price,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }
      }

      HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
    }

    // 2. CITY SELECTOR & SEARCH BAR ROW
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // City Selector Dropdown Button (as seen in video)
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .clickable(onClick = onOpenCitySelector)
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = OrangeAccent,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = selectedCity,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF111827),
                  maxLines = 1
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

          // WhatsApp Direct Button
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF25D366),
            modifier = Modifier
              .height(46.dp)
              .clickable {
                val intent = Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse("https://wa.me/923152292493?text=Assalam-o-Alaikum%20Hat%20Cab!%20I%20want%20to%20inquire%20about%20car%20rental.")
                )
                try { context.startActivity(intent) } catch (_: Exception) {}
              }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Input Field
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = {
            Text("Search Civic, Corolla, APV, Fortuner, Alto...", color = Color(0xFF9CA3AF), fontSize = 13.sp)
          },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            cursorColor = NavyPrimary,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = NavyPrimary,
            unfocusedBorderColor = Color(0xFFE5E7EB)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
        )
      }
    }

    // 3. CATEGORY CHIPS
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { category ->
          val isSelected = selectedCategory == category
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) NavyPrimary else Color.White,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isSelected) NavyPrimary else Color(0xFFE5E7EB)
            ),
            modifier = Modifier.clickable { viewModel.selectCategory(category) }
          ) {
            Text(
              text = category,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else Color(0xFF4B5563),
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
    }

    // 4. SECTION HEADER: "Wapsi Cars" (EXACT FROM REFERENCE VIDEO)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Wapsi Cars",
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF111827)
        )

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFFEFF6FF)
        ) {
          Text(
            text = "${filteredFleet.size} Available",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }
    }

    // 5. LIST OF WAPSI CARS (EXACT CARD LAYOUT FROM VIDEO)
    if (filteredFleet.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = null,
              tint = Color(0xFF9CA3AF),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No car matching current filters",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Select 'All Cities' or 'All' category to view entire fleet.",
              fontSize = 12.sp,
              color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = {
                viewModel.selectCity("All Cities")
                viewModel.selectCategory("All")
                viewModel.setSearchQuery("")
              },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Show All Cars", color = Color.White)
            }
          }
        }
      }
    } else {
      items(filteredFleet, key = { it.id }) { car ->
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          CarCard(
            car = car,
            onViewDetails = { onOpenDetail(car) },
            onBookNow = { onOpenBooking(car) }
          )
        }
      }
    }
  }
}
