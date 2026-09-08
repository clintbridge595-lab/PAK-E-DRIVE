package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Car
import com.example.ui.components.CarCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SearchScreen(
  viewModel: MainViewModel,
  onOpenBooking: (Car) -> Unit,
  onOpenDetail: (Car) -> Unit,
  modifier: Modifier = Modifier
) {
  val fleet = viewModel.fleet
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  var sortByPriceAsc by remember { mutableStateOf<Boolean?>(null) } // null = default, true = low-high, false = high-low

  val categories = listOf("All", "Sedan", "SUV", "7-Seater", "Van", "Luxury Wedding")

  val filteredFleet = remember(fleet, selectedCategory, searchQuery, sortByPriceAsc) {
    var result = fleet.filter { car ->
      val matchesCategory = (selectedCategory == "All" || car.category.equals(selectedCategory, ignoreCase = true))
      val matchesQuery = searchQuery.isBlank() ||
        car.name.contains(searchQuery, ignoreCase = true) ||
        car.make.contains(searchQuery, ignoreCase = true) ||
        car.description.contains(searchQuery, ignoreCase = true) ||
        car.serviceType.contains(searchQuery, ignoreCase = true)
      matchesCategory && matchesQuery
    }
    if (sortByPriceAsc != null) {
      result = if (sortByPriceAsc == true) {
        result.sortedBy { it.tenHourRate }
      } else {
        result.sortedByDescending { it.tenHourRate }
      }
    }
    result
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BackgroundLight)
  ) {
    // Top Bar Header
    Surface(
      color = Color.White,
      modifier = Modifier.fillMaxWidth(),
      shadowElevation = 1.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "Fleet Catalog & Rates",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = NavyPrimary
        )
        Text(
          text = "Transparent pricing with verified chauffeur service included",
          fontSize = 12.sp,
          color = TextSecondaryMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input (CRITICAL: Black text inside input on white background)
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = {
            Text("Type car name, seating, or city...", color = TextSecondaryMuted, fontSize = 13.sp)
          },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondaryMuted)
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF0A0F1D),
            unfocusedTextColor = Color(0xFF0A0F1D),
            cursorColor = NavyPrimary,
            focusedContainerColor = Color(0xFFF7F9FC),
            unfocusedContainerColor = Color(0xFFF7F9FC),
            focusedBorderColor = NavyPrimary,
            unfocusedBorderColor = BorderStroke
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categories.forEach { cat ->
            val isSelected = selectedCategory == cat
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.selectCategory(cat) },
              label = {
                Text(
                  text = cat,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NavyPrimary,
                selectedLabelColor = Color.White,
                containerColor = Color(0xFFF1F4F9),
                labelColor = TextPrimaryDark
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = if (isSelected) NavyPrimary else BorderStroke
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Sorting Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Showing ${filteredFleet.size} vehicles",
            fontSize = 12.sp,
            color = TextSecondaryMuted,
            fontWeight = FontWeight.Medium
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(
              onClick = {
                sortByPriceAsc = if (sortByPriceAsc == true) null else true
              },
              colors = ButtonDefaults.textButtonColors(
                contentColor = if (sortByPriceAsc == true) OrangeAccent else TextSecondaryMuted
              )
            ) {
              Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text("Price Low", fontSize = 11.sp)
            }

            TextButton(
              onClick = {
                sortByPriceAsc = if (sortByPriceAsc == false) null else false
              },
              colors = ButtonDefaults.textButtonColors(
                contentColor = if (sortByPriceAsc == false) OrangeAccent else TextSecondaryMuted
              )
            ) {
              Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text("Price High", fontSize = 11.sp)
            }
          }
        }
      }
    }

    // Vehicle Cards List
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp, start = 16.dp, end = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(filteredFleet, key = { it.id }) { car ->
        CarCard(
          car = car,
          onViewDetails = { onOpenDetail(car) },
          onBookNow = { onOpenBooking(car) }
        )
      }
    }
  }
}
