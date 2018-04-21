package utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Location
import android.os.Environment
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.ThemedSpinnerAdapter
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.loqoursys.meko.R
import com.loqoursys.meko.data.FoodItem
import com.loqoursys.meko.data.MekoOrder
import com.loqoursys.meko.data.OrderStatus
import com.loqoursys.meko.listener.ClickListener
import com.loqoursys.meko.qr.ImageType
import com.loqoursys.meko.qr.QRGContents
import com.loqoursys.meko.qr.QRGEncoder
import com.loqoursys.meko.qr.QRGSaver
import kotlinx.android.synthetic.main.spinner_item.view.*
import java.util.*

/**
 * Created by root on 4/10/18 for LoqourSys
 */
class MekoUtils {
    companion object {
        infix fun toJavaClass(className: String) = className::class.java
    }

}

class CartAdapter(val context: Context, private var cartItems: ArrayList<FoodItem>)
    : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val colorNormal = ContextCompat.getColor(context, R.color.divider_white)
    private val colorAlt = Color.parseColor("#80E8F5E9")

    override fun getItemCount(): Int = cartItems.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        if (position % 2 == 0) {
            holder.itemBg.setBackgroundColor(colorNormal)
        } else {
            holder.itemBg.setBackgroundColor(colorAlt)
        }
        holder.bind(item.servings, item.item_name, item.price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(view)
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemCount: TextView = itemView.findViewById(R.id.item_num)
        private val itemName: TextView = itemView.findViewById(R.id.item_name)
        private val itemPrice: TextView = itemView.findViewById(R.id.item_price)
        val itemBg: ConstraintLayout = itemView.findViewById(R.id.item_bg)

        fun bind(count: Int, name: String, price: Float) {
            itemCount.text = "$count"
            itemName.text = name
            itemPrice.text = "$price".toKshs()

        }
    }

    fun addItem(pos: Int, item: FoodItem) {
        cartItems.add(pos, item)
        notifyItemInserted(pos)
        redrawViews()
    }

    fun removeItem(pos: Int) {
        cartItems.removeAt(pos)
        notifyItemRemoved(pos)
        redrawViews()
    }

    private fun redrawViews() {
        notifyDataSetChanged()
    }

}

class OrdersAdapter(val context: Context, private var orderItems: ArrayList<MekoOrder>)
    : RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder>() {

    private val colorNormal = ContextCompat.getColor(context, R.color.divider_white)
    private val colorAlt = Color.parseColor("#80E8F5E9")
    private val cal = Calendar.getInstance()

    override fun getItemCount(): Int = orderItems.size

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val item = orderItems[position]
        if (position % 2 == 0) {
            holder.itemBg.setBackgroundColor(colorNormal)
        } else {
            holder.itemBg.setBackgroundColor(colorAlt)
        }

        cal.time = Date(item.order_date)
        val orderDate = DateTimeTemplate.format(cal, "%dd%/%mm%/%y%")
        val count = position + 1

        holder.bind(number = count, date = orderDate,
                status = item.order_status, order_number = item.order_id, items_count = item.orderItems!!.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.orders_item, parent, false)
        return OrdersViewHolder(view)
    }

    class OrdersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemCount: TextView = itemView.findViewById(R.id.items_count)
        private val itemNumber: TextView = itemView.findViewById(R.id.number)
        private val orderStatus: TextView = itemView.findViewById(R.id.order_status)
        private val orderDate: TextView = itemView.findViewById(R.id.order_date)
        private val orderNumber: TextView = itemView.findViewById(R.id.order_number)
        val itemBg: ConstraintLayout = itemView.findViewById(R.id.order_bg)

        fun bind(items_count: Int, number: Int, status: OrderStatus, date: String, order_number: String) {
            itemCount.text = "$items_count"
            itemNumber.text = "$number"
            orderStatus.text = status.toString()
            orderDate.text = date
            orderNumber.text = order_number

        }
    }

    fun addItem(pos: Int, item: MekoOrder) {
        orderItems.add(pos, item)
        notifyItemInserted(pos)
        redrawViews()
    }

    fun removeItem(pos: Int) {
        orderItems.removeAt(pos)
        notifyItemRemoved(pos)
        redrawViews()
    }

    private fun redrawViews() {
        notifyDataSetChanged()
    }

}

abstract class SwipeToDeleteCallback(context: Context)
    : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    private val deleteIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete_black_24dp)!!
    private val intrinsicWidth = deleteIcon.intrinsicWidth
    private val intrinsicHeight = deleteIcon.intrinsicHeight
    private val background = ColorDrawable()
    private val colorBG = ContextCompat.getColor(context, R.color.colorAccent)


    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?,
                        target: RecyclerView.ViewHolder?): Boolean = false

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                             actionState: Int, isCurrentlyActive: Boolean) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
//        Draw delete backgound
        background.color = colorBG
        background.setBounds(itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom)

        background.draw(c)

//        Calculate icon position
        val iconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val iconMargin = (itemHeight - intrinsicHeight) / 2

        val iconLeft = itemView.left + iconMargin
        val iconRight = itemView.left + iconMargin + intrinsicWidth

        val iconBottom = iconTop + intrinsicHeight

//        Draw delete icon
        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        deleteIcon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}

class RecyclerTouchListener(context: Context, clickListener: ClickListener) : RecyclerView.OnItemTouchListener {
    private var clickListener: ClickListener? = null
    private var gestureDetector: GestureDetector

    init {
        this.clickListener = clickListener
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return true
            }
        })
    }

    override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {}

    override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent): Boolean {
        val child = rv?.findChildViewUnder(e.x, e.y)
        if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
            clickListener?.onClick(child, rv.getChildAdapterPosition(child))

        }
        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

}

class Preferences {
    companion object {
        private var sharedPreferences: SharedPreferences? = null

        private const val PREFERENCE_NAME = "Loci preference"
        private const val PREFERENCE_COUNTRY = "Country"
        private const val PREFERENCE_COUNTRY_CODE = "Country code"
        private const val PREFERENCE_DIAL_CODE = "Country dial code"
        //        private const val PREFERENCE_CURRENCY_CODE = "Country currency code"
        private const val PREFERENCE_FRESH_INSTALL = "Install"
        private const val PREFERENCE_AVATAR_LOCATION = "User Avatar"
        private const val PREFERENCE_USER_NUMBER = "Number"
        private const val PREFERENCE_USER_NAME = "Name"
        //        private const val PREFERENCE_CRYPTO_TREND = "Crypto Trend"
//        private const val PREFERENCE_CRYPTO_TREND_COMPLETE = "Crypto trend complete"
//        private const val PREFERENCE_BACKGROUND_SYNC = "Background Sync"
        private const val PREFERENCE_NOTIFICATION_TONE_NAME = "Notification Tone name"
        private const val PREFERENCE_NOTIFICATION_URI = "Notification Tone URI"


        fun loadPreferences(context: Context) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }

        fun setPreferenceCountry(name: String) {
            sharedPreferences?.edit()?.putString(PREFERENCE_COUNTRY, name)?.apply()
        }

        fun setPreferenceCountryCode(country_code: String) {
            sharedPreferences?.edit()?.putString(PREFERENCE_COUNTRY_CODE, country_code)?.apply()
        }

        fun setPreferenceCountryDialCode(dial_code: String) {
            sharedPreferences?.edit()?.putString(PREFERENCE_DIAL_CODE, dial_code)?.apply()
        }

//        fun setPreferenceCountryCurrencyCode(currency_code: String) {
//            sharedPreferences?.edit()?.putString(PREFERENCE_CURRENCY_CODE, currency_code)?.apply()
//        }

        fun setPreferenceInstall(isNew: Boolean) {
            sharedPreferences?.edit()?.putBoolean(PREFERENCE_FRESH_INSTALL, isNew)?.apply()
        }

        fun getPreferenceCountry(): String {
            return sharedPreferences?.getString(PREFERENCE_COUNTRY, "Kenya")!!
        }

        fun getPreferenceCountryCode(): String {
            return sharedPreferences?.getString(PREFERENCE_COUNTRY_CODE, "KE")!!
        }

        fun getPreferenceDialCode(): String {
            return sharedPreferences?.getString(PREFERENCE_DIAL_CODE, "+254")!!
        }

//        fun getPreferenceCurrencyCode(): String {
//            return sharedPreferences?.getString(PREFERENCE_CURRENCY_CODE, "USD")!!
//        }

        fun isNewUser(): Boolean {
            return sharedPreferences?.getBoolean(PREFERENCE_FRESH_INSTALL, true)!!
        }

        fun setPreferenceToneName(toneName: String) {
            sharedPreferences?.edit()?.putString(PREFERENCE_NOTIFICATION_TONE_NAME, toneName)?.apply()
        }

        fun getPreferenceToneName(): String {
            return sharedPreferences?.getString(PREFERENCE_NOTIFICATION_TONE_NAME, "Default")!!
        }

        fun setPreferenceToneUri(toneUriString: String) {
            sharedPreferences?.edit()?.putString(PREFERENCE_NOTIFICATION_URI, toneUriString)?.apply()
        }

        fun getPreferenceToneUriString(): String? {
            return sharedPreferences?.getString(PREFERENCE_NOTIFICATION_URI, null)
        }

        fun setPreferenceAvatarLocation(locationString: String) {
            sharedPreferences?.edit()?.putString(PREFERENCE_AVATAR_LOCATION, locationString)?.apply()
        }

        fun getPreferenceAvatarLocation(): String? {
            return sharedPreferences?.getString(PREFERENCE_AVATAR_LOCATION, null)
        }

        fun setPreferenceNumber(number: String) {
            sharedPreferences?.edit()?.putString(PREFERENCE_USER_NUMBER, number)?.apply()
        }

        fun getPreferenceNumber(): String {
            return sharedPreferences?.getString(PREFERENCE_USER_NUMBER, "+1234")!!
        }

        fun setPreferenceName(name: String) {
            sharedPreferences?.edit()?.putString(PREFERENCE_USER_NAME, name)?.apply()
        }

        fun getPreferenceName(): String {
            return sharedPreferences?.getString(PREFERENCE_USER_NAME, "New user")!!
        }

    }
}


fun pop(context: Context, view: View) {
    val anim = AnimationUtils.loadAnimation(context, R.anim.pop)
    anim.interpolator = OvershootInterpolator()
    anim.fillAfter = true
    view.startAnimation(anim)
}

fun showToast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, length).show()
}

val addressList = ArrayList<Address>()

fun logData(message: String) {
    Log.d("MEKO", " $message")
}


val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
val mStorage: FirebaseStorage = FirebaseStorage.getInstance()
val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
val photoMetadata: StorageMetadata = StorageMetadata.Builder()
        .setContentType("image/jpeg")
        .build()

class FirebaseUtil {
    companion object {
        const val PROFILE_PHOTO_REF = "Images/Profile photos"
        const val USERS_REF = "Meko users"
        const val ORDERS_REF = "Meko orders"
        const val DELIVERY_FEE_KEY = "delivery_fee"
    }
}

class MekoFolders {
    companion object {
        var RECEIPTS_FOLDER: String = "${Environment.getExternalStorageDirectory().absolutePath}/Meko/Receipts"
    }
}

class SpinnerAdapter(context: Context, objects: Array<String>) : ArrayAdapter<String>(context, R.layout.spinner_item, objects), ThemedSpinnerAdapter {
    private val mDropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View

        view = if (convertView == null) {
            // Inflate the drop down using the helper's LayoutInflater
            val inflater = mDropDownHelper.dropDownViewInflater
            inflater.inflate(R.layout.spinner_item, parent, false)
        } else {
            convertView
        }

        view.text1.text = getItem(position)

        return view
    }

    override fun getDropDownViewTheme(): Resources.Theme? {
        return mDropDownHelper.dropDownViewTheme
    }

    override fun setDropDownViewTheme(theme: Resources.Theme?) {
        mDropDownHelper.dropDownViewTheme = theme
    }
}

class Receipts {
    companion object {
        fun generateReceipt(context: Context, receiptCode: String): Bitmap? {
            val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = manager.defaultDisplay
            val point = Point()
            display.getSize(point)
            val width = point.x
            val height = point.y
            var smallerDimension = if (width < height) width else height
            smallerDimension = smallerDimension * 3 / 4
            val encoder = QRGEncoder(receiptCode, null, QRGContents.Type.TEXT, smallerDimension)

            return try {
                encoder.encodeAsBitmap()

            } catch (e: Exception) {
                showToast(context, "Could not generate receipt")
                null
            }
        }

        fun saveReceipt(receipt: Bitmap, name: String) {
            try {
                val saved = QRGSaver.save(MekoFolders.RECEIPTS_FOLDER, name, receipt, ImageType.JPEG_IMAGE)
                if (saved) {
                    logData("Saved")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


}

//Shopping
var deliveryFee: Float = 0F
val mekoCart = ArrayList<FoodItem>()
val mekoOrders = ArrayList<MekoOrder>()
var selectedLocation: Location? = null

//Conversions
fun String.toKshs(): String = "Kshs $this"

fun View.snackBar(message: String, length: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, length).show()
}

fun getOrderTotal(orderItems: ArrayList<FoodItem>): Float {
    var total = 0f
    orderItems.forEach {
        total += it.price
    }
    return total
}