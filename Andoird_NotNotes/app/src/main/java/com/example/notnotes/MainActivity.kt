package com.example.notnotes

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notnotes.database.FirebaseConnection
import com.example.notnotes.userservice.ChangePasswordActivity
import com.example.notnotes.userservice.LoginActivity
import com.example.notnotes.userservice.ProfileActivity
import com.example.notnotes.databinding.ActivityMainBinding
import com.example.notnotes.listener.FirebaseListener
import com.example.notnotes.listener.FragmentListener
import com.example.notnotes.listener.ItemClickListener
import com.example.notnotes.model.Note
import com.example.notnotes.model.User
import com.example.notnotes.noteservice.MyNoteAdapter
import com.example.notnotes.noteservice.NoteDetailFragment

class MainActivity :
    AppCompatActivity(),
    FragmentListener,
    FirebaseListener,
    ItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: MyNoteAdapter
    private lateinit var noteList: ArrayList<Note>
    private lateinit var database: FirebaseConnection
    private lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = getUserSession()
        database = FirebaseConnection(this, this)
        initViews()

        binding.fab.setOnClickListener {
            openNoteDetailFragment(null)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            database.getNotes(user.userName)
        }

    }

    private fun openNoteDetailFragment(bundle: Bundle?) {
        hideComponents()

        val fragment = NoteDetailFragment()
        fragment.arguments = bundle
        fragment.setFragmentListener(this)

        val fragmentManager: FragmentManager = supportFragmentManager

        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragmentContainer, fragment)

        fragmentTransaction.addToBackStack("NoteDetailFragment")

        fragmentTransaction.commit()
    }



    private fun hideComponents () {
        binding.tvUsername.visibility = View.GONE
        binding.recyclerview.visibility = View.GONE
        binding.fab.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
    }
     private fun showComponents () {
        binding.tvUsername.visibility = View.VISIBLE
        binding.recyclerview.visibility = View.VISIBLE
        binding.fab.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
    }

    private fun initViews() {
        initHelloTextView()
        initRecyclerView()
    }

    private fun initHelloTextView() {
        val userName = user.fullName
        val formattedString = getString(R.string.formatted_string, userName)

        val spannableString = SpannableString(formattedString)

        val start = formattedString.indexOf(userName)
        val end = start + userName.length
        val styleSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(styleSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val colorSpan = ForegroundColorSpan(resources.getColor(R.color.pink))
        spannableString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvUsername.text = spannableString
    }

    private fun initRecyclerView() {
        noteList = ArrayList()
        noteAdapter = MyNoteAdapter(this, noteList, this)
        binding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(
                this.context,
                LinearLayoutManager.VERTICAL,
                false
            )

            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    RecyclerView.VERTICAL
                )
            )
            adapter = noteAdapter
        }
        database.getNotes(user.userName)
    }

    private fun getUserSession(): User {
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val fullName = sharedPreferences.getString("fullName", "")
        val userName = sharedPreferences.getString("userName", "")
        val password = ""
        val email = sharedPreferences.getString("email", null)
        val phoneNumber = sharedPreferences.getString("phoneNumber", null)
        val address = sharedPreferences.getString("address", null)
        val job  = sharedPreferences.getString("job", null)
        val homepage = sharedPreferences.getString("homepage", null)
        return User(fullName!!, userName!!, password, email, phoneNumber, address, job, homepage)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_profile -> openProfileActivity()
            R.id.menu_item_logout -> logoutAccount()
            R.id.menu_item_change_password -> openChangePasswordActivity()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun logoutAccount() {
        deleteSharedPreferences("MyPreferences")
        openLoginActivity()
    }

    override fun deleteSharedPreferences(prefName: String): Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences(prefName, MODE_PRIVATE)

        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        return true

    }

    private fun openChangePasswordActivity() {
        val changePasswordIntent = Intent(this, ChangePasswordActivity::class.java)
        startActivity(changePasswordIntent)
    }

    private fun openLoginActivity() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    private fun openProfileActivity() {
        val profileIntent = Intent(this, ProfileActivity::class.java)
        startActivity(profileIntent)
    }

    // -- Fragment Listener --

    override fun onFragmentClosed() {
        showComponents()
    }


    // -- Firebase Listener --
    override fun onUsernameExist(user: User) {

    }

    override fun onStartAccess() {

    }

    override fun onUserNotExist() {

    }

    override fun onFailure() {

    }

    override fun onReadNoteListComplete(notes: List<Note>) {
        noteList.clear()
        noteList.addAll(notes)
        noteAdapter.notifyDataSetChanged()
    }


    // -- Item Click Listener --
    override fun onItemClick(note: Note) {
        val bundle = Bundle()
        bundle.putParcelable(NOTE_KEY, note)
        openNoteDetailFragment(bundle)
    }

    override fun onDeleteItemClick(note: Note) {
        val title = "Xóa note"
        val message = "Bạn có chắc rằng mình muốn xóa note"
        showDialogConfirm(title, message, note)
    }

    private fun showDialogConfirm(title: String, message: String, note: Note) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Xóa") { dialog, _ ->
                database.deleteNote(note, user.userName)
                database.getNotes(user.userName)
            }
            .setNegativeButton("Hủy bỏ") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    companion object {
        val NOTE_KEY = "note_key"
    }

}