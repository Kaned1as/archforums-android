package com.kanedias.holywarsoo

import android.os.Bundle
import android.util.Log
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
import com.kanedias.holywarsoo.dto.SearchTopicResults
import com.kanedias.holywarsoo.model.SearchContentsModel
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
 * Fragment representing topic search content.
 * Shows a list of topics this search results contain.
 * Can be navigated with paging controls.
 *
 * @author Kanedias
 *
 * Created on 19.12.19
 */
class SearchTopicContentFragment: ContentFragment() {

    companion object {
        const val SEARCH_ARG = "SEARCH_ARG"
    }

    @BindView(R.id.topic_list_scroll_area)
    lateinit var searchViewRefresher: SwipeRefreshLayout

    @BindView(R.id.topic_list_bottom_navigation)
    lateinit var pageNavigation: ViewGroup

    @BindView(R.id.topic_list)
    lateinit var searchView: RecyclerView

    lateinit var contents: SearchContentsModel

    lateinit var pageControls: PageViews

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_forum_contents, parent, false)
        ButterKnife.bind(this, view)

        searchView.layoutManager = LinearLayoutManager(context)

        searchViewRefresher.setOnRefreshListener { refreshContent() }

        contents = ViewModelProviders.of(this).get(SearchContentsModel::class.java)
        contents.results.value = requireArguments().getSerializable(SEARCH_ARG) as SearchTopicResults
        contents.results.observe(this, Observer { searchView.adapter = SearchPageContentsAdapter(it) })
        contents.results.observe(this, Observer { refreshViews() })

        pageControls = PageViews(this, contents, pageNavigation)
        refreshContent()

        return view
    }

    override fun refreshContent() {
        Log.d("SearchFrag", "Refreshing content, search ${contents.results.value?.name}, page ${contents.currentPage.value}")

        lifecycleScope.launchWhenResumed {
            searchViewRefresher.isRefreshing = true

            try {
                val loaded = withContext(Dispatchers.IO) {
                    Network.loadSearchResults(contents.results.value!!)
                }
                contents.results.value = loaded
                contents.pageCount.value = loaded.pageCount
                contents.currentPage.value = loaded.currentPage
            } catch (ex: Exception) {
                context?.let { Network.reportErrors(it, ex) }
            }

            searchViewRefresher.isRefreshing = false
        }
    }

    override fun refreshViews() {
        val searchResults = contents.results.value ?: return
        val activity = activity as? MainActivity ?: return

        activity.addButton.visibility = View.GONE

        activity.toolbar.apply {
            title = searchResults.name
            subtitle = "${getString(R.string.page)} ${searchResults.currentPage}"
        }

        when (searchResults.pageCount) {
            1 -> pageNavigation.visibility = View.GONE
            else -> pageNavigation.visibility = View.VISIBLE
        }
    }

    class SearchPageContentsAdapter(results: SearchTopicResults) : RecyclerView.Adapter<TopicViewHolder>() {

        val topics = results.topics

        override fun getItemCount() = topics.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.fragment_topic_list_item, parent, false)
            return TopicViewHolder(view)
        }

        override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
            val topic = topics[position]
            holder.setup(topic)
        }

    }
}