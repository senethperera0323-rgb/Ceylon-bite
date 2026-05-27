package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApiClient
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Enums ---
enum class UserRole { SPLASH, CUSTOMER, STAFF_LOGIN, STAFF_DASHBOARD }

data class CartItem(
    val menuItem: MenuItemEntity,
    val selectedModifiers: List<ModifierEntity>,
    val quantity: Int
)

data class SriLankanTown(
    val name: String,
    val distanceKm: Double,
    val isUberEatsCovered: Boolean,
    val isPickMeEatsCovered: Boolean
)

class RestaurantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RestaurantDatabase.getDatabase(application)
    private val dao = db.restaurantDao()

    // --- Dynamic Trilingual Translation Engine ---
    private val translationsEn = mapOf(
        "app_title" to "CeylonBite",
        "tagline" to "AI-Spiced Restaurant Suite & Smart POS",
        "created_by" to "Created by Seneth Perera",
        "credits" to "CeylonBite Restaurant Platform v1.2 — Designed & Engineered by Seneth Perera. All rights reserved.",
        "btn_customer" to "Order Food (Customer Portal)",
        "btn_staff" to "Restaurant Management POS & KDS",
        "btn_back" to "Return to Main Menu",
        "login_title" to "Secure Staff Gateway",
        "login_subtitle" to "Access internal cash register, inventory, and KDS tickets.",
        "lbl_restaurant_id" to "Restaurant Group ID",
        "lbl_username" to "Username / Operator",
        "lbl_password" to "Password PIN",
        "btn_login" to "Authorize & Login",
        "err_login" to "Invalid Operator Credentials or Group ID specified.",
        "select_language" to "Select Language / භාෂාව තෝරන්න / மொழி",
        "delivery_location" to "Sri Lanka Delivery Address Finder",
        "lbl_select_town" to "Select Operational Town",
        "lbl_distance_from_branch" to "Branch Radial Distance",
        "uber_eats_status" to "Uber Eats Aggregator Coverage (5.0 km Max)",
        "pickme_eats_status" to "PickMe Eats Aggregator Coverage (7.0 km Max)",
        "zone_delivery" to "Delivery Available",
        "zone_takeaway_only" to "TAKEAWAY & PICKUP ONLY (Outside delivery zone)",
        "coverage_warning" to "Your locality falls outside limits of Uber/PickMe Eats active range. Ordering is locked into Takeaway/Pickup ONLY.",
        "btn_update_location" to "Confirm Address & Apply Geofencing",
        "menu_categories" to "Ceylon Dishes Menu",
        "price_lkr" to "Rs.",
        "add_to_cart" to "Customize & Add To Bill",
        "cart_title" to "Guest Order Cart",
        "customize_item" to "Toppings & Modifiers",
        "base_price" to "Base Recipe Price",
        "addons_total" to "Selected Modifiers Toll",
        "item_total" to "Estimated Item Total",
        "btn_confirm_item" to "Add to Order Basket",
        "btn_checkout" to "Initiate PayHere Payment Invoice",
        "payment_title" to "PayHere™ Gateway Simulation",
        "payment_sim_desc" to "Mock gateway verifying payment authorization for Card, Mobile wallets, Genie or Frimi.",
        "btn_pay_card" to "Complete Secured Pre-Paid Card Checkout",
        "btn_pay_cod" to "Confirm Cash on Delivery Order",
        "order_tracking" to "Live Order Status Tracker",
        "order_id_lbl" to "Order Reference",
        "order_placed" to "Order Received on Core Server",
        "order_preparing" to "Kitchen cooking the spices...",
        "order_dispatched" to "Dispatch (Courier out with warm food!)",
        "order_completed" to "Meal Handed Over Safely. Enjoy!",
        "order_history" to "Historic Order Registry",
        "kds_dashboard" to "Kitchen Display Terminal (KDS)",
        "live_active_tickets" to "Cooking & Fulfillment Board",
        "btn_dispatch" to "Fulfill & Dispatch",
        "btn_complete" to "Complete Ticket",
        "pos_manager" to "POS AI Menu Builder",
        "add_new_item" to "Add New Dish with Gemini AI",
        "dish_name_input" to "Enter Sri Lankan Dish Name (e.g. Cheese Kottu, Chicken Biryani)",
        "ai_auto_complete" to "Gemini AI Multi-Language Generate",
        "ai_generating" to "Invoking Gemini to translate and map imagery...",
        "is_available_status" to "Live Availability status",
        "out_of_stock" to "Out of Stock (Auto-Disabled in portals)",
        "store_open_toggle" to "Aggregator Store Online (Uber / PickMe Sync)",
        "aggregators_heading" to "Store Status & Simulated Delivery Aggregators",
        "connected_printers" to "Hardware Printing Integration (ESC/POS 80mm)",
        "print_kitchen_token" to "Print Cooking Token",
        "print_receipt" to "Print Customer Invoice",
        "sms_simulation" to "Notify.lk transactional SMS dispatch",
        "push_received" to "Alert dispatched successfully!",
        "receipt_printed" to "ESC/POS receipt queue executed over simulated Bluetooth!",
        "active_store" to "Active Store Status",
        "open" to "OPEN",
        "closed" to "CLOSED — Aggregators Stopped",
        "sms_log_header" to "Transactional SMS Logs (Notify.lk Gateway SDK)",
        "epson_log_header" to "ESC/POS Kitchen Token Logs (Bluetooth Core)",
        "amount" to "Amount",
        "success" to "Success",
        "sim_aggregator_order" to "Inject Simulated Uber/PickMe Guest Order"
    )

    private val translationsSi = mapOf(
        "app_title" to "සෙලෝන්බයිට්",
        "tagline" to "AI තාක්ෂණයෙන් සවිබල ගැන්වූ අවන්හල් පද්ධතිය සහ POS",
        "created_by" to "Seneth Perera විසින් නිර්මාණය කරන ලදී",
        "credits" to "CeylonBite අවන්හල් පද්ධතිය v1.2 — සෙනෙත් රොෂාන් පෙරේරා විසින් සැලසුම් කර නිපදවන ලදී.",
        "btn_customer" to "භෝජන ඇණවුම් (පාරිභෝගික ද්වාරය)",
        "btn_staff" to "අවන්හල් කළමනාකරණය POS සහ KDS පද්ධතිය",
        "btn_back" to "ප්‍රධාන මෙනුවට යන්න",
        "login_title" to "සේවක ආරක්ෂිත පිවිසුම",
        "login_subtitle" to "ඇතුළත මුදල් ලේඛනය, තොග පද්ධතිය සහ KDS ටිකට්පත් වලට පිවිසෙන්න.",
        "lbl_restaurant_id" to "අවන්හල් සමූහ හැඳුනුම්පත",
        "lbl_username" to "පරිශීලක නාමය / ක්‍රියාකරු",
        "lbl_password" to "ආරක්ෂිත මුරපද PIN",
        "btn_login" to "අවසර ලබාගෙන පිවිසෙන්න",
        "err_login" to "අවලංගු ක්‍රියාකරු අක්තපත්‍ර හෝ අවන්හල් සමූහ හැඳුනුම්පතක් ඇතුළත් කර ඇත.",
        "select_language" to "භාෂාව තෝරන්න",
        "delivery_location" to "ශ්‍රී ලංකා බෙදාහැරීම් ස්ථාන සොයන්නා",
        "lbl_select_town" to "මෙහෙයුම් නගරය තෝරන්න",
        "lbl_distance_from_branch" to "ශාඛාවේ සිට අරීය දුර",
        "uber_eats_status" to "Uber Eats බෙදාහැරීම් සීමාව (උපරිම කිලෝමීටර් 5.0)",
        "pickme_eats_status" to "PickMe Eats බෙදාහැරීම් සීමාව (උපරිම කිලෝමීටර් 7.0)",
        "zone_delivery" to "බෙදාහැරීම සක්‍රීයයි",
        "zone_takeaway_only" to "පැමිණ රැගෙන යාම පමණයි (බෙදාහැරීමේ සීමාවෙන් බැහැරයි)",
        "coverage_warning" to "ඔබ තෝරාගත් ස්ථානය Uber/PickMe Eats සීමාවෙන් බැහැරයි. ඇණවුම් ලබාගත හැක්කේ පැමිණ රැගෙන යාමට පමණි.",
        "btn_update_location" to "ස්ථානය තහවුරු කර භූ-සීමාව යොදන්න",
        "menu_categories" to "ප්‍රණීත ශ්‍රී ලාංකීය ආහාර රැසක්",
        "price_lkr" to "රු.",
        "add_to_cart" to "ආහාරය වෙනස් කර බිලට එක් කරන්න",
        "cart_title" to "පාරිභෝගික ඇණවුම් කූඩය",
        "customize_item" to "අමතර වෙනස් කිරීම් සහ රසකාරක",
        "base_price" to "මූලික ආහාර මිල",
        "addons_total" to "තෝරාගත් අමතර ද්‍රව්‍ය මිල",
        "item_total" to "සම්පූර්ණ ආහාර මිල",
        "btn_confirm_item" to "ඇණවුම් කූඩයට එක් කරන්න",
        "btn_checkout" to "PayHere මුදල් ගෙවීමට යොමු වන්න",
        "payment_title" to "PayHere™ ගෙවීම් පද්ධතිය",
        "payment_sim_desc" to "කාඩ්පත්, ජංගම පසුම්බි, Genie හෝ Frimi හරහා ගෙවීම් තහවුරු කිරීමේ අනුකරණය.",
        "btn_pay_card" to "කාඩ්පත් මඟින් ගෙවීම අවසන් කරන්න",
        "btn_pay_cod" to "භාණ්ඩ ලැබුණු පසුව මුදල් ගෙවන්න (COD)",
        "order_tracking" to "සජීවී ඇණවුම් ට්‍රැකර්",
        "order_id_lbl" to "ඇණවුම් අංකය",
        "order_placed" to "ඇණවුම ලැබී ලියාපදිංචි විය",
        "order_preparing" to "කුස්සියේ සූපවේදීන් ආහාරය පිසිනවා...",
        "order_dispatched" to "බෙදාහැරීමට යොමු විය (රයිඩර් මාර්ගයේ ගමන් කරයි!)",
        "order_completed" to "ආහාරය ඔබට භාර දෙන ලදී. රස විඳින්න!",
        "order_history" to "පැරණි බිල්පත් ලේඛනය",
        "kds_dashboard" to "කුස්සි සජීවී පාලක පැනලය (KDS)",
        "live_active_tickets" to "පිසින සහ සූදානම් කරන ඇණවුම්",
        "btn_dispatch" to "පිරිනමා බෙදාහරින්න",
        "btn_complete" to "ටිකට්පත අවසන් කරන්න",
        "pos_manager" to "POS AI මෙනු නිර්මාපකයා",
        "add_new_item" to "Gemini AI මඟින් අලුත් කෑමක් එක් කරන්න",
        "dish_name_input" to "කෑමෙහි නම ඇතුළත් කරන්න (උදා: Cheese Kottu, Chicken Rice)",
        "ai_auto_complete" to "Gemini AI භාෂා පරිවර්තනය සමඟ එක් කරන්න",
        "ai_generating" to "Gemini AI පරිවර්තන සහ පින්තූරය තෝරනවා...",
        "is_available_status" to "සජීවී තොග තිබේ",
        "out_of_stock" to "තොග අවසන් (ස්වයංක්‍රීයව අක්‍රීය වේ)",
        "store_open_toggle" to "නියෝජිත සේවාවල අවන්හල් විවෘත භාවය (Uber/PickMe)",
        "aggregators_heading" to "සහකාර සේවා සහ ඇණවුම් සමමුහුර්තකරණය",
        "connected_printers" to "තාක්ෂණික මුද්‍රණ එකතුව (ESC/POS 80mm)",
        "print_kitchen_token" to "කුස්සි ටෝකනය මුද්‍රණය",
        "print_receipt" to "පාරිභෝගික බිල මුද්‍රණය",
        "sms_simulation" to "Notify.lk ඔස්සේ කෙටි පණිවිඩ සේවා",
        "push_received" to "කෙටි පණිවිඩය සාර්ථකව යවන ලදී!",
        "receipt_printed" to "ESC/POS මුද්‍රණය සාර්ථකව සිදු විය!",
        "active_store" to "අවන්හල් ක්‍රියාකාරීත්වය",
        "open" to "විවෘතයි",
        "closed" to "වසා ඇත — සහකාර සේවා අක්‍රීයයි",
        "sms_log_header" to "කෙටි පණිවිඩ වාර්තා (Notify.lk SDK)",
        "epson_log_header" to "ESC/POS මුද්‍රණ වාර්තා (බ්ලූටූත්)",
        "amount" to "මුදල",
        "success" to "සාර්ථකයි",
        "sim_aggregator_order" to "Uber/PickMe ඇණවුමක් අනුකරණය කරන්න"
    )

    private val translationsTa = mapOf(
        "app_title" to "செலோன்பைட்",
        "tagline" to "AI மூலம் இயங்கும் உணவக மேலாண்மை மற்றும் POS",
        "created_by" to "Seneth Perera அவர்களால் உருவாக்கப்பட்டது",
        "credits" to "CeylonBite உணவக மேலாண்மைத்தளம் v1.2 — செனெத் பெரேராவால் வடிவமைக்கப்பட்டது. அனைத்து உரிமைகளும் பாதுகாக்கப்பட்டவை.",
        "btn_customer" to "உணவு ஆர்டர் செய்க (வாடிக்கையாளர் போர்டல்)",
        "btn_staff" to "உணவக மேலாண்மை, POS & KDS முனையம்",
        "btn_back" to "முதன்மை மெனுவுக்குத் திரும்பு",
        "login_title" to "பாதுகாப்பான பணியாளர் நுழைவு",
        "login_subtitle" to "உள் பிஓஎஸ் பணப்பதிவேடு, இருப்பு மற்றும் சமையலறை ஆர்டர்களை அணுகவும்.",
        "lbl_restaurant_id" to "உணவக குழு அடையாள எண்",
        "lbl_username" to "பயனர் பெயர் / இயக்கி",
        "lbl_password" to "பாதுகாப்பான பின் எண்",
        "btn_login" to "அங்கீகாரம் & உள்நுழைக",
        "err_login" to "தவறான பணியாளர் அடையாளங்கள் அல்லது உணவக குழு ஐடி.",
        "select_language" to "மொழியைத் தேர்வு செய்க",
        "delivery_location" to "இலங்கை டெலிவரி முகவரி கண்டுபிடிப்பான்",
        "lbl_select_town" to "செயல்பாட்டு நகரத்தைத் தேர்வுசெய்க",
        "lbl_distance_from_branch" to "கிளையில் இருந்து தூரம்",
        "uber_eats_status" to "Uber Eats விநியோக வரம்பு (அதிகபட்சம் 5.0 கிமீ)",
        "pickme_eats_status" to "PickMe Eats விநியோக வரம்பு (அதிகபட்சம் 7.0 கிமீ)",
        "zone_delivery" to "டெலிவரி கிடைக்கும்",
        "zone_takeaway_only" to "வந்து வாங்குதல் மட்டும் (விநியோக எல்லைக்கு வெளியே)",
        "coverage_warning" to "உங்கள் முகவரி விநியோக எல்லைக்கு வெளியே உள்ளது. வந்து வாங்குதல் மட்டுமே சாத்தியம்.",
        "btn_update_location" to "இடத்தை உறுதிப்படுத்தி வரம்பை பயன்படுத்துக",
        "menu_categories" to "சுவையான இலங்கை உணவுகள்",
        "price_lkr" to "ரூ.",
        "add_to_cart" to "மாற்றியமைத்து பில்லில் சேர்க்கவும்",
        "cart_title" to "வாடிக்கையாளர் கூடை",
        "customize_item" to "கூடுதல் சேர்க்கைகள் & மாற்றங்கள்",
        "base_price" to "அடிப்படை விலை",
        "addons_total" to "கூடுதல் பொருட்களின் விலை",
        "item_total" to "மொத்த உணவு விலை",
        "btn_confirm_item" to "கூடையில் சேர்க்கவும்",
        "btn_checkout" to "PayHere கட்டண நுழைவுக்குச் செல்லவும்",
        "payment_title" to "PayHere™ கட்டண முறை",
        "payment_sim_desc" to "கார்டுகள், மொபைல் வாலட்டுகள், Genie அல்லது Frimi கட்டண அங்கீகார அனுகரணம்.",
        "btn_pay_card" to "கார்டு மூலம் பணம் செலுத்தவும்",
        "btn_pay_cod" to "பொருள் பெற்ற பின் பணம் செலுத்தவும் (COD)",
        "order_tracking" to "ஆர்டர் நேரடி டிராக்கர்",
        "order_id_lbl" to "ஆர்டர் எண்",
        "order_placed" to "ஆர்டர் பெறப்பட்டு பதிவு செய்யப்பட்டது",
        "order_preparing" to "சமையலறையில் உணவு தயாரிக்கப்படுகிறது...",
        "order_dispatched" to "டெலிவரிக்கு அனுப்பப்பட்டது (விநியோகஸ்தர் வருகிறார்!)",
        "order_completed" to "ஆர்டர் வெற்றிகரமாக வழங்கப்பட்டது. உணவை அனுபவிக்கவும்!",
        "order_history" to "பழைய பில்களின் பதிவேடு",
        "kds_dashboard" to "சமையலறை நேரடி காட்சி (KDS)",
        "live_active_tickets" to "செயலில் இருக்கும் சமையல் டிக்கெட்டுகள்",
        "btn_dispatch" to "டெலிவரிக்கு அனுப்பு",
        "btn_complete" to "டிக்கெட்டை நிறைவு செய்",
        "pos_manager" to "POS AI மெனு மேலாளர்",
        "add_new_item" to "Gemini AI உடன் புதிய உணவைச் சேர்க்கவும்",
        "dish_name_input" to "உணவின் பெயரை உள்ளிடவும் (Kottu/Rice)",
        "ai_auto_complete" to "Gemini AI பல மொழி ஒத்திசைவு",
        "ai_generating" to "விளக்கம் மற்றும் படத்தை பெறுகிறது...",
        "is_available_status" to "நேரடி இருப்பு நிலை",
        "out_of_stock" to "இருப்பு இல்லை (தானாகவே முடக்கப்படும்)",
        "store_open_toggle" to "டெலிவரி தளம் திறந்த நிலை (Uber/PickMe)",
        "aggregators_heading" to "பங்குதாரர்கள் மற்றும் ஆர்டர் ஒத்திசைவு",
        "connected_printers" to "அச்சுப்பொறி இணைப்பு (ESC/POS 80mm)",
        "print_kitchen_token" to "சமையலறை பில் அச்சிடு",
        "print_receipt" to "வாடிக்கையாளர் பில் அச்சிடு",
        "sms_simulation" to "Notify.lk வழியாக குறுஞ்செய்தி விநியோகம்",
        "push_received" to "குறுஞ்செய்தி வெற்றிகரமாக அனுப்பப்பட்டது!",
        "receipt_printed" to "ESC/POS அச்சிடும் செயல்முறை வெற்றிகரமாக அனுப்பப்பட்டது!",
        "active_store" to "உணவக செயல்பாடு",
        "open" to "திறந்துள்ளது",
        "closed" to "மூடப்பட்டுள்ளது — டெலிவரி தளம் நிறுத்தப்பட்டது",
        "sms_log_header" to "குறுஞ்செய்தி பதிவுகள் (Notify.lk SDK)",
        "epson_log_header" to "ESC/POS அச்சிடும் பதிவுகள் (ப்ளூடூத்)",
        "amount" to "தொகை",
        "success" to "வெற்றி",
        "sim_aggregator_order" to "Uber/PickMe ஆர்டரை உள்ளிடவும்"
    )

    fun tr(key: String, langCode: String): String {
        return when (langCode) {
            "si" -> translationsSi[key] ?: translationsEn[key] ?: key
            "ta" -> translationsTa[key] ?: translationsEn[key] ?: key
            else -> translationsEn[key] ?: key
        }
    }

    // --- State Managers for App flows ---
    private val _currentRole = MutableStateFlow(UserRole.SPLASH)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _isStoreOpen = MutableStateFlow(true)
    val isStoreOpen: StateFlow<Boolean> = _isStoreOpen.asStateFlow()

    private val _restaurantGroupId = MutableStateFlow("CB-COLOMBO-1")
    val restaurantGroupId: StateFlow<String> = _restaurantGroupId.asStateFlow()

    // --- Location and geofencing configurations ---
    val sriLankanTowns = listOf(
        SriLankanTown("Colombo Fort / කොළඹ කොටුව", 2.1, isUberEatsCovered = true, isPickMeEatsCovered = true),
        SriLankanTown("Kollupitiya / කොල්ලුපිටිය", 3.5, isUberEatsCovered = true, isPickMeEatsCovered = true),
        SriLankanTown("Bambalapitiya / බම්බලපිටිය", 4.8, isUberEatsCovered = true, isPickMeEatsCovered = true),
        SriLankanTown("Rajagiriya / රාජගිරිය", 6.2, isUberEatsCovered = false, isPickMeEatsCovered = true),
        SriLankanTown("Dehiwala / දෙහිවල", 6.9, isUberEatsCovered = false, isPickMeEatsCovered = true),
        SriLankanTown("Mount Lavinia / ගල්කිස්ස", 8.2, isUberEatsCovered = false, isPickMeEatsCovered = false),
        SriLankanTown("Moratuwa / මොරටුව", 14.5, isUberEatsCovered = false, isPickMeEatsCovered = false),
        SriLankanTown("Negombo / මීගමුව", 37.0, isUberEatsCovered = false, isPickMeEatsCovered = false),
        SriLankanTown("Galle / ගාල්ල", 116.0, isUberEatsCovered = false, isPickMeEatsCovered = false)
    )

    private val _selectedTown = MutableStateFlow(sriLankanTowns[1]) // Kollupitiya default
    val selectedTown: StateFlow<SriLankanTown> = _selectedTown.asStateFlow()

    // Mode: Takeaway only or Delivery allowed
    val isDeliveryAllowed: StateFlow<Boolean> = _selectedTown.map { town ->
        town.isUberEatsCovered || town.isPickMeEatsCovered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _deliveryMethod = MutableStateFlow("TAKEAWAY") // "DELIVERY", "TAKEAWAY"
    val deliveryMethod: StateFlow<String> = _deliveryMethod.asStateFlow()

    // Cart details
    private val _cartList = MutableStateFlow<List<CartItem>>(emptyList())
    val cartList: StateFlow<List<CartItem>> = _cartList.asStateFlow()

    // Active screen customizables
    private val _customizingItem = MutableStateFlow<MenuItemEntity?>(null)
    val customizingItem: StateFlow<MenuItemEntity?> = _customizingItem.asStateFlow()

    private val _selectedModifiers = MutableStateFlow<List<ModifierEntity>>(emptyList())
    val selectedModifiers: StateFlow<List<ModifierEntity>> = _selectedModifiers.asStateFlow()

    // Active order tracking flow
    private val _activeTrackedOrder = MutableStateFlow<OrderEntity?>(null)
    val activeTrackedOrder: StateFlow<OrderEntity?> = _activeTrackedOrder.asStateFlow()

    // Receipt printer simulations log (Bluetooth token logger)
    private val _printerLogs = MutableStateFlow<List<String>>(emptyList())
    val printerLogs: StateFlow<List<String>> = _printerLogs.asStateFlow()

    // SMS simulation log (Notify.lk gateway transactional logger)
    private val _smsLogs = MutableStateFlow<List<String>>(emptyList())
    val smsLogs: StateFlow<List<String>> = _smsLogs.asStateFlow()

    // Live menu items & modifiers from DB
    val menuItemsList: StateFlow<List<MenuItemEntity>> = dao.getMenuItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val modifiersList: StateFlow<List<ModifierEntity>> = dao.getModifiers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ordersList: StateFlow<List<OrderEntity>> = dao.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeKdsOrders: StateFlow<List<OrderEntity>> = dao.getActiveOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI generation progress
    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    init {
        // Prepopulate core dishes on first launch
        viewModelScope.launch {
            // Load App Config language
            dao.getAppConfig().collectLatest { config ->
                if (config != null) {
                    _selectedLanguage.value = config.selectedLanguage
                    _isStoreOpen.value = config.isStoreOpen
                    _restaurantGroupId.value = config.restaurantGroupId
                } else {
                    // Create basic default config
                    val initConfig = AppConfigEntity()
                    dao.saveAppConfig(initConfig)
                }
            }
        }

        viewModelScope.launch {
            // Check if menu is empty, prepopulate with gorgeous trilingual defaults
            dao.getMenuItems().firstOrNull()?.let { list ->
                if (list.isEmpty()) {
                    prepopulateDatabase()
                }
            }
        }
    }

    private suspend fun prepopulateDatabase() {
        Log.i("RestaurantViewModel", "First launch - injecting curated trilingual Sri Lankan dishes and modifier options.")
        val defaultMenus = listOf(
            MenuItemEntity(
                id = "item_01",
                nameEn = "Cheese Kottu Roti",
                nameSi = "චීස් කොත්තු රොටි",
                nameTa = "சீஸ் கொத்து ரொட்டி",
                descriptionEn = "Sri Lankan style Kottu with rich mozzarella melt, veggies, and shredded organic parotta.",
                descriptionSi = "නැවුම් එළවළු, චීස් සහ දේශීය කුළුබඩු එක්කර ගාගත් පරාටා සමඟ පිළිගන්වන රසවත් චීස් කොත්තු.",
                descriptionTa = "சுவையான காய்கறிகள், உருகிய சீஸ் மற்றும் உறிஞ்சப்பட்ட பரோட்டாக்களுடன் தயாரிக்கப்பட்ட சீஸ் கொத்து.",
                basePrice = 850.0,
                imageUrl = "https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?auto=format&fit=crop&w=600&q=80",
                isAvailable = true,
                category = "Kottu"
            ),
            MenuItemEntity(
                id = "item_02",
                nameEn = "Wok Spicy Fried Rice",
                nameSi = "නාවික කුළුබඩු සහිත ෆ්‍රයිඩ් රයිස්",
                nameTa = "காரமான வறுக்கப்பட்ட சோறு",
                descriptionEn = "Fragrant basmati wok-fried with fresh leeks, carrots, chilies, and Sri Lankan chicken gravy.",
                descriptionSi = "සුවඳැති බාස්මතී සහල් නැවුම් එළවළු සහ කුළුබඩු සමඟ වොක් එකක බැදගන්නා ලද ප්‍රණීත රයිස්.",
                descriptionTa = "மணம் மிக்க பாசுமதி அரிசி, புதிய காய்கறி மற்றும் மசாலாப் பொருட்களுடன் வறுத்த ருசியான சோறு.",
                basePrice = 720.0,
                imageUrl = "https://images.unsplash.com/photo-1603133872878-684f208fb84b?auto=format&fit=crop&w=600&q=80",
                isAvailable = true,
                category = "Fried Rice"
            ),
            MenuItemEntity(
                id = "item_03",
                nameEn = "Ceylon Egg Parotta",
                nameSi = "බිත්තර පරාටා",
                nameTa = "முட்டை பரோட்டா",
                descriptionEn = "Flaky layered flatbread infused with farm eggs and served with rich dhal curry.",
                descriptionSi = "නැවුම් බිත්තර සහ අර්තාපල් හොදි සමඟ පිළිගන්වන ස්ථර පරාටා.",
                descriptionTa = "புதிய முட்டை மற்றும் தால் கறியுடன் பரிமாறப்படும் மொறுமொறுப்பான பரோட்டா.",
                basePrice = 220.0,
                imageUrl = "https://images.unsplash.com/photo-1626132647523-66f5bf380027?auto=format&fit=crop&w=600&q=80",
                isAvailable = true,
                category = "Parotta"
            ),
            MenuItemEntity(
                id = "item_04",
                nameEn = "Royal Colombo Faluda",
                nameSi = "රෝයල් ෆලුඩා බීම",
                nameTa = "கொழும்பு ராஜ மகுட பலூடா",
                descriptionEn = "Chilled rose milk blend topped with sweet basil seeds, jelly cubes, and premium vanilla bean ice cream.",
                descriptionSi = "රෝස කිරි මිශ්‍රණය, තක්මරියා බීජ, ජෙලි සහ වැනිලා අයිස්ක්‍රීම් සමඟ පිරිනමන සිසිල් පානය.",
                descriptionTa = "ரோஸ் மில்க், கசகசா விதைகள், ஜெல்லி மற்றும் வெண்ணிலா ஐஸ்கிரீமுடன் பரிமாறப்படும் குளிர்ச்சியான பானம்.",
                basePrice = 450.0,
                imageUrl = "https://images.unsplash.com/photo-1497534446932-c925b458314e?auto=format&fit=crop&w=600&q=80",
                isAvailable = true,
                category = "Drink"
            )
        )

        val defaultModifiers = listOf(
            ModifierEntity("mod_01", "item_01", "Extra Cheese", "චීස් වැඩිපුර", "கூடுதல் சீஸ்", 150.0, true),
            ModifierEntity("mod_02", "item_01", "Double Egg Mix", "බිත්තර දෙකක්", "இரட்டை முட்டை", 100.0, true),
            ModifierEntity("mod_03", "item_01", "Ceylon Chicken Gravy", "කුකුළු මස් හොදි", "கோழி குழம்பு", 80.0, true),
            ModifierEntity("mod_04", "item_02", "Fried King Prawns", "බැදපු රතු ඉස්සන්", "வறுத்த இறால்", 280.0, true),
            ModifierEntity("mod_05", "item_02", "Chili Paste Spoon", "නැවුම් මිරිස් පේස්ට්", "மிளகாய் சாந்து", 50.0, true),
            ModifierEntity("mod_06", "item_03", "Spicy Chicken Devilled", "දේවල් චිකන් කෑලි", "காரமான சிக்கன்", 200.0, true),
            ModifierEntity("mod_07", "item_04", "Extra Ice Cream Scoop", "අයිස්ක්‍රීම් හැන්දක්", "கூடுதல் ஐஸ்கிரீம்", 100.0, true)
        )

        dao.insertMenuItems(defaultMenus)
        dao.insertModifiers(defaultModifiers)
    }

    // Changing language dynamically on-the-fly and saving in local configuration
    fun setLanguage(langCode: String) {
        viewModelScope.launch {
            _selectedLanguage.value = langCode
            dao.saveAppConfig(AppConfigEntity(selectedLanguage = langCode, isStoreOpen = _isStoreOpen.value, restaurantGroupId = _restaurantGroupId.value))
        }
    }

    // Toggle restaurant opening status
    fun setStoreStatus(isOpen: Boolean) {
        viewModelScope.launch {
            _isStoreOpen.value = isOpen
            dao.saveAppConfig(AppConfigEntity(selectedLanguage = _selectedLanguage.value, isStoreOpen = isOpen, restaurantGroupId = _restaurantGroupId.value))
        }
    }

    // Selecting location and adapting geofencing rules
    fun selectTown(town: SriLankanTown) {
        _selectedTown.value = town
        // Automatically check geofencing bounds
        if (!town.isUberEatsCovered && !town.isPickMeEatsCovered) {
            _deliveryMethod.value = "TAKEAWAY"
        } else {
            _deliveryMethod.value = "DELIVERY"
        }
    }

    fun setDeliveryMethod(method: String) {
        if (isDeliveryAllowed.value || method == "TAKEAWAY") {
            _deliveryMethod.value = method
        }
    }

    // Switch screens
    fun navigateTo(role: UserRole) {
        _currentRole.value = role
    }

    // --- Cart Actions ---
    fun startCustomize(item: MenuItemEntity) {
        _customizingItem.value = item
        _selectedModifiers.value = emptyList()
    }

    fun toggleModifier(modifier: ModifierEntity) {
        val current = _selectedModifiers.value.toMutableList()
        if (current.any { it.id == modifier.id }) {
            current.removeAll { it.id == modifier.id }
        } else {
            current.add(modifier)
        }
        _selectedModifiers.value = current
    }

    fun confirmCustomizeAndAddCart(item: MenuItemEntity, quantity: Int) {
        val list = _cartList.value.toMutableList()
        list.add(CartItem(item, _selectedModifiers.value.toList(), quantity))
        _cartList.value = list

        _customizingItem.value = null
        _selectedModifiers.value = emptyList()
    }

    fun removeFromCart(index: Int) {
        val list = _cartList.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _cartList.value = list
        }
    }

    fun clearCart() {
        _cartList.value = emptyList()
    }

    fun getCartTotal(): Double {
        var total = 0.0
        for (item in _cartList.value) {
            val itemBase = item.menuItem.basePrice
            val modifierTotal = item.selectedModifiers.sumOf { it.price }
            total += (itemBase + modifierTotal) * item.quantity
        }
        return total
    }

    // --- Checkout & Notifications Simulation ---
    fun placeOrder(customerName: String, phone: String, address: String, payMethod: String) {
        viewModelScope.launch {
            val total = getCartTotal()
            if (total <= 0) return@launch

            val orderId = "CB-${System.currentTimeMillis().toString().takeLast(6)}"
            val summaryList = mutableListOf<String>()

            for (cart in _cartList.value) {
                val configStr = if (cart.selectedModifiers.isNotEmpty()) {
                    " (" + cart.selectedModifiers.joinToString(", ") { it.nameEn } + ")"
                } else ""
                summaryList.add("${cart.quantity}x ${cart.menuItem.nameEn}$configStr")
            }

            val summaryStr = summaryList.joinToString("\n")

            // Real SQLite insertion via DAO
            val order = OrderEntity(
                orderId = orderId,
                itemsSummary = summaryStr,
                customerName = customerName,
                customerPhone = phone,
                deliveryAddress = if (_deliveryMethod.value == "DELIVERY") address else "Takeaway Mode - pickup at Colombo restaurant",
                deliveryMethod = _deliveryMethod.value,
                deliveryService = if (_deliveryMethod.value == "DELIVERY") "CeylonBite Express" else "Self-Pickup",
                paymentMethod = payMethod,
                paymentStatus = if (payMethod == "Card / PayHere") "PAID" else "PENDING",
                orderStatus = "PENDING",
                totalAmount = total,
                isAggregator = false,
                timestamp = System.currentTimeMillis()
            )

            dao.insertOrder(order)
            _activeTrackedOrder.value = order

            // Trilingual automatic Notify.lk SMS trigger log mockup
            Log.i("Notify.lk", "Initializing SMS Pipeline for phone $phone")
            val lang = _selectedLanguage.value
            val smsText = when(lang) {
                "si" -> "සෙලෝන්බයිට්: ඔබගේ ඇණවුම ($orderId) රු.$total සඳහා සාර්ථකව ලැබුණි. ස්තූතියි! (Created by Seneth Perera)"
                "ta" -> "செலோன்பைட்: உங்களது உணவு ஆர்டர் ($orderId) பெறப்பட்டது. மொத்தத் தொகை ரூ.$total. நன்றி! (Created by Seneth Perera)"
                else -> "CeylonBite Alert: Your order ($orderId) for Rs. $total is accepted. Preparing now! (Created by Seneth Perera)"
            }
            sendMockSms(smsText)

            // Automate kitchen tokens (ESC/POS Epson Print Token)
            printEpsonReceiptMock(order)

            clearCart()
        }
    }

    private fun sendMockSms(message: String) {
        val currentLogs = _smsLogs.value.toMutableList()
        currentLogs.add(0, "[Notify.lk — ${System.currentTimeMillis()}] $message")
        _smsLogs.value = currentLogs.take(50) // keep last 50 logs
    }

    private fun printEpsonReceiptMock(order: OrderEntity) {
        val printOutput = """
            ==========================================
                      CEYLONBITE POS PRINTER          
              Operator Terminal Group: ${_restaurantGroupId.value}
              Created by Seneth Perera
            ==========================================
            Order Code     : ${order.orderId}
            Date & Time    : ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(order.timestamp))}
            Mode           : ${order.deliveryMethod} (${order.deliveryService})
            Customer       : ${order.customerName}
            Phone          : ${order.customerPhone}
            ------------------------------------------
            Menu Specifications:
            ${order.itemsSummary}
            ------------------------------------------
            Total Bill     : Rs. ${order.totalAmount}
            Payment Type   : ${order.paymentMethod}
            Gateway Auth   : APPROVED (PayHere SL Sim)
            ==========================================
                     KITCHEN COOKING TOKEN (ESC)      
            ==========================================
        """.trimIndent()

        val currentLogs = _printerLogs.value.toMutableList()
        currentLogs.add(0, printOutput)
        _printerLogs.value = currentLogs.take(30)
    }

    // Manual print event triggered by staff
    fun triggerManualPrint(order: OrderEntity, type: String) {
        printEpsonReceiptMock(order)
    }

    // --- POS Admin Menu Creator with Gemini Image API ---
    fun submitMenuItemWithAiImage(dishName: String) {
        if (dishName.trim().isEmpty()) return
        _isAiGenerating.value = true

        viewModelScope.launch {
            try {
                // Trigger Gemini API generate Content call
                val aiData = GeminiApiClient.generateFoodData(dishName)

                // Save item to Database Menu items
                val newItem = MenuItemEntity(
                    id = aiData.id,
                    nameEn = aiData.nameEn,
                    nameSi = aiData.nameSi,
                    nameTa = aiData.nameTa,
                    descriptionEn = aiData.descriptionEn,
                    descriptionSi = aiData.descriptionSi,
                    descriptionTa = aiData.descriptionTa,
                    basePrice = aiData.basePrice,
                    imageUrl = aiData.imageUrl,
                    isAvailable = true,
                    category = aiData.category
                )

                dao.insertMenuItem(newItem)

                // Save recommended modifiers
                val modifiersEntities = aiData.recommendedModifiers.map { mod ->
                    ModifierEntity(
                        id = "mod_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                        menuItemId = aiData.id,
                        nameEn = mod.nameEn,
                        nameSi = mod.nameSi,
                        nameTa = mod.nameTa,
                        price = mod.price,
                        isAvailable = true
                    )
                }

                dao.insertModifiers(modifiersEntities)
                Log.d("RestaurantViewModel", "Successfully saved AI generated item: ${aiData.nameEn}")
            } catch (e: Exception) {
                Log.e("RestaurantViewModel", "Failed generating AI food items: ${e.message}", e)
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    // Update Live Inventory stock levels via KDS instantly toggled
    fun toggleItemInStock(itemId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            dao.updateMenuItemAvailability(itemId, isAvailable)
            Log.i("RestaurantViewModel", "Toggled live inventory status. Synced online across CeylonBite, Uber Eats, and PickMe.")
        }
    }

    // Update active orders status on KDS board and fire out Notify.lk push/SMS transitions
    fun advanceOrderStatus(orderId: String, currentStatus: String) {
        viewModelScope.launch {
            val nextStatus = when(currentStatus) {
                "PENDING" -> "PREPARING"
                "PREPARING" -> "OUT_FOR_DELIVERY"
                "OUT_FOR_DELIVERY" -> "COMPLETED"
                else -> "COMPLETED"
            }

            dao.updateOrderStatus(orderId, nextStatus)

            // Trigger SMS notifications simulation for status change
            val updatedOrder = ordersList.value.find { it.orderId == orderId } ?: return@launch
            if (updatedOrder.paymentStatus == "PENDING" && nextStatus == "COMPLETED") {
                dao.updatePaymentStatus(orderId, "PAID")
            }

            if (_activeTrackedOrder.value?.orderId == orderId) {
                _activeTrackedOrder.value = updatedOrder.copy(orderStatus = nextStatus)
            }

            val lang = _selectedLanguage.value
            val statusAlert = when(nextStatus) {
                "PREPARING" -> when(lang) {
                    "si" -> "සෙලෝන්බයිට්: ඔබගේ ඇණවුම ($orderId) දැන් සූපවේදීන් විසින් සූදානම් කරමින් පවතී."
                    "ta" -> "செலோன்பைட்: உங்களது உணவு ஆர்டர் ($orderId) சமையலறையில் தயாராகிக்கொண்டிருக்கிறது."
                    else -> "CeylonBite: Your order ($orderId) is now being prepared by our chefs!"
                }
                "OUT_FOR_DELIVERY" -> when(lang) {
                    "si" -> "සෙලෝන්බයිට්: රයිඩර් මහතා ඔබගේ ඇණවුම ($orderId) රැගෙන පිටත්ව ඇත. සතුටින් භුක්ති විඳින්න!"
                    "ta" -> "செலோன்பைட்: விநியோகஸ்தர் உங்களது ஆர்டருடன் ($orderId) புறப்பட்டுவிட்டார். விரைவில் வரும்!"
                    else -> "CeylonBite Dispatch: Rider is en-route with your warm dinner ($orderId)!"
                }
                "COMPLETED" -> when(lang) {
                    "si" -> "සෙලෝන්බයිට්: ඔබගේ ඇණවුම ($orderId) භාරදෙන ලදී. නිර්මාණ සෙනෙත් Perera."
                    "ta" -> "செலோன்பைட்: ஆர்டர் ($orderId) வெற்றிகரமாக ஒப்படைக்கப்பட்டது. நன்றி. Seneth Perera."
                    else -> "CeylonBite: Order ($orderId) delivered successfully. Enjoy! Created by Seneth Perera."
                }
                else -> ""
            }

            if (statusAlert.isNotEmpty()) {
                sendMockSms(statusAlert)
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            dao.deleteMenuItem(id)
        }
    }

    fun clearTrackedOrder() {
        _activeTrackedOrder.value = null
    }

    // --- Aggregators order injections simulated ---
    fun injectSimulatedAggregatedOrder(serviceName: String) {
        viewModelScope.launch {
            if (!_isStoreOpen.value) {
                Log.w("AggregatorApi", "Store is CLOSED. Webhooks from PickMe/Uber eats are blocked.")
                return@launch
            }

            val orderId = "AG-${System.currentTimeMillis().toString().takeLast(6)}"
            val foods = listOf(
                "1x Cheese Kottu Roti (Extra Cheese)",
                "2x Royal Colombo Faluda",
                "1x Wok Spicy Fried Rice"
            )
            val dishSummary = foods.shuffled().take(2).joinToString("\n")
            val total = 1650.0

            val order = OrderEntity(
                orderId = orderId,
                itemsSummary = dishSummary,
                customerName = "Simulated ${serviceName} Guest",
                customerPhone = "077123${Random.nextInt(1000, 9999)}",
                deliveryAddress = "Guest Address in Colombo District",
                deliveryMethod = "DELIVERY",
                deliveryService = serviceName,
                paymentMethod = "Card / Online Integrated",
                paymentStatus = "PAID",
                orderStatus = "PENDING",
                totalAmount = total,
                isAggregator = true,
                timestamp = System.currentTimeMillis()
            )

            dao.insertOrder(order)
            printEpsonReceiptMock(order)

            val smsMsg = "CeylonBite Webhook: External order $orderId received from $serviceName Hub sync. Dispatched to KDS ticket."
            sendMockSms(smsMsg)
        }
    }
}
