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
import com.mievento.ideal.data.models.Guest
import com.mievento.ideal.data.repositories.GuestRepository
import com.mievento.ideal.presentation.adapters.GuestAdapter
import com.mievento.ideal.presentation.presenters.GuestListPresenter
import com.mievento.ideal.presentation.presenters.GuestListView
import com.mievento.ideal.utils.TokenManager
import com.mievento.ideal.utils.gone
import com.mievento.ideal.utils.showToast
import com.mievento.ideal.utils.visible

class GuestActivity : AppCompatActivity(), GuestListView {

    private lateinit var presenter: GuestListPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var guestAdapter: GuestAdapter

    private var eventId = 0
    private var eventName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventId = intent.getIntExtra("event_id", 0)
        eventName = intent.getStringExtra("event_name") ?: "Evento"

        val tokenManager = TokenManager(this)
        val guestRepository = GuestRepository(tokenManager)
        presenter = GuestListPresenter(this, guestRepository)

        setupUI()
        presenter.loadGuests(eventId)
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
            text = "Invitados - $eventName"
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
            layoutManager = LinearLayoutManager(this@GuestActivity)
            setPadding(16, 16, 16, 16)
        }

        // Empty view
        emptyView = TextView(this).apply {
            text = "No hay invitados aún.\n¡Agrega el primer invitado!"
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
                showCreateGuestDialog()
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
        guestAdapter = GuestAdapter(
            emptyList(),
            onGuestClick = { guest -> showEditGuestDialog(guest) },
            onGuestLongClick = { guest -> showGuestOptionsDialog(guest) }
        )
        recyclerView.adapter = guestAdapter
    }

    private fun showGuestOptionsDialog(guest: Guest) {
        val options = arrayOf("Editar", "Cambiar Confirmación", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle(guest.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditGuestDialog(guest)
                    1 -> showConfirmationDialog(guest)
                    2 -> showDeleteDialog(guest)
                }
            }
            .show()
    }

    private fun showConfirmationDialog(guest: Guest) {
        val options = arrayOf("Confirmado", "Pendiente", "Rechazado")
        val currentIndex = when (guest.confirmation_status) {
            "confirmado" -> 0
            "pendiente" -> 1
            "rechazado" -> 2
            else -> 1
        }

        AlertDialog.Builder(this)
            .setTitle("Estado de Confirmación")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                val newStatus = when (which) {
                    0 -> "confirmado"
                    1 -> "pendiente"
                    2 -> "rechazado"
                    else -> "pendiente"
                }
                presenter.updateGuestConfirmation(guest.id, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteDialog(guest: Guest) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Invitado")
            .setMessage("¿Estás seguro de que quieres eliminar a '${guest.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                presenter.deleteGuest(guest.id)
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

    override fun showGuests(guests: List<Guest>) {
        if (guests.isEmpty()) {
            recyclerView.gone()
            emptyView.visible()
        } else {
            emptyView.gone()
            recyclerView.visible()
            guestAdapter.updateGuests(guests)
        }
    }

    override fun onGuestCreated() {
        showToast("Invitado agregado")
    }

    override fun onGuestUpdated() {
        showToast("Invitado actualizado")
        presenter.loadGuests(eventId)
    }

    override fun onGuestDeleted() {
        showToast("Invitado eliminado")
        presenter.loadGuests(eventId)
    }

    override fun showCreateGuestDialog() {
        showGuestFormDialog(null)
    }

    override fun showEditGuestDialog(guest: Guest) {
        showGuestFormDialog(guest)
    }

    private fun showGuestFormDialog(guest: Guest?) {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val etName = EditText(this).apply {
            hint = "Nombre *"
            setText(guest?.name ?: "")
        }

        val etEmail = EditText(this).apply {
            hint = "Email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(guest?.email ?: "")
        }

        val etPhone = EditText(this).apply {
            hint = "Teléfono"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setText(guest?.phone ?: "")
        }

        val etRelationship = EditText(this).apply {
            hint = "Relación"
            setText(guest?.relationship ?: "")
        }

        val etNotes = EditText(this).apply {
            hint = "Notas"
            setText(guest?.notes ?: "")
        }

        dialogView.addView(etName)
        dialogView.addView(etEmail)
        dialogView.addView(etPhone)
        dialogView.addView(etRelationship)
        dialogView.addView(etNotes)

        AlertDialog.Builder(this)
            .setTitle(if (guest == null) "Agregar Invitado" else "Editar Invitado")
            .setView(dialogView)
            .setPositiveButton(if (guest == null) "Agregar" else "Actualizar") { _, _ ->
                if (guest == null) {
                    presenter.createGuest(
                        eventId,
                        etName.text.toString(),
                        etEmail.text.toString(),
                        etPhone.text.toString(),
                        etRelationship.text.toString(),
                        etNotes.text.toString()
                    )
                } else {
                    presenter.updateGuest(
                        guest.id,
                        etName.text.toString(),
                        etEmail.text.toString(),
                        etPhone.text.toString(),
                        etRelationship.text.toString(),
                        etNotes.text.toString()
                    )
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}