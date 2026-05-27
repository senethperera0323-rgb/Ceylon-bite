package com.example.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AiFoodGenerationResult(
    val id: String,
    val nameEn: String,
    val nameSi: String,
    val nameTa: String,
    val descriptionEn: String,
    val descriptionSi: String,
    val descriptionTa: String,
    val basePrice: Double,
    val category: String,
    val imageUrl: String,
    val recommendedModifiers: List<AiModifierResult>
)

data class AiModifierResult(
    val nameEn: String,
    val nameSi: String,
    val nameTa: String,
    val price: Double
)

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    // Highly responsive mock content if Gemini API key is missing or error occurs
    fun getFallbackData(dishName: String): AiFoodGenerationResult {
        val cleanName = dishName.trim().lowercase()
        val category = when {
            cleanName.contains("kottu") -> "Kottu"
            cleanName.contains("rice") || cleanName.contains("biryani") -> "Fried Rice"
            cleanName.contains("parotta") || cleanName.contains("roti") || cleanName.contains("paratta") -> "Parotta"
            cleanName.contains("juice") || cleanName.contains("mojito") || cleanName.contains("tea") || cleanName.contains("coffee") || cleanName.contains("faluda") -> "Drink"
            else -> "Short Eats"
        }

        val imageUrl = when (category) {
            "Kottu" -> "https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?auto=format&fit=crop&w=600&q=80"
            "Fried Rice" -> "https://images.unsplash.com/photo-1603133872878-684f208fb84b?auto=format&fit=crop&w=600&q=80"
            "Parotta" -> "https://images.unsplash.com/photo-1626132647523-66f5bf380027?auto=format&fit=crop&w=600&q=80"
            "Drink" -> "https://images.unsplash.com/photo-1497534446932-c925b458314e?auto=format&fit=crop&w=600&q=80"
            else -> "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=600&q=80"
        }

        val nameEn = dishName.trim().capitalize()
        val nameSi = when {
            cleanName.contains("kottu") -> "කොත්තු"
            cleanName.contains("rice") -> "ෆ්‍රයිඩ් රයිස්"
            cleanName.contains("parotta") -> "පරාටා"
            cleanName.contains("juice") -> "ජූස්"
            else -> "කෑම වර්ග"
        }
        val nameTa = when {
            cleanName.contains("kottu") -> "கொத்து"
            cleanName.contains("rice") -> "சோறு"
            cleanName.contains("parotta") -> "பரோட்டா"
            cleanName.contains("juice") -> "சாறு"
            else -> "உணவு"
        }

        return AiFoodGenerationResult(
            id = "menu_ai_${System.currentTimeMillis()}",
            nameEn = nameEn,
            nameSi = nameSi,
            nameTa = nameTa,
            descriptionEn = "Tasty, freshly prepared $dishName with local Sri Lankan spices and fresh ingredients.",
            descriptionSi = "දේශීය ශ්‍රී ලාංකීය කුළුබඩු සහ නැවුම් අමුද්‍රව්‍ය යොදා සකස් කරන ලද රසවත් $dishName.",
            descriptionTa = "உள்ளூர் இலங்கை மசாலாப் பொருட்களுடன் புதிதாக தயாரிக்கப்பட்ட சுவையான $dishName.",
            basePrice = when (category) {
                "Kottu" -> 750.0
                "Fried Rice" -> 850.0
                "Parotta" -> 150.0
                "Drink" -> 350.0
                else -> 450.0
            },
            category = category,
            imageUrl = imageUrl,
            recommendedModifiers = listOf(
                AiModifierResult("Extra Cheese (චීස් වැඩිපුර / கூடுதல் பாலாடைக்கட்டி)", "චීස් වැඩිපුර", "கூடுதல் பாலாடைக்கட்டி", 180.0),
                AiModifierResult("Double Egg (බිත්තර දෙකක් / இரட்டை முட்டை)", "බිත්තර දෙකක්", "இரட்டை முட்டை", 100.0),
                AiModifierResult("Add Extra Gravy (හොදි වැඩිපුර / கூடுதல் குழம்பு)", "හොදි වැඩිපුර", "கூடுதல் குழம்பு", 60.0),
                AiModifierResult("Make Extra Spicy (අධික සැර / கூடுதல் காரம்)", "අධික සැර", "கூடுதல் காரம்", 40.0)
            )
        )
    }

    suspend fun generateFoodData(dishName: String): AiFoodGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("FakeKey")) {
            Log.w(TAG, "Gemini API key is not configured or placeholder. Swapping to local AI auto-complete parser.")
            return@withContext getFallbackData(dishName)
        }

        val prompt = """
            You are an AI chef for a Sri Lankan restaurant. Respond with a raw JSON representation of the dish named "$dishName".
            Do not include markdown blocks or formatting blocks around JSON, return ONLY the raw JSON string matching this exact schema:
            {
               "name_en": "English culinary name",
               "name_si": "Sinhala translation of the culinary name in Sinhala characters",
               "name_ta": "Tamil translation of the culinary name in Tamil characters",
               "description_en": "Delicious description in English context emphasizing Sri Lankan spices",
               "description_si": "Description in Sinhala language",
               "description_ta": "Description in Tamil language",
               "base_price": numeric price in Sri Lankan Rupees (LKR) such as 650.0,
               "category": "Choose one of: 'Kottu', 'Fried Rice', 'Parotta', 'Drink', 'Short Eats'",
               "modifiers": [
                   {"name_en": "Extra Gravy", "name_si": "හොදි වැඩිපුර", "name_ta": "கூடுதல் குழம்பு", "price": 50.0},
                   {"name_en": "Cheese Modifier", "name_si": "චීස්", "name_ta": "சீஸ்", "price": 150.0}
               ]
            }
        """.trimIndent()

        val url = "$BASE_URL?key=$apiKey"
        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toString().toRequestBody(MEDIA_TYPE_JSON))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Post request failed with response code ${response.code}")
                    return@withContext getFallbackData(dishName)
                }

                val bodyString = response.body?.string() ?: return@withContext getFallbackData(dishName)
                val responseJson = JSONObject(bodyString)
                val textCandidate = responseJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // Remove markdown wrapper if any
                var cleanJsonStr = textCandidate.trim()
                if (cleanJsonStr.startsWith("```json")) {
                    cleanJsonStr = cleanJsonStr.substringAfter("```json").substringBeforeLast("```").trim()
                } else if (cleanJsonStr.startsWith("```")) {
                    cleanJsonStr = cleanJsonStr.substringAfter("```").substringBeforeLast("```").trim()
                }

                val foodJson = JSONObject(cleanJsonStr)
                val category = foodJson.optString("category", "Kottu")
                val imageUrl = when (category) {
                    "Kottu" -> "https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?auto=format&fit=crop&w=600&q=80"
                    "Fried Rice" -> "https://images.unsplash.com/photo-1603133872878-684f208fb84b?auto=format&fit=crop&w=600&q=80"
                    "Parotta" -> "https://images.unsplash.com/photo-1626132647523-66f5bf380027?auto=format&fit=crop&w=600&q=80"
                    "Drink" -> "https://images.unsplash.com/photo-1497534446932-c925b458314e?auto=format&fit=crop&w=600&q=80"
                    else -> "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&w=600&q=80"
                }

                val recommendedModifiers = mutableListOf<AiModifierResult>()
                val modifiersArray = foodJson.optJSONArray("modifiers")
                if (modifiersArray != null) {
                    for (i in 0 until modifiersArray.length()) {
                        val mod = modifiersArray.getJSONObject(i)
                        recommendedModifiers.add(
                            AiModifierResult(
                                nameEn = mod.optString("name_en", "Extra Option"),
                                nameSi = mod.optString("name_si", "අමතර"),
                                nameTa = mod.optString("name_ta", "கூடுதல் விருப்பம்"),
                                price = mod.optDouble("price", 50.0)
                            )
                        )
                    }
                }

                if (recommendedModifiers.isEmpty()) {
                    recommendedModifiers.addAll(getFallbackData(dishName).recommendedModifiers)
                }

                AiFoodGenerationResult(
                    id = "menu_ai_${System.currentTimeMillis()}",
                    nameEn = foodJson.optString("name_en", dishName),
                    nameSi = foodJson.optString("name_si", "කොත්තු"),
                    nameTa = foodJson.optString("name_ta", "கொத்து"),
                    descriptionEn = foodJson.optString("description_en", "Crafted with island taste."),
                    descriptionSi = foodJson.optString("description_si", "දේශීය රසයෙන් යුක්තයි."),
                    descriptionTa = foodJson.optString("description_ta", "சுவையான இலங்கை உணவு."),
                    basePrice = foodJson.optDouble("base_price", 750.0),
                    category = category,
                    imageUrl = imageUrl,
                    recommendedModifiers = recommendedModifiers
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API: ${e.message}", e)
            getFallbackData(dishName)
        }
    }
}
