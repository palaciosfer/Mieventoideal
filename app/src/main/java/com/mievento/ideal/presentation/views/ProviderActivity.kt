package com.mievento.ideal.presentation.views

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mievento.ideal.data.models.Provider
import com.mievento.ideal.data.repositories.ProviderRepository
import com.mievento.ideal.presentation.adapters.ProviderAdapter
import com.mievento.ideal.presentation.presenters.ProviderListPresenter
import com.mievento.ideal.presentation.presenters.ProviderListView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class ProviderActivity : AppCompatActivity(), ProviderListView {

    private lateinit var presenter: ProviderListPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var providerAdapter: ProviderAdapter

    private var eventId = 0
    private var eventName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventId = intent.getIntExtra("event_id", 0)
        eventName = intent.getStringExtra("event_name") ?: "Evento"

        val tokenManager = TokenManager(this)
        val providerRepository = ProviderRepository(tokenManager)
        presenter = ProviderListPresenter(this, providerRepository)

        setupUI()
        presenter.loadProviders(eventId)
    }

    private fun setupUI() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // Toolbar
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 48, 32, 24)
            setBackgroundColor(Color.parseColor("#2196F3"))
            gravity = Gravity.CENTER_VERTICAL
        }

        val btnBack = Button(this).apply {
            text = "←"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.WHITE)
            textSize = 20f
            setOnClickListener { finish() }
        }

        val title = TextView(this).apply {
            text = "Proveedores - $eventName"
            textSize = 18f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(32, 0, 0, 0)
        }

        toolbar.addView(btnBack)
        toolbar.addView(title)

        // Content container
        val contentContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        // RecyclerView
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@ProviderActivity)
            setPadding(16, 16, 16, 16)
        }

        // Empty view
        emptyView = TextView(this).apply {
            text = "No hay proveedores aún.\n¡Agrega el primer proveedor!"
            textSize = 16f
            setTextColor(Color.parseColor("#757575"))
            gravity = Gravity.CENTER
            setPadding(32)
            gone()
        }

        // Progress bar
        progressBar = ProgressBar(this).apply {
            gone()
        }

        // FAB
        fab = FloatingActionButton(this).apply {
            setImageResource(android.R.drawable.ic_input_add)
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setOnClickListener {
                showCreateProviderDialog()
            }
        }

        contentContainer.addView(recyclerView)
        contentContainer.addView(emptyView)
        contentContainer.addView(progressBar, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))
        contentContainer.addView(fab, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.END
        ).apply {
            setMargins(0, 0, 48, 48)
        })

        mainLayout.addView(toolbar)
        mainLayout.addView(contentContainer)

        setContentView(mainLayout)

        // Setup adapter
        providerAdapter = ProviderAdapter(
            providers = emptyList(),
            onProviderClick = { provider -> showEditProviderDialog(provider) },
            onProviderLongClick = { provider -> showProviderOptionsDialog(provider) }
        )
        recyclerView.adapter = providerAdapter
    }

    private fun showProviderOptionsDialog(provider: Provider) {
        val options = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle(provider.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditProviderDialog(provider)
                    1 -> showDeleteDialog(provider)
                }
            }
            .show()
    }

    private fun showDeleteDialog(provider: Provider) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Proveedor")
            .setMessage("¿Estás seguro de que quieres eliminar '${provider.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                presenter.deleteProvider(provider.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun showLoading() {
        progressBar.visible()
        recyclerView.gone()
        emptyView.gone()
    }

    override fun hideLoading() {
        progressBar.gone()
    }

    override fun showError(message: String) {
        showToast(message)
    }

    override fun showProviders(providers: List<Provider>) {
        if (providers.isEmpty()) {
            recyclerView.gone()
            emptyView.visible()
        } else {
            emptyView.gone()
            recyclerView.visible()
            providerAdapter.updateProviders(providers)
        }
    }

    override fun onProviderCreated() {
        showToast("Proveedor agregado")
    }

    override fun onProviderUpdated() {
        showToast("Proveedor actualizado")
        presenter.loadProviders(eventId)
    }

    override fun onProviderDeleted() {
        showToast("Proveedor eliminado")
        presenter.loadProviders(eventId)
    }

    override fun showCreateProviderDialog() {
        showProviderFormDialog(null)
    }

    override fun showEditProviderDialog(provider: Provider) {
        showProviderFormDialog(provider)
    }

    private fun showProviderFormDialog(provider: Provider?) {
        val scrollView = ScrollView(this)
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val etName = EditText(this).apply {
            hint = "Nombre *"
            setText(provider?.name ?: "")
        }

        val etService = EditText(this).apply {
            hint = "Servicio *"
            setText(provider?.service ?: "")
        }

        val etContactName = EditText(this).apply {
            hint = "Nombre de contacto"
            setText(provider?.contact_name ?: "")
        }

        val etPhone = EditText(this).apply {
            hint = "Teléfono"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setText(provider?.phone ?: "")
        }

        val etEmail = EditText(this).apply {
            hint = "Email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(provider?.email ?: "")
        }

        val etBudget = EditText(this).apply {
            hint = "Presupuesto estimado"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(provider?.estimated_budget?.toString() ?: "")
        }

        val etNotes = EditText(this).apply {
            hint = "Notas"
            setText(provider?.notes ?: "")
        }

        dialogView.addView(etName)
        dialogView.addView(etService)
        dialogView.addView(etContactName)
        dialogView.addView(etPhone)
        dialogView.addView(etEmail)
        dialogView.addView(etBudget)
        dialogView.addView(etNotes)

        scrollView.addView(dialogView)

        AlertDialog.Builder(this)
            .setTitle(if (provider == null) "Agregar Proveedor" else "Editar Proveedor")
            .setView(scrollView)
            .setPositiveButton(if (provider == null) "Agregar" else "Actualizar") { _, _ ->
                if (provider == null) {
                    presenter.createProvider(
                        eventId,
                        etName.text.toString(),
                        etService.text.toString(),
                        etContactName.text.toString(),
                        etPhone.text.toString(),
                        etEmail.text.toString(),
                        etBudget.text.toString(),
                        etNotes.text.toString()
                    )
                } else {
                    presenter.updateProvider(
                        provider.id,
                        etName.text.toString(),
                        etService.text.toString(),
                        etContactName.text.toString(),
                        etPhone.text.toString(),
                        etEmail.text.toString(),
                        etBudget.text.toString(),
                        etNotes.text.toString()
                    )
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}