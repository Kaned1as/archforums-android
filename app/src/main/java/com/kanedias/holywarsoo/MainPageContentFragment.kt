package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.ForumDesc
import com.kanedias.holywarsoo.misc.resolveAttr
import com.kanedias.holywarsoo.model.MainPageModel
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
 * Fragment showing main page, i.e. list of forums with optional categories.
 *
 * @author Kanedias
 *
 * Created on 2019-12-19
 */
class MainPageContentFragment: ContentFragment() {

    @BindView(R.id.main_forum_list_scroll_area)
    lateinit var forumListRefresher: SwipeRefreshLayout

    @BindView(R.id.main_forum_list)
    lateinit var forumList: RecyclerView

    lateinit var contents: MainPageModel

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_main_contents, parent, false)
        ButterKnife.bind(this, view)

        forumList.layoutManager = LinearLayoutManager(context)

        forumListRefresher.setOnRefreshListener { refreshContent() }
        forumListRefresher.setColorSchemeColors(requireContext().resolveAttr(R.attr.colorSecondary))
        forumListRefresher.setProgressBackgroundColorSchemeColor(requireContext().resolveAttr(R.attr.colorPrimary))

        contents = ViewModelProviders.of(requireActivity()).get(MainPageModel::class.java)
        contents.forums.observe(this, Observer { forumList.adapter = ForumListAdapter(it) })
        contents.forums.observe(this, Observer { refreshViews() })

        refreshContent()

        return view
    }

    override fun refreshViews() {
        val activity = activity as? MainActivity ?: return

        activity.toolbar.apply {
            title = getString(R.string.app_name)
            subtitle = ""
        }
    }

    override fun refreshContent() {
        lifecycleScope.launchWhenResumed {
            forumListRefresher.isRefreshing = true
            try {
                val loaded = withContext(Dispatchers.IO) { Network.loadForumList() }
                contents.forums.value = loaded
            } catch (ex: Exception) {
                context?.let { Network.reportErrors(it, ex) }
            }

            forumListRefresher.isRefreshing = false
        }
    }

    /**
     * Adapter for presenting forum descriptions and links in a material card view.
     * Main page only contains top-level forums, which always are part of some category.
     */
    class ForumListAdapter(private val forumDescList: List<ForumDesc>) : RecyclerView.Adapter<ForumViewHolder>() {

        override fun getItemCount() = forumDescList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val holder = inflater.inflate(R.layout.fragment_forum_list_item, parent, false)
            return ForumViewHolder(holder)
        }

        override fun onBindViewHolder(holder: ForumViewHolder, position: Int) {
            val forum = forumDescList[position]
            holder.setup(forum)

            // show category if it's changed
            val categoryChanged = position > 0 && forum.category != forumDescList[position - 1].category
            if (forum.category != null && (position == 0 || categoryChanged)) {
                holder.forumCategoryArea.visibility = View.VISIBLE
            } else {
                holder.forumCategoryArea.visibility = View.GONE
            }
        }

    }
}