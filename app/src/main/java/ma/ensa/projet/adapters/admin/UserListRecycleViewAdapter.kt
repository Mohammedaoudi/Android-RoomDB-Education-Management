package ma.ensa.projet.adapters.admin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ma.ensa.projet.R
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.LecturerAndUser
import ma.ensa.projet.data.entities.User
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
import java.util.Calendar

import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList

class UserListRecycleViewAdapter(
    private val context: Context,
    private val originalList: ArrayList<User>
) : RecyclerView.Adapter<UserListRecycleViewAdapter.LecturerViewHolder>(), Filterable {

    private val bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context)
    private var currentFilterText: String = ""
    private var selectedRole: Constants.Role? = null
    private var filteredList: ArrayList<User> = originalList
    private var selectedImageUri: Uri? = null
    private var currentEditingUser: User? = null


    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): LecturerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.admin_layout_recycle_view_list_lecturer, parent, false)
        return LecturerViewHolder(view)
    }

    override fun onBindViewHolder( holder: LecturerViewHolder, position: Int) {
        val user = filteredList[position]
        holder.avatar.setImageBitmap(user.avatar?.let { Utils.getBitmapFromBytes(it) })
        holder.txtUsername.text = user.fullName
        holder.txtRoleName.text = Utils.getRoleName(user.role)

        holder.btnEditUser.setOnClickListener { showEditLecturerDialog(user) }

        holder.btnDeleteUser.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Notification")
                .setMessage("Confirm delete user?")
                .setPositiveButton("Yes") { dialog, _ -> deleteUser(user) }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                currentFilterText = charSequence.toString()
                val query = Utils.removeVietnameseAccents(charSequence.toString().lowercase(Locale.getDefault()))

                if (query != null) {
                    filteredList = if (query.isEmpty()) {
                        originalList
                    } else {
                        originalList.filter { user ->
                            Utils.removeVietnameseAccents(user.fullName?.lowercase(Locale.getDefault()))
                                ?.contains(query) == true
                        } as ArrayList<User>
                    }
                }

                return FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredList = filterResults.values as ArrayList<User>
                notifyDataSetChanged()
            }
        }
    }



    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            val avatar: ImageView? = bottomSheetDialog.findViewById(R.id.avatar)
            avatar?.setImageURI(uri)

            // Update the user's avatar in the database
            currentEditingUser?.let { user ->
                try {
                    // Get bitmap from ImageView and convert to bytes
                    val avatarView = bottomSheetDialog.findViewById<ImageView>(R.id.avatar)
                    if (avatarView != null) {
                        val avatarBytes = Utils.getBytesFromBitmap(Utils.getBitmapFromView(avatarView))
                        user.avatar = avatarBytes

                        // Use coroutine to update in database
                        CoroutineScope(Dispatchers.IO).launch {
                            AppDatabase.getInstance(context)?.userDAO()?.update(user)

                            // Update the RecyclerView on the main thread
                            withContext(Dispatchers.Main) {
                                notifyItemChanged(originalList.indexOf(user))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Utils.showToast(context, "Failed to update image: ${e.message}")
                }
            }
        }
    }

    fun addUser(lecturerAndUser: LecturerAndUser) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId: Long
            try {
                userId = AppDatabase.getInstance(context)?.userDAO()?.insert(lecturerAndUser.user)!!
            } catch (ex: SQLiteConstraintException) {
                withContext(Dispatchers.Main) {
                    Utils.showToast(context, "Email already exists!")
                }
                return@launch
            }

            lecturerAndUser.lecturer?.let {
                it.userId = userId
                AppDatabase.getInstance(context)?.lecturerDAO()?.insert(it)
            }

            withContext(Dispatchers.Main) {
                originalList.add(0, lecturerAndUser.user)
                notifyItemInserted(0)
            }
        }
    }

    private fun editUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(context)?.userDAO()?.update(user)

            // Update the RecyclerView on the main thread
            withContext(Dispatchers.Main) {
                getFilter().filter(currentFilterText)
                notifyItemChanged(originalList.indexOf(user))
            }
        }
    }

    private fun deleteUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            if (user.role == Constants.Role.LECTURER) {
                AppDatabase.getInstance(context)?.lecturerDAO()?.deleteByUser(user.id)
            }
            AppDatabase.getInstance(context)?.userDAO()?.delete(user)

            withContext(Dispatchers.Main) {
                originalList.remove(user)
                filteredList.remove(user)
                getFilter().filter(currentFilterText)
                notifyItemRemoved(originalList.indexOf(user))
            }
        }
    }


    @SuppressLint("InflateParams")
    private fun showEditLecturerDialog( user: User) {
        currentEditingUser = user  // Store the current editing user
        selectedImageUri = null
        val view = LayoutInflater.from(context).inflate(R.layout.admin_bottom_sheet_edit_user, null)
        bottomSheetDialog.setContentView(view)
        val behavior = BottomSheetBehavior.from((view.parent as View))
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
        bottomSheetDialog.show()

        val iconCamera: ImageView = view.findViewById(R.id.iconCamera)
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val edtEmail: EditText = view.findViewById(R.id.edtEmail)
        val edtFullName: EditText = view.findViewById(R.id.edtFullName)
        val txtRole: TextView = view.findViewById(R.id.txtRole)
        val radioGroupGender: RadioGroup = view.findViewById(R.id.radioGroupGender)
        val edtDob: EditText = view.findViewById(R.id.edtDob)
        val edtAddress: EditText = view.findViewById(R.id.edtAddress)
        val edtSpecialization: EditText = view.findViewById(R.id.edtSpecialization)

        val btnEdit: Button = view.findViewById(R.id.btnEditUser)

        selectedRole = user.role

        (edtSpecialization.parent as LinearLayout).visibility = View.GONE


        avatar.setImageBitmap(user.avatar?.let { Utils.getBitmapFromBytes(it) })
        edtEmail.setText(user.email)
        edtFullName.setText(user.fullName)
        txtRole.text = Utils.getRoleName(user.role)

        if (user.gender == Constants.MALE) {
            radioGroupGender.check(R.id.radioButtonMale)
        } else {
            radioGroupGender.check(R.id.radioButtonFemale)
        }
        edtDob.setText(Utils.formatDate("dd/MM/YYYY").format(user.dob))
        edtAddress.setText(user.address)

        if (user.role == Constants.Role.LECTURER) {
            CoroutineScope(Dispatchers.IO).launch {
                val lecturer = AppDatabase.getInstance(context)?.lecturerDAO()?.getByUser(user.id)
                withContext(Dispatchers.Main) {
                    edtSpecialization.visibility = View.VISIBLE
                    if (lecturer != null) {
                        edtSpecialization.setText(lecturer.specialization)
                    }
                }
            }
        }

        iconCamera.setOnClickListener {
            ImagePicker.with(context as Activity)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        val roleNames = arrayOf("Administrator", "Specialist", "Lecturer")
        txtRole.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Select Role")
                .setItems(roleNames) { dialog, which ->
                    txtRole.text = roleNames[which]

                    when (which) {
                        0 -> {
                            selectedRole = Constants.Role.ADMIN
                            (edtSpecialization.parent as LinearLayout).visibility = View.GONE

                        }

                        1 -> {
                            selectedRole = Constants.Role.LECTURER
                            (edtSpecialization.parent as LinearLayout).visibility = View.VISIBLE

                        }
                    }
                }
                .show()
        }

        edtDob.setOnClickListener { showDatePickerDialog(it) }

        btnEdit.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Notification")
                .setMessage("Confirm update user?")
                .setPositiveButton("Yes") { dialog, _ ->
                    updateUser(user, edtEmail, edtFullName, radioGroupGender, edtDob, edtAddress, edtSpecialization)
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun showDatePickerDialog(view: View) {

        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                (view as EditText).setText(Utils.formatDate("dd/MM/yyyy").format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateUser(
        user: User,
        edtEmail: EditText,
        edtFullName: EditText,
        radioGroupGender: RadioGroup,
        edtDob: EditText,
        edtAddress: EditText,
        edtSpecialization: EditText
    ) {
        // Update user details from input fields
        user.email = edtEmail.text.toString()
        user.fullName = edtFullName.text.toString()
        user.gender = if (radioGroupGender.checkedRadioButtonId == R.id.radioButtonMale) Constants.MALE else Constants.FEMALE

        try {
            user.dob = Utils.formatDate("dd/MM/yyyy").parse(edtDob.text.toString())!!
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        user.address = edtAddress.text.toString()
        user.role = selectedRole!!

        // Run database operations in the background
        CoroutineScope(Dispatchers.IO).launch {
            // If the user role is Lecturer, update the lecturer's specialization
            if (user.role == Constants.Role.LECTURER) {
                val db = AppDatabase.getInstance(context)
                val lecturer = db?.lecturerDAO()?.getByUser(user.id)
                lecturer?.let {
                    it.specialization = edtSpecialization.text.toString()
                    db.lecturerDAO().update(it)
                }
            }

            // Update the user in the database
            editUser(user)

            // UI updates must happen on the main thread
            withContext(Dispatchers.Main) {
                currentEditingUser = null
                selectedImageUri = null
                bottomSheetDialog.dismiss()
            }
        }
    }


    class LecturerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val txtUsername: TextView = view.findViewById(R.id.txtUsername)
        val txtRoleName: TextView = view.findViewById(R.id.txtRoleName)
        val btnEditUser: Button = view.findViewById(R.id.btnEditUser)
        val btnDeleteUser: Button = view.findViewById(R.id.btnDeleteUser)
    }
}
