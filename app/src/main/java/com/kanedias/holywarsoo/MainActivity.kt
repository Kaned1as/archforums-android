package com.kanedias.holywarsoo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.kanedias.holywarsoo.dto.SearchTopicResults
import com.kanedias.holywarsoo.markdown.mdRendererFrom
import com.kanedias.holywarsoo.misc.showFullscreenFragment
import com.kanedias.holywarsoo.model.MainPageModel
import com.kanedias.holywarsoo.service.Config
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.IllegalStateException

/**
 * Main activity of the application. Has toolbar and navigation drawer to allow login and search shortcuts.
 * All fragment transactions happen here.
 */
class MainActivity : AppCompatActivity() {

    @BindView(R.id.main_area)
    lateinit var drawer: DrawerLayout

    @BindView(R.id.main_sidebar)
    lateinit var sidebar: NavigationView

    @BindView(R.id.main_toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.main_reply_button)
    lateinit var addButton: FloatingActionButton

    lateinit var sidebarHeader: SidebarHeaderViewHolder

    private lateinit var mainPageModel: MainPageModel

    private lateinit var donateHelper: DonateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        // setup action bar
        setSupportActionBar(toolbar)

        // setup donate helper
        donateHelper = DonateHelper(this)

        // setup sidebar
        sidebar.menu.forEach { it.isEnabled = false }
        sidebar.setNavigationItemSelectedListener { item -> onSidebarItemSelected(item) }
        sidebarHeader = SidebarHeaderViewHolder(sidebar.getHeaderView(0))

        // setup drawer and menu button
        val drawerToggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close)
        drawer.addDrawerListener(drawerToggle)
        drawer.addDrawerListener(object: DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                val resources = listOf(
                    R.drawable.guy_fawkes_mask,
                    R.drawable.incognito,
                    R.drawable.bomb,
                    R.drawable.nuke)
                sidebarHeader.randomImage.setImageResource(resources.random())
            }
        })
        drawerToggle.syncState()

        mainPageModel = ViewModelProviders.of(this).get(MainPageModel::class.java)
        mainPageModel.account.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                sidebar.menu.forEach { item -> item.isEnabled = false }
                sidebarHeader.username.setText(R.string.guest)
                sidebarHeader.loginButton.setImageResource(R.drawable.login)
                sidebarHeader.loginButton.setOnClickListener {
                    drawer.closeDrawers()
                    showFullscreenFragment(LoginFragment())
                }
            } else {
                sidebar.menu.forEach { item -> item.isEnabled = true }
                sidebarHeader.username.text = it
                sidebarHeader.loginButton.setImageResource(R.drawable.exit)
                sidebarHeader.loginButton.setOnClickListener {
                    drawer.closeDrawers()
                    Network.logout()
                    refreshContent()
                }
            }
        })

        // hack: resume fragment that is activated on tapping "back"
        supportFragmentManager.addOnBackStackChangedListener(object: FragmentManager.OnBackStackChangedListener {
            var lastStackSize = 0

            override fun onBackStackChanged() {
                val currentStackSize = supportFragmentManager.backStackEntryCount
                if (currentStackSize < lastStackSize) { // we navigated back
                    val top = supportFragmentManager.fragments.findLast { it is ContentFragment }
                    (top as? ContentFragment)?.refreshViews()
                }

                lastStackSize = currentStackSize
            }
        })

        refreshContent()
    }

    override fun onStart() {
        super.onStart()

        checkWhatsNew()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // setupTopSearch(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_donate -> donateHelper.donate()
            //R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_about -> showFullscreenFragment(AboutFragment())
            else -> return super.onOptionsItemSelected(item)
        }

        // it was handled in `when` block or we wouldn't be at this point
        // confirm it
        return true
    }

    private fun setupTopSearch(menu: Menu) {
        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView
    }

    private fun checkWhatsNew() {
        data class Release(val versionName: String, val textId: Int)

        val releases = mapOf(
            5 to Release("1.1.3", R.string.release_5),
            6 to Release("1.1.4", R.string.release_6),
            7 to Release("1.1.5", R.string.release_7)
        )

        val currVersion = BuildConfig.VERSION_CODE
        if (Config.lastVersion < currVersion) {
            val whatsNew = StringBuilder(150)
            for(missedRelease in currVersion downTo Config.lastVersion + 1) {
                if (!releases.containsKey(missedRelease)) {
                    // no info on that release, probably internal bugfix or refactoring
                    // (or current version is very old)
                    continue
                }

                val release = releases.getValue(missedRelease)
                whatsNew.append("${release.versionName}\n")
                whatsNew.append("----------------------\n")

                val parts = getString(release.textId).split("\n").map(String::trim)
                parts.forEach {
                    whatsNew.append("- $it\n")
                }

                whatsNew.append("\n\n")
            }

            if (whatsNew.isEmpty()) {
                // no info of new releases, skip showing dialog
                return
            }

            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.whats_new)
                .setMessage(mdRendererFrom(this).toMarkdown(whatsNew.toString()))
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.help_the_project) { _, _ -> donateHelper.donate() }
                .show()

            Config.lastVersion = currVersion
            return
        }
    }

    private fun onSidebarItemSelected(item: MenuItem): Boolean {
        val page = when (item.itemId) {
            R.id.menu_item_favorites -> {
                val name = getString(R.string.favorite_topics)
                SearchTopicResults(name = name, link = Network.FAVORITE_TOPICS_URL)
            }
            R.id.menu_item_replies -> {
                val name = getString(R.string.replies_topics)
                SearchTopicResults(name = name, link = Network.REPLIES_TOPICS_URL)
            }
            R.id.menu_item_new_messages -> {
                val name = getString(R.string.new_messages_topics)
                SearchTopicResults(name = name, link = Network.NEW_MESSAGES_TOPICS_URL)
            }
            R.id.menu_item_recent -> {
                val name = getString(R.string.recent_topics)
                SearchTopicResults(name = name, link = Network.RECENT_TOPICS_URL)
            }
            else -> throw IllegalStateException("No such page!")
        }
        drawer.closeDrawers()

        val frag = SearchTopicContentFragment().apply {
            arguments = Bundle().apply { putSerializable(SearchTopicContentFragment.SEARCH_ARG, page) }
        }
        showFullscreenFragment(frag)

        return true
    }

    private fun refreshContent() {
        if (Network.isLoggedIn()) {
            mainPageModel.account.value = Network.getUsername()

            // re-login in background if needed
            if (Network.daysToAuthExpiration() < 3) {
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) { Network.refreshLogin() }
                    } catch (ex: Exception) {
                        Network.reportErrors(this@MainActivity, ex)
                    }
                }
            }
        } else {
            // not logged in, show guest name
            mainPageModel.account.value = null
        }
    }

    class SidebarHeaderViewHolder(private val iv: View) {
        @BindView(R.id.sidebar_header_random_image)
        lateinit var randomImage: ImageView

        @BindView(R.id.sidebar_header_current_user_name)
        lateinit var username: TextView

        @BindView(R.id.sidebar_header_login)
        lateinit var loginButton: ImageView

        init {
            ButterKnife.bind(this, iv)
        }
    }

}
