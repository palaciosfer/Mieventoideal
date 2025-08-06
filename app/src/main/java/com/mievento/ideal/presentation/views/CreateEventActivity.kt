package com.mievento.ideal.presentation.views

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.mievento.ideal.data.repositories.EventRepository
import com.mievento.ideal.presentation.presenters.CreateEventPresenter
import com.mievento.ideal.presentation.presenters.CreateEventView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CreateEventActivity : AppCompatActivity(), CreateEventView {

    private lateinit var presenter: CreateEventPresenter
    private lateinit var etName: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var btnDate: Button
    private lateinit var btnTime: Button
    private lateinit var etLocation: EditText
    private lateinit var etDescription: EditText
    private lateinit var etNotes: EditText
    private lateinit var etBudget: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var ivSelectedImage: ImageView
    private lateinit var btnRemoveImage: Button
    private lateinit var cbHighQuality: CheckBox
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var imageUploadProgress: ProgressBar

    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String? = null
    private var currentPhotoPath: String? = null
    private var selectedDate = ""
    private var selectedTime = ""
    private var isEditMode = false
    private var eventId = 0

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    companion object {
        private const val IMAGE_PICK_REQUEST = 200
        private const val CAMERA_REQUEST = 201
        private const val PERMISSION_REQUEST = 202
        private const val MANAGE_STORAGE_REQUEST = 203
        private const val TAG = "CreateEventActivity"

        // Permisos necesarios según versión de Android
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si es modo edición
        isEditMode = intent.getBooleanExtra("edit_mode", false)
        eventId = intent.getIntExtra("event_id", 0)

        val tokenManager = TokenManager(this)
        val eventRepository = EventRepository(tokenManager)
        presenter = CreateEventPresenter(this, eventRepository)

        setupUI()

        if (isEditMode && eventId > 0) {
            // TODO: Cargar datos del evento para editar
            // presenter.loadEvent(eventId)
        }
    }

    private fun setupUI() {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.WHITE)
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32)
        }

        // Toolbar
        val toolbar = createToolbar()

        // Info para admin
        val adminInfo = createAdminInfo()

        // Form fields
        createFormFields()

        // Sección de imagen
        val imageSection = createImageSection()

        // Botón guardar
        btnSave = Button(this).apply {
            text = if (isEditMode) "💾 Actualizar Evento" else "🎉 Crear Evento"
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            setPadding(32)
            textSize = 16f
            setOnClickListener { saveEvent() }
        }

        progressBar = ProgressBar(this).apply {
            gone()
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 16, 0, 16)
        }

        mainLayout.addView(toolbar)
        mainLayout.addView(adminInfo)
        mainLayout.addView(createSectionHeader("📋 Información Básica"))
        mainLayout.addView(createLabel("Nombre del Evento *"))
        mainLayout.addView(etName, layoutParams)
        mainLayout.addView(createLabel("Tipo de Evento *"))
        mainLayout.addView(spinnerType, layoutParams)
        mainLayout.addView(createLabel("Fecha del Evento *"))
        mainLayout.addView(btnDate, layoutParams)
        mainLayout.addView(createLabel("Hora del Evento *"))
        mainLayout.addView(btnTime, layoutParams)
        mainLayout.addView(createLabel("Ubicación *"))
        mainLayout.addView(etLocation, layoutParams)
        mainLayout.addView(createSectionHeader("📝 Detalles Adicionales"))
        mainLayout.addView(createLabel("Descripción"))
        mainLayout.addView(etDescription, layoutParams)
        mainLayout.addView(createLabel("Notas"))
        mainLayout.addView(etNotes, layoutParams)
        mainLayout.addView(createLabel("Presupuesto Base (MXN)"))
        mainLayout.addView(etBudget, layoutParams)
        mainLayout.addView(imageSection)
        mainLayout.addView(btnSave, layoutParams)
        mainLayout.addView(progressBar, layoutParams)

        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }

    private fun createToolbar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 32)
            gravity = Gravity.CENTER_VERTICAL

            val btnBack = Button(this@CreateEventActivity).apply {
                text = "← Volver"
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(Color.parseColor("#2196F3"))
                textSize = 16f
                setOnClickListener { finish() }
            }

            val title = TextView(this@CreateEventActivity).apply {
                text = if (isEditMode) "✏️ Editar Evento" else "🎉 Crear Nuevo Evento"
                textSize = 20f
                setTextColor(Color.parseColor("#212121"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER
            }

            addView(btnBack)
            addView(title)
        }
    }

    private fun createAdminInfo(): TextView {
        return TextView(this).apply {
            text = "🔧 Como administrador, puedes crear eventos atractivos con imágenes de alta calidad que los usuarios podrán solicitar"
            textSize = 14f
            setTextColor(Color.parseColor("#4CAF50"))
            setPadding(16, 16, 16, 24)
            setBackgroundColor(Color.parseColor("#E8F5E8"))
        }
    }

    private fun createFormFields() {
        etName = createEditText("Ej: Boda de María y Juan")

        spinnerType = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@CreateEventActivity,
                android.R.layout.simple_spinner_item,
                arrayOf("boda", "fiesta", "conferencia", "taller", "otro")
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        btnDate = Button(this).apply {
            text = "📅 Seleccionar Fecha *"
            setBackgroundColor(Color.parseColor("#E0E0E0"))
            setTextColor(Color.parseColor("#212121"))
            setPadding(32, 16, 32, 16)
            setOnClickListener { showDatePicker() }
        }

        btnTime = Button(this).apply {
            text = "🕐 Seleccionar Hora *"
            setBackgroundColor(Color.parseColor("#E0E0E0"))
            setTextColor(Color.parseColor("#212121"))
            setPadding(32, 16, 32, 16)
            setOnClickListener { showTimePicker() }
        }

        etLocation = createEditText("📍 Ej: Salón de Eventos El Jardín")
        etDescription = createEditText("📝 Describe los detalles del evento...").apply {
            minLines = 3
        }
        etNotes = createEditText("📌 Notas adicionales para el organizador...").apply {
            minLines = 2
        }
        etBudget = createEditText("💰 Ej: 50000").apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }

    private fun createImageSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.parseColor("#F8F9FA"))

            val imageHeader = createSectionHeader("🖼️ Imagen del Evento")

            val imageInfo = TextView(this@CreateEventActivity).apply {
                text = "Una buena imagen hace que tu evento sea más atractivo para los participantes"
                textSize = 12f
                setTextColor(Color.parseColor("#757575"))
                setPadding(16, 8, 16, 8)
            }

            // 🔥 NUEVO: Checkbox para alta calidad
            val qualityLayout = LinearLayout(this@CreateEventActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 8, 16, 16)
                setBackgroundColor(Color.parseColor("#E3F2FD"))
            }

            cbHighQuality = CheckBox(this@CreateEventActivity).apply {
                text = "🎯 Subir en alta calidad (menos compresión, archivo más grande)"
                textSize = 12f
                setTextColor(Color.parseColor("#1976D2"))
            }

            qualityLayout.addView(cbHighQuality)

            val buttonLayout = LinearLayout(this@CreateEventActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            btnSelectImage = Button(this@CreateEventActivity).apply {
                text = "📷 Galería"
                setBackgroundColor(Color.parseColor("#2196F3"))
                setTextColor(Color.WHITE)
                setPadding(24, 16, 24, 16)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(8, 0, 8, 0)
                }
                setOnClickListener { selectFromGallery() }
            }

            btnTakePhoto = Button(this@CreateEventActivity).apply {
                text = "📸 Cámara"
                setBackgroundColor(Color.parseColor("#FF9800"))
                setTextColor(Color.WHITE)
                setPadding(24, 16, 24, 16)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(8, 0, 8, 0)
                }
                setOnClickListener { takePhoto() }
            }

            buttonLayout.addView(btnSelectImage)
            buttonLayout.addView(btnTakePhoto)

            ivSelectedImage = ImageView(this@CreateEventActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    400
                ).apply {
                    setMargins(16, 16, 16, 8)
                }
                setBackgroundColor(Color.parseColor("#E0E0E0"))
                scaleType = ImageView.ScaleType.CENTER_CROP
                visibility = View.GONE
            }

            btnRemoveImage = Button(this@CreateEventActivity).apply {
                text = "🗑️ Quitar Imagen"
                setBackgroundColor(Color.parseColor("#F44336"))
                setTextColor(Color.WHITE)
                setPadding(16, 12, 16, 12)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setOnClickListener { removeImage() }
                visibility = View.GONE
            }

            imageUploadProgress = ProgressBar(this@CreateEventActivity).apply {
                gone()
            }

            addView(imageHeader)
            addView(imageInfo)
            addView(qualityLayout)
            addView(buttonLayout)
            addView(ivSelectedImage)
            addView(btnRemoveImage)
            addView(imageUploadProgress)
        }
    }

    private fun createSectionHeader(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.parseColor("#2196F3"))
            setPadding(0, 24, 0, 16)
        }
    }

    private fun createLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.parseColor("#757575"))
            setPadding(0, 16, 0, 8)
        }
    }

    private fun createEditText(hint: String): EditText {
        return EditText(this).apply {
            this.hint = hint
            setPadding(32)
            setBackgroundResource(android.R.drawable.edit_text)
        }
    }

    private fun selectFromGallery() {
        when {
            checkAllPermissions() -> {
                openGallery()
            }
            shouldShowRequestPermissionRationale() -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissions()
            }
        }
    }

    private fun takePhoto() {
        when {
            checkAllPermissions() -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale() -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissions()
            }
        }
    }

    private fun checkAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        return REQUIRED_PERMISSIONS.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST)
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permisos Necesarios")
            .setMessage("Para agregar imágenes a tu evento necesitamos permisos para:\n\n" +
                    "📷 Cámara: Para tomar fotos\n" +
                    "🖼️ Galería: Para seleccionar imágenes\n\n" +
                    "¿Deseas conceder estos permisos?")
            .setPositiveButton("Sí, conceder") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("No ahora") { dialog, _ ->
                dialog.dismiss()
                showToast("Sin permisos no puedes agregar imágenes")
            }
            .show()
    }

    private fun openGallery() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                    type = "image/*"
                }
            } else {
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                }
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, IMAGE_PICK_REQUEST)
            } else {
                showToast("No se encontró una aplicación de galería")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery", e)
            showToast("Error al abrir la galería")
        }
    }

    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (intent.resolveActivity(packageManager) != null) {
                val photoFile = createImageFile()
                photoFile?.let { file ->
                    val photoUri = FileProvider.getUriForFile(
                        this,
                        "com.mievento.ideal.fileprovider",
                        file
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(intent, CAMERA_REQUEST)
                }
            } else {
                showToast("No se encontró una aplicación de cámara")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera", e)
            showToast("Error al abrir la cámara")
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error creating image file", e)
            null
        }
    }

    private fun removeImage() {
        selectedImageUri = null
        uploadedImageUrl = null
        currentPhotoPath = null
        ivSelectedImage.setImageDrawable(null)
        ivSelectedImage.gone()
        btnRemoveImage.gone()
        btnSelectImage.text = "📷 Galería"
        btnTakePhoto.text = "📸 Cámara"
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = dateFormat.format(calendar.time)
                btnDate.text = "📅 " + displayDateFormat.format(calendar.time)
                btnDate.setBackgroundColor(Color.parseColor("#4CAF50"))
                btnDate.setTextColor(Color.WHITE)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 86400000
        }.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                selectedTime = timeFormat.format(calendar.time)
                btnTime.text = "🕐 $selectedTime"
                btnTime.setBackgroundColor(Color.parseColor("#4CAF50"))
                btnTime.setTextColor(Color.WHITE)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveEvent() {
        if (!validateForm()) return

        if (selectedImageUri != null && uploadedImageUrl == null) {
            uploadImageThenCreateEvent()
        } else {
            createEvent()
        }
    }

    private fun validateForm(): Boolean {
        when {
            etName.text.toString().trim().isEmpty() -> {
                showToast("Por favor ingresa el nombre del evento")
                etName.requestFocus()
                return false
            }
            selectedDate.isEmpty() -> {
                showToast("Por favor selecciona la fecha del evento")
                btnDate.requestFocus()
                return false
            }
            selectedTime.isEmpty() -> {
                showToast("Por favor selecciona la hora del evento")
                btnTime.requestFocus()
                return false
            }
            etLocation.text.toString().trim().isEmpty() -> {
                showToast("Por favor ingresa la ubicación del evento")
                etLocation.requestFocus()
                return false
            }
        }
        return true
    }

    private fun uploadImageThenCreateEvent() {
        imageUploadProgress.visible()
        btnSave.isEnabled = false

        try {
            // 🔥 MEJORADO: Usar compresión inteligente basada en calidad seleccionada
            val compressedFile = if (cbHighQuality.isChecked) {
                compressImageHighQuality(selectedImageUri!!)
            } else {
                compressImage(selectedImageUri!!)
            }

            if (compressedFile != null) {
                uploadImageToServer(compressedFile) { success, imageUrl ->
                    runOnUiThread {
                        imageUploadProgress.gone()
                        if (success && imageUrl != null) {
                            uploadedImageUrl = imageUrl
                            createEvent()
                        } else {
                            btnSave.isEnabled = true
                            showToast("Error al subir la imagen. El evento se creará sin imagen.")
                            createEvent()
                        }
                    }
                }
            } else {
                imageUploadProgress.gone()
                btnSave.isEnabled = true
                showToast("Error al procesar la imagen. Creando evento sin imagen.")
                createEvent()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al subir imagen", e)
            imageUploadProgress.gone()
            btnSave.isEnabled = true
            showToast("Error al subir la imagen. Creando evento sin imagen.")
            createEvent()
        }
    }

    // ✅ ACTUALIZADO: Compresión inteligente para imágenes grandes
    private fun compressImage(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e(TAG, "No se pudo decodificar la imagen")
                return null
            }

            Log.d(TAG, "Imagen original: ${originalBitmap.width}x${originalBitmap.height}")

            // 🔥 NUEVO: Ajustar tamaño máximo según resolución original
            val maxSize = when {
                originalBitmap.width > 4000 || originalBitmap.height > 4000 -> 2048 // Imágenes 4K+ → 2K
                originalBitmap.width > 2000 || originalBitmap.height > 2000 -> 1920 // Imágenes 2K+ → Full HD
                else -> 1280 // Imágenes menores → HD
            }

            val resizedBitmap = resizeBitmap(originalBitmap, maxSize)

            val compressedFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(compressedFile)

            // 🔥 NUEVO: Ajustar calidad según tamaño final
            val compressionQuality = when {
                resizedBitmap.width > 1920 -> 75 // Imágenes grandes → menor calidad
                resizedBitmap.width > 1280 -> 80 // Imágenes medianas → calidad media
                else -> 85 // Imágenes pequeñas → mejor calidad
            }

            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, outputStream)
            outputStream.close()

            // Limpiar memoria
            if (originalBitmap != resizedBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()

            Log.d(TAG, "Imagen comprimida: ${compressedFile.length() / 1024}KB (calidad: $compressionQuality%)")

            compressedFile
        } catch (e: Exception) {
            Log.e(TAG, "Error comprimiendo imagen", e)
            null
        }
    }

    // 🔥 NUEVO: Compresión de alta calidad para imágenes especiales
    private fun compressImageHighQuality(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e(TAG, "No se pudo decodificar la imagen")
                return null
            }

            Log.d(TAG, "Imagen original (alta calidad): ${originalBitmap.width}x${originalBitmap.height}")

            // Para alta calidad, mantener resolución mayor
            val maxSize = when {
                originalBitmap.width > 6000 || originalBitmap.height > 6000 -> 4096 // 6K+ → 4K
                originalBitmap.width > 4000 || originalBitmap.height > 4000 -> 3072 // 4K+ → 3K
                originalBitmap.width > 2000 || originalBitmap.height > 2000 -> 2048 // 2K+ → 2K
                else -> 1920 // Menores → Full HD
            }

            val resizedBitmap = resizeBitmap(originalBitmap, maxSize)

            val compressedFile = File(cacheDir, "high_quality_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(compressedFile)

            // Alta calidad: mayor compresión
            val compressionQuality = when {
                resizedBitmap.width > 3000 -> 85 // Muy grandes → 85%
                resizedBitmap.width > 2000 -> 90 // Grandes → 90%
                else -> 95 // Menores → 95%
            }

            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, outputStream)
            outputStream.close()

            // Limpiar memoria
            if (originalBitmap != resizedBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()

            Log.d(TAG, "Imagen alta calidad: ${compressedFile.length() / 1024}KB (calidad: $compressionQuality%)")

            compressedFile
        } catch (e: Exception) {
            Log.e(TAG, "Error comprimiendo imagen alta calidad", e)
            null
        }
    }

    // ✅ FUNCIÓN EXISTENTE: Redimensionar bitmap
    private fun resizeBitmap(originalBitmap: Bitmap, maxSize: Int): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        // Si ya es pequeña, no redimensionar
        if (width <= maxSize && height <= maxSize) {
            return originalBitmap
        }

        // Calcular nuevo tamaño manteniendo proporción
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / aspectRatio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * aspectRatio).toInt()
        }

        Log.d(TAG, "Redimensionando de ${width}x${height} a ${newWidth}x${newHeight}")

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
    }

    private fun createFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            file
        } catch (e: Exception) {
            Log.e(TAG, "Error creating file from URI", e)
            null
        }
    }

    private fun uploadImageToServer(file: File, callback: (Boolean, String?) -> Unit) {
        val fileSizeKB = file.length() / 1024
        Log.d(TAG, "📤 Subiendo imagen: ${file.name} (${fileSizeKB}KB)")

        // 🔥 ACTUALIZADO: Timeouts más largos para imágenes grandes
        val client = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS) // 2 minutos
            .writeTimeout(300, TimeUnit.SECONDS)   // 5 minutos para subir
            .readTimeout(120, TimeUnit.SECONDS)    // 2 minutos para respuesta
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d(TAG, "🌐 Request URL: ${request.url}")
                Log.d(TAG, "🌐 Request Method: ${request.method}")
                val response = chain.proceed(request)
                Log.d(TAG, "📡 Response Code: ${response.code}")
                response
            }
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                file.name,
                file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("http://3.208.254.174/api/events/upload-image")
            .post(requestBody)
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Android-MiEvento/1.0")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "❌ Network failure", e)
                callback(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "📥 Response (${response.code}): $responseBody")

                    when (response.code) {
                        200 -> {
                            val imageUrl = extractImageUrlFromResponse(responseBody)
                            if (imageUrl != null) {
                                Log.d(TAG, "✅ Upload exitoso: $imageUrl")
                                callback(true, imageUrl)
                            } else {
                                Log.e(TAG, "❌ No se pudo extraer URL")
                                callback(false, null)
                            }
                        }
                        413 -> {
                            Log.e(TAG, "❌ Archivo demasiado grande")
                            callback(false, "Archivo demasiado grande")
                        }
                        500 -> {
                            Log.e(TAG, "❌ Error del servidor: $responseBody")
                            callback(false, "Error del servidor")
                        }
                        else -> {
                            Log.e(TAG, "❌ Error ${response.code}: $responseBody")
                            callback(false, "Error de conexión")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error procesando respuesta", e)
                    callback(false, null)
                }
            }
        })
    }

    // Función helper para extraer la URL de la respuesta JSON
    private fun extractImageUrlFromResponse(responseBody: String?): String? {
        return try {
            responseBody?.let { body ->
                val imageUrlPattern = "\"image_url\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val matchResult = imageUrlPattern.find(body)
                matchResult?.groupValues?.get(1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting image URL", e)
            null
        }
    }

    private fun createEvent() {
        presenter.createEvent(
            name = etName.text.toString().trim(),
            type = spinnerType.selectedItem.toString(),
            date = selectedDate,
            time = selectedTime,
            location = etLocation.text.toString().trim(),
            description = etDescription.text.toString().trim(),
            notes = etNotes.text.toString().trim(),
            budget = etBudget.text.toString().trim(),
            imageUrl = uploadedImageUrl
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST -> {
                val deniedPermissions = permissions.filterIndexed { index, _ ->
                    grantResults[index] != PackageManager.PERMISSION_GRANTED
                }

                when {
                    deniedPermissions.isEmpty() -> {
                        showToast("✅ Permisos concedidos")
                    }
                    deniedPermissions.any { permission ->
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                    } -> {
                        showGoToSettingsDialog()
                    }
                    else -> {
                        showToast("Permisos denegados. No puedes agregar imágenes.")
                    }
                }
            }
        }
    }

    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permisos Requeridos")
            .setMessage("Para usar esta función, necesitas habilitar los permisos en Configuración.\n\n" +
                    "Ve a: Configuración > Aplicaciones > Mi Evento Ideal > Permisos")
            .setPositiveButton("Ir a Configuración") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivityForResult(intent, MANAGE_STORAGE_REQUEST)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app settings", e)
            showToast("No se pudo abrir la configuración")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_REQUEST -> {
                    data?.data?.let { uri ->
                        selectedImageUri = uri
                        displaySelectedImage(uri)
                        Log.d(TAG, "Gallery image selected: $uri")
                    } ?: run {
                        showToast("No se pudo obtener la imagen de la galería")
                    }
                }
                CAMERA_REQUEST -> {
                    currentPhotoPath?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            selectedImageUri = Uri.fromFile(file)
                            displaySelectedImage(selectedImageUri!!)
                            Log.d(TAG, "Camera photo saved: $path")
                        } else {
                            showToast("No se pudo guardar la foto")
                        }
                    } ?: run {
                        val imageBitmap = data?.extras?.get("data") as? Bitmap
                        imageBitmap?.let { bitmap ->
                            val uri = saveBitmapToFile(bitmap)
                            if (uri != null) {
                                selectedImageUri = uri
                                displaySelectedImage(uri)
                            }
                        } ?: showToast("No se pudo obtener la foto")
                    }
                }
                MANAGE_STORAGE_REQUEST -> {
                    if (checkAllPermissions()) {
                        showToast("¡Permisos configurados! Ahora puedes agregar imágenes.")
                    }
                }
            }
        } else {
            when (requestCode) {
                IMAGE_PICK_REQUEST -> Log.d(TAG, "Gallery selection cancelled")
                CAMERA_REQUEST -> Log.d(TAG, "Camera capture cancelled")
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        return try {
            val file = createImageFile()
            file?.let {
                val outputStream = FileOutputStream(it)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.close()
                Uri.fromFile(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap to file", e)
            null
        }
    }

    private fun displaySelectedImage(uri: Uri) {
        try {
            Log.d(TAG, "🖼️ Displaying image: $uri")

            // Versión simplificada sin RequestListener
            Glide.with(this)
                .load(uri)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                .centerCrop()
                .into(ivSelectedImage)

            ivSelectedImage.visible()
            btnRemoveImage.visible()
            btnSelectImage.text = "✅ Imagen Seleccionada"
            btnTakePhoto.text = "📸 Cambiar Foto"

            Log.d(TAG, "Image displayed successfully: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying image", e)
            showToast("Error al mostrar la imagen")
        }
    }

    override fun showLoading() {
        progressBar.visible()
        btnSave.isEnabled = false
    }

    override fun hideLoading() {
        progressBar.gone()
        btnSave.isEnabled = true
    }

    override fun showError(message: String) {
        showToast(message)
    }

    override fun onEventCreated() {
        showToast("🎉 Evento ${if (isEditMode) "actualizado" else "creado"} exitosamente")
        setResult(RESULT_OK)
        finish()
    }
}