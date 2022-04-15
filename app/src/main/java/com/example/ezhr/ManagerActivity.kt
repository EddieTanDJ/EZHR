package com.example.ezhr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.example.ezhr.fragments.*
import com.example.ezhr.viewmodel.AccountViewModel
import com.example.ezhr.viewmodel.AccountViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class ManagerActivity : AppCompatActivity() {
    // Set all fragment
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private var username = ""

    private val viewModelAccount: AccountViewModel by viewModels {
        val app = application as EZHRApp
        AccountViewModelFactory(app.accountRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)
        drawerLayout = findViewById(R.id.manager_nav)
        val host: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment?
                ?: return
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationManager)
        setSupportActionBar(findViewById(R.id.topAppBar))
        // Setup drawerLayout
        val navController = host.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.attendanceManagerFragment,
                R.id.leaveManagerFragment,
                R.id.homeManagerFragment,
                R.id.claimsManagerFragment,
                R.id.profileFragment
            ), drawerLayout
        )


        // Setup the action bar
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up the bottom navigation
        bottomNavigationView.setupWithNavController(navController)
        // Set up navigation menu
        val sideNavView = findViewById<NavigationView>(R.id.nav_view_manager)
        sideNavView?.setupWithNavController(navController)
        getName()
    }

    /**
     * Logout function when user clicks on logout button on the options menu
     * @param view : View
     * @return : Boolean
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    /**
     * Logout function when user clicks on logout button on the options menu
     * @param item : MenuItem
     * @return Boolean
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profileFragment -> {
                item.onNavDestinationSelected(findNavController(R.id.fragment_container))
            }
            R.id.action_logout -> {
                // Logout from app in firebase
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Have NavigationUI handle up behavior in the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        // Allows NavigationUI to support proper up navigation or the drawer layout
        // drawer menu, depending on the situation
        return findNavController(R.id.fragment_container).navigateUp(appBarConfiguration)
    }

    /**
     * Get the name of the user from the database
     */
    private fun getName() {
        viewModelAccount.fetchUserData().observe(this) {
            if (it.exception != null) {
                Toast.makeText(
                    this,
                    "Error: ${it.exception.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }
            Log.d(TAG, "Data: ${it.user}")
            username = "${it.user!!.EmployeeName.toString()}"
            Log.d(TAG, "Username: $username")
            val navigationView = findViewById<NavigationView>(R.id.nav_view_manager)
            val headerView: View = navigationView.getHeaderView(0)
            val nameTextView: TextView = headerView.findViewById(R.id.nameTV)
            nameTextView.text = username
        }
    }}