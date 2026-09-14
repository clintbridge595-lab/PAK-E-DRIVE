package com.example.data.model

data class PakistanRoute(
  val id: String,
  val fromCity: String,
  val toCity: String,
  val distanceKm: Int,
  val estimatedDuration: String,
  val highwayName: String,
  val startingRate: Int,
  val keyStops: String,
  val isPopular: Boolean = true,
  val routeBadge: String = "Intercity VIP"
)

object PakistanRoutesData {
  val routes: List<PakistanRoute> = listOf(
    PakistanRoute(
      id = "route_khi_hyd",
      fromCity = "Karachi",
      toCity = "Hyderabad",
      distanceKm = 165,
      estimatedDuration = "2.5 Hours",
      highwayName = "M-9 Super Highway",
      startingRate = 12000,
      keyStops = "Nooriabad, Kotri, Hyderabad Bypass",
      isPopular = true,
      routeBadge = "Most Popular"
    ),
    PakistanRoute(
      id = "route_khi_lhr",
      fromCity = "Karachi",
      toCity = "Lahore",
      distanceKm = 1210,
      estimatedDuration = "14 Hours",
      highwayName = "M-5 Sukkur-Multan & M-4",
      startingRate = 65000,
      keyStops = "Sukkur, Rohri, Multan, Khanewal",
      isPopular = true,
      routeBadge = "Executive Long Haul"
    ),
    PakistanRoute(
      id = "route_khi_isb",
      fromCity = "Karachi",
      toCity = "Islamabad",
      distanceKm = 1410,
      estimatedDuration = "16 Hours",
      highwayName = "M-5, M-4, M-2 Motorway",
      startingRate = 75000,
      keyStops = "Sukkur, Multan, Faisalabad, Kallar Kahar",
      isPopular = true,
      routeBadge = "Federal VIP Protocol"
    ),
    PakistanRoute(
      id = "route_khi_qta",
      fromCity = "Karachi",
      toCity = "Quetta",
      distanceKm = 685,
      estimatedDuration = "10.5 Hours",
      highwayName = "RCD Highway (N-25)",
      startingRate = 48000,
      keyStops = "Hub, Bela, Khuzdar, Kalat, Mastung",
      isPopular = true,
      routeBadge = "Balochistan Express"
    ),
    PakistanRoute(
      id = "route_khi_mux",
      fromCity = "Karachi",
      toCity = "Multan",
      distanceKm = 915,
      estimatedDuration = "10.5 Hours",
      highwayName = "M-5 Sukkur-Multan Motorway",
      startingRate = 55000,
      keyStops = "Hyderabad, Moro, Sukkur, Bahawalpur",
      isPopular = true,
      routeBadge = "City of Saints"
    ),
    PakistanRoute(
      id = "route_khi_fsd",
      fromCity = "Karachi",
      toCity = "Faisalabad",
      distanceKm = 1120,
      estimatedDuration = "13 Hours",
      highwayName = "M-5 & M-4 Motorway",
      startingRate = 60000,
      keyStops = "Sukkur, Multan, Shorkot, Gojra",
      isPopular = true,
      routeBadge = "Industrial Corridor"
    ),
    PakistanRoute(
      id = "route_khi_pew",
      fromCity = "Karachi",
      toCity = "Peshawar",
      distanceKm = 1490,
      estimatedDuration = "17 Hours",
      highwayName = "M-5, M-4, M-2, M-1",
      startingRate = 85000,
      keyStops = "Multan, Islamabad, Attock, Nowshera",
      isPopular = true,
      routeBadge = "Khyber Gateway"
    ),
    PakistanRoute(
      id = "route_khi_dik",
      fromCity = "Karachi",
      toCity = "Dera Ismail Khan",
      distanceKm = 1050,
      estimatedDuration = "13.5 Hours",
      highwayName = "Indus Highway (N-55)",
      startingRate = 58000,
      keyStops = "Sehwan, Larkana, Shikarpur, Kashmore, Rajanpur",
      isPopular = true,
      routeBadge = "Indus Highway Express"
    ),
    PakistanRoute(
      id = "route_khi_suk",
      fromCity = "Karachi",
      toCity = "Sukkur",
      distanceKm = 480,
      estimatedDuration = "6 Hours",
      highwayName = "National Highway (N-5) & M-5",
      startingRate = 28000,
      keyStops = "Hyderabad, Nawabshah, Moro, Khairpur",
      isPopular = true,
      routeBadge = "Sindh Central"
    ),
    PakistanRoute(
      id = "route_isb_gil",
      fromCity = "Islamabad",
      toCity = "Gilgit",
      distanceKm = 510,
      estimatedDuration = "12.5 Hours",
      highwayName = "Karakoram Highway (KKH N-35)",
      startingRate = 45000,
      keyStops = "Abbottabad, Mansehra, Besham, Chilas",
      isPopular = true,
      routeBadge = "Northern Tourism VIP"
    ),
    PakistanRoute(
      id = "route_lhr_isb",
      fromCity = "Lahore",
      toCity = "Islamabad",
      distanceKm = 375,
      estimatedDuration = "4 Hours",
      highwayName = "M-2 Motorway",
      startingRate = 22000,
      keyStops = "Sheikhupura, Bhera, Salt Range, Chakwal",
      isPopular = true,
      routeBadge = "Motorway Protocol"
    ),
    PakistanRoute(
      id = "route_lhr_fsd",
      fromCity = "Lahore",
      toCity = "Faisalabad",
      distanceKm = 185,
      estimatedDuration = "2.5 Hours",
      highwayName = "M-3 Motorway",
      startingRate = 14000,
      keyStops = "Sheikhupura, Nankana Sahib",
      isPopular = true,
      routeBadge = "Intercity Fast"
    ),
    PakistanRoute(
      id = "route_isb_pew",
      fromCity = "Islamabad",
      toCity = "Peshawar",
      distanceKm = 180,
      estimatedDuration = "2 Hours",
      highwayName = "M-1 Motorway",
      startingRate = 15000,
      keyStops = "Fateh Jang, Swabi, Nowshera, Chamkani",
      isPopular = true,
      routeBadge = "KPK Executive"
    )
  )
}
