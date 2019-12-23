package com.kanedias.holywarsoo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.Forum
import com.kanedias.holywarsoo.model.MainPageModel
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    @BindView(R.id.main_area)
    lateinit var drawer: DrawerLayout

    @BindView(R.id.main_toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.main_forum_list)
    lateinit var forumList: RecyclerView

    private lateinit var mainPage: MainPageModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        forumList.layoutManager = LinearLayoutManager(this)

        setSupportActionBar(toolbar)
        val drawerToggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close)
        drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        mainPage = ViewModelProviders.of(this).get(MainPageModel::class.java)
        mainPage.forums.observe(this, Observer { forumList.adapter = ForumListAdapter(it) })

        lifecycleScope.launchWhenResumed {
            val loaded = withContext(Dispatchers.IO) { Network.loadForumList() }
            mainPage.forums.value = loaded
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

}
