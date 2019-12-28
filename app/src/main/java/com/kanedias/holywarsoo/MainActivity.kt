package com.kanedias.holywarsoo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.navigation.NavigationView
import com.kanedias.holywarsoo.dto.Forum
import com.kanedias.holywarsoo.dto.SearchPage
import com.kanedias.holywarsoo.misc.showFullscreenFragment
import com.kanedias.holywarsoo.model.MainPageModel
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    @BindView(R.id.main_area)
    lateinit var drawer: DrawerLayout

    @BindView(R.id.main_sidebar)
    lateinit var sidebar: NavigationView

    @BindView(R.id.main_toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.main_forum_list_scroll_area)
    lateinit var forumListRefresher: SwipeRefreshLayout

    @BindView(R.id.main_forum_list)
    lateinit var forumList: RecyclerView

    lateinit var sidebarHeader: SidebarHeaderViewHolder

    private lateinit var mainPageModel: MainPageModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        // setup action bar
        setSupportActionBar(toolbar)

        // setup forum view
        forumList.layoutManager = LinearLayoutManager(this)

        // setup sidebar
        sidebar.menu.forEach { it.isEnabled = false }
        sidebar.setNavigationItemSelectedListener { item -> onSidebarItemSelected(item) }
        sidebarHeader = SidebarHeaderViewHolder(sidebar.getHeaderView(0))

        // setup refresher
        forumListRefresher.setOnRefreshListener { refreshContent() }

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
        mainPageModel.forums.observe(this, Observer { forumList.adapter = ForumListAdapter(it) })
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

        refreshContent()
    }

    private fun onSidebarItemSelected(item: MenuItem): Boolean {
        val page = when (item.itemId) {
            R.id.menu_item_favorites -> {
                val name = getString(R.string.favorite_topics)
                SearchPage(name = name, link = Network.FAVORITE_TOPICS_URL)
            }
            R.id.menu_item_replies -> {
                val name = getString(R.string.replies_topics)
                SearchPage(name = name, link = Network.REPLIES_TOPICS_URL)
            }
            R.id.menu_item_new_messages -> {
                val name = getString(R.string.new_messages_topics)
                SearchPage(name = name, link = Network.NEW_MESSAGES_TOPICS_URL)
            }
            R.id.menu_item_recent -> {
                val name = getString(R.string.recent_topics)
                SearchPage(name = name, link = Network.RECENT_TOPICS_URL)
            }
            else -> throw IllegalStateException("No such page!")
        }
        drawer.closeDrawers()

        val frag = SearchPageFragment().apply {
            arguments = Bundle().apply { putSerializable(SearchPageFragment.SEARCH_ARG, page) }
        }
        showFullscreenFragment(frag)

        return true
    }

    private fun refreshContent() {
        if (Network.isLoggedIn()) {
            mainPageModel.account.value = Network.getUsername()
        } else {
            mainPageModel.account.value = null
        }

        lifecycleScope.launchWhenResumed {
            forumListRefresher.isRefreshing = true
            try {
                val loaded = withContext(Dispatchers.IO) { Network.loadForumList() }
                mainPageModel.forums.value = loaded
            } catch (ex: Exception) {
                Network.reportErrors(this@MainActivity, ex)
            }

            forumListRefresher.isRefreshing = false
        }
    }

    class ForumListAdapter(private val forumList: List<Forum>?) : RecyclerView.Adapter<ForumViewHolder>() {

        override fun getItemCount() = forumList?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val holder = inflater.inflate(R.layout.fragment_forum_list_item, parent, false)
            return ForumViewHolder(holder)
        }

        override fun onBindViewHolder(holder: ForumViewHolder, position: Int) {
            val forum = forumList!![position]
            holder.setup(forum)
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
