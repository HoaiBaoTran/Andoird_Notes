package com.example.notnotes.userservice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.notnotes.R
import com.example.notnotes.databinding.ActivityProfileBinding
import com.example.notnotes.model.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var user: User

    private val NO_INFORMATION = getString(R.string.no_information)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        user = getUserSession()
        loadUserInfo()

        binding.btnImage.setOnClickListener {
            openEditProfileActivity()
        }
    }
    private fun getUserSession(): User {
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val fullName = sharedPreferences.getString("fullName", "")
        val userName = sharedPreferences.getString("userName", "")
        val password = ""
        val email = sharedPreferences.getString("email", "")
        val phoneNumber = sharedPreferences.getString("phoneNumber", null)
        val address = sharedPreferences.getString("address", null)
        val job  = sharedPreferences.getString("job", null)
        val homepage = sharedPreferences.getString("homepage", null)
        return User(fullName!!, email!!, password, phoneNumber, address, job, homepage)
    }

    override fun onResume() {
        super.onResume()
        user = getUserSession()
        loadUserInfo()
    }

    private fun loadUserInfo() {
        binding.apply {
            tvNameInfo.text = user.fullName
            tvName.text = user.fullName
        }

        binding.tvEmailInfo.text = if (user.email == null) {
            NO_INFORMATION
        } else {
            user.email
        }


        binding.tvPhoneNumberInfo.text = if (user.phoneNumber == null) {
            NO_INFORMATION
        } else {
            user.phoneNumber
        }

        binding.tvAddressInfo.text = if(user.address == null) {
            NO_INFORMATION
        }
        else {
            user.address
        }

        binding.tvJobInfo.text = if (user.job == null) {
            NO_INFORMATION
        }
        else {
            user.job
        }

        binding.tvHomepageInfo.text = if (user.homepage == null) {
            NO_INFORMATION
        }
        else {
            user.homepage
        }
    }

    private fun openEditProfileActivity () {
        val editProfileIntent = Intent(this, EditProfileActivity::class.java)
        startActivity(editProfileIntent)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.profile_option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_item_back -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}