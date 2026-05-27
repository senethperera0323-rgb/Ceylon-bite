package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameSi: String,
    val nameTa: String,
    val descriptionEn: String,
    val descriptionSi: String,
    val descriptionTa: String,
    val basePrice: Double,
    val imageUrl: String,
    val isAvailable: Boolean,
    val category: String
)

@Entity(tableName = "modifiers")
data class ModifierEntity(
    @PrimaryKey val id: String,
    val menuItemId: String, // link to parent item
    val nameEn: String,
    val nameSi: String,
    val nameTa: String,
    val price: Double,
    val isAvailable: Boolean
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val itemsSummary: String, // Trilingual or English item summary with toppings
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryMethod: String, // "DELIVERY", "TAKEAWAY"
    val deliveryService: String, // "PickMe Eats", "Uber Eats", "CeylonBite Express"
    val paymentMethod: String, // "Card / PayHere", "Cash on Delivery (COD)"
    val paymentStatus: String, // "PENDING", "PAID"
    val orderStatus: String, // "PENDING", "PREPARING", "OUT_FOR_DELIVERY", "COMPLETED", "CANCELLED"
    val totalAmount: Double,
    val isAggregator: Boolean, // True for Uber/PickMe simulated pulls
    val timestamp: Long
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val selectedLanguage: String = "en", // "en", "si", "ta"
    val isStoreOpen: Boolean = true,
    val restaurantGroupId: String = "CB-COLOMBO-01",
    val printerConnected: Boolean = false,
    val printerName: String = "BT-Receipt-80mm"
)

// --- DAOs ---

@Dao
interface RestaurantDao {
    // Menu items
    @Query("SELECT * FROM menu_items")
    fun getMenuItems(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateMenuItemAvailability(id: String, isAvailable: Boolean)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItem(id: String)

    // Modifiers
    @Query("SELECT * FROM modifiers")
    fun getModifiers(): Flow<List<ModifierEntity>>

    @Query("SELECT * FROM modifiers WHERE menuItemId = :menuItemId")
    fun getModifiersForItem(menuItemId: String): Flow<List<ModifierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModifier(modifier: ModifierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModifiers(modifiers: List<ModifierEntity>)

    @Query("DELETE FROM modifiers WHERE id = :id")
    suspend fun deleteModifier(id: String)

    // Orders
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderStatus != 'COMPLETED' AND orderStatus != 'CANCELLED' ORDER BY timestamp DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderStatus = :isStatus WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, isStatus: String)

    @Query("UPDATE orders SET paymentStatus = :paymentStatus WHERE orderId = :orderId")
    suspend fun updatePaymentStatus(orderId: String, paymentStatus: String)

    // Config
    @Query("SELECT * FROM app_config WHERE id = 1")
    fun getAppConfig(): Flow<AppConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppConfig(config: AppConfigEntity)
}

// --- Database ---

@Database(
    entities = [MenuItemEntity::class, ModifierEntity::class, OrderEntity::class, AppConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RestaurantDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao

    companion object {
        @Volatile
        private var INSTANCE: RestaurantDatabase? = null

        fun getDatabase(context: Context): RestaurantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RestaurantDatabase::class.java,
                    "restaurant_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
