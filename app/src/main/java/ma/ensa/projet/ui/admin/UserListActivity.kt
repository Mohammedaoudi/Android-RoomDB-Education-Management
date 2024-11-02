package ma.ensa.projet.ui.admin

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ma.ensa.projet.R
import ma.ensa.projet.adapters.admin.UserListRecycleViewAdapter
import ma.ensa.projet.data.AppDatabase
import ma.ensa.projet.data.dto.LecturerAndUser
import ma.ensa.projet.data.entities.Lecturer
import ma.ensa.projet.data.entities.User
import ma.ensa.projet.utilities.Constants
import ma.ensa.projet.utilities.Utils
import ma.ensa.projet.utilities.Validator
import java.text.ParseException
import java.util.Calendar
import java.util.function.Supplier
import java.util.stream.Collectors

class UserListActivity : AppCompatActivity() {
    private var userId: Long = 0
    private var selectedRole: Constants.Role? = null
    private var userListRecycleViewAdapter: UserListRecycleViewAdapter? = null

    private var layoutLecturer: LinearLayout? = null
    private var btnBack: ImageView? = null
    private var searchViewLecturer: SearchView? = null
    private var btnAddUser: Button? = null

    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_list_user)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View>(R.id.layoutLecturer)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initLecturerListView()
        handleEventListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null && data.data != null) {
            val uri = data.data
            val avatar = bottomSheetDialog!!.findViewById<ImageView>(R.id.avatar)
            avatar?.setImageURI(uri)
        }
        userListRecycleViewAdapter?.onActivityResult(requestCode, resultCode, data)
    }



    private fun initLecturerListView() {
        val intent = intent
        val bundle = checkNotNull(intent.extras)
        userId = bundle.getLong(Constants.USER_ID, 0)

        layoutLecturer = findViewById<LinearLayout>(R.id.layoutLecturer)
        btnBack = findViewById<ImageView>(R.id.btnBack)
        searchViewLecturer = findViewById<SearchView>(R.id.searchViewLecturer)
        btnAddUser = findViewById<Button>(R.id.btnAddUser)
        bottomSheetDialog = BottomSheetDialog(this)

        lifecycleScope.launch {
            // Fetch users asynchronously on a background thread
            val fetchedUsers = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@UserListActivity)
                    ?.userDAO()
                    ?.getAllRoleWithoutSelectedRole(Constants.Role.STUDENT)
                    ?.filter { it.id != userId }
                    ?.toCollection(ArrayList())
            }

            // Update the UI with the fetched data
            val rvLecturer = findViewById<RecyclerView>(R.id.rvLecturer)
            userListRecycleViewAdapter = fetchedUsers?.let { UserListRecycleViewAdapter(this@UserListActivity, it) }
            rvLecturer.layoutManager = LinearLayoutManager(this@UserListActivity)
            rvLecturer.adapter = userListRecycleViewAdapter
        }
    }


    private fun handleEventListeners() {
        layoutLecturer!!.setOnClickListener { v: View ->
            if (v.id == R.id.layoutLecturer) {
                searchViewLecturer!!.clearFocus()
            }
        }

        btnBack!!.setOnClickListener { v: View? -> finish() }

        searchViewLecturer!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                userListRecycleViewAdapter?.getFilter()?.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                userListRecycleViewAdapter?.getFilter()?.filter(newText)
                return false
            }
        })

        btnAddUser!!.setOnClickListener { v: View? -> showAddLecturerDialog() }
    }

    @SuppressLint("InflateParams")
    private fun showAddLecturerDialog() {
        val view: View =
            LayoutInflater.from(this).inflate(R.layout.admin_bottom_sheet_add_user, null)
        bottomSheetDialog!!.setContentView(view)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false
        bottomSheetDialog!!.show()

        view.findViewById<View>(R.id.iconCamera).setOnClickListener { v: View? ->
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        val roleNames = arrayOf("Administrator", "Lecturer")
        view.findViewById<View>(R.id.txtRole).setOnClickListener { v: View? ->
            AlertDialog.Builder(this)
                .setTitle("Select Role")
                .setItems(
                    roleNames
                ) { dialog: DialogInterface?, which: Int ->
                    (view.findViewById<View>(
                        R.id.txtRole
                    ) as EditText).setText(roleNames[which])
                    when (which) {
                        0 -> {
                            selectedRole = Constants.Role.ADMIN
                            (view.findViewById<View>(R.id.edtSpecialization)
                                .parent as LinearLayout).visibility = View.GONE

                        }


                        1 -> {
                            selectedRole = Constants.Role.LECTURER
                            (view.findViewById<View>(R.id.edtSpecialization)
                                .parent as LinearLayout).visibility = View.VISIBLE

                        }
                    }
                }
                .show()
        }

        view.findViewById<View>(R.id.edtDob).setOnClickListener { view: View ->
            this.showDatePickerDialog(
                view
            )
        }

        view.findViewById<View>(R.id.btnAddUser).setOnClickListener { v: View? ->
            AlertDialog.Builder(this)
                .setTitle("Notification")
                .setMessage("Add new user?")
                .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                    performAddUser(view)
                }
                .setNegativeButton("No", null)
                .show()
        }

    }

    private fun performAddUser(view: View) {
        if (!validateInputs(view)) return

        val lecturerAndUser = LecturerAndUser(
            user = User(), // Initialize user
            lecturer = Lecturer(userId = 0)
        )

        try {
            lecturerAndUser.user.avatar = Utils.getBytesFromBitmap(Utils.getBitmapFromView(view.findViewById(R.id.avatar)))
            lecturerAndUser.user.email = (view.findViewById<EditText>(R.id.edtEmail)).text.toString()
            lecturerAndUser.user.password = Utils.hashPassword("123456")
            lecturerAndUser.user.fullName = (view.findViewById<EditText>(R.id.edtFullName)).text.toString()
            lecturerAndUser.user.dob = Utils.formatDate("dd/MM/YYYY").parse((view.findViewById<EditText>(R.id.edtDob)).text.toString())
            lecturerAndUser.user.address = (view.findViewById<EditText>(R.id.edtAddress)).text.toString()
            lecturerAndUser.user.role = selectedRole
            val genderId = (view.findViewById<RadioGroup>(R.id.radioGroupGender)).checkedRadioButtonId
            lecturerAndUser.user.gender = if (genderId == R.id.radioButtonMale) Constants.MALE else Constants.FEMALE

            if (selectedRole == Constants.Role.LECTURER) {
                lecturerAndUser.lecturer?.specialization = (view.findViewById<EditText>(R.id.edtSpecialization)).text.toString()

            }
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }

        userListRecycleViewAdapter?.addUser(lecturerAndUser)
        bottomSheetDialog!!.dismiss()
        Utils.showToast(this, "Added successfully")
    }

    private fun showDatePickerDialog(view: View) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { v: DatePicker?, year: Int, month: Int, day: Int ->
                val date = day.toString() + "/" + (month + 1) + "/" + year
                (view as TextView).text = date
            }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun validateInputs(view: View): Boolean {
        return (validateNotEmpty(view, R.id.edtEmail, "Email cannot be empty")
                && validateEmail(view, R.id.edtEmail)
                && validateNotEmpty(view, R.id.edtFullName, "Full name cannot be empty")
                && validateNotEmpty(view, R.id.txtRole, "Role cannot be empty")
                && validateNotEmpty(view, R.id.edtDob, "Date of birth cannot be empty")
                && validateNotEmpty(view, R.id.edtAddress, "Address cannot be empty")
                && validateNotEmpty(
            view,
            R.id.edtSpecialization,
            "Specialization cannot be empty"
        ))

    }


    private fun validateNotEmpty(view: View, viewId: Int, errorMessage: String): Boolean {
        val editText = view.findViewById<EditText>(viewId)
        if ((editText.parent as LinearLayout).visibility == View.VISIBLE && editText.text.toString()
                .trim { it <= ' ' }
                .isEmpty()
        ) {
            Utils.showToast(this, errorMessage)
            return false
        }
        return true
    }

    private fun validateEmail(view: View, viewId: Int): Boolean {
        val editText = view.findViewById<EditText>(viewId)
        if (editText != null && !Validator.isValidEmail(editText.text.toString())) {
            Utils.showToast(this, "Invalid email")
            return false
        }
        return true
    }
}