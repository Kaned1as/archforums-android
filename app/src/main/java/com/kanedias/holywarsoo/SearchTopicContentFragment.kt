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
import com.kanedias.holywarsoo.dto.SearchTopicResults
import com.kanedias.holywarsoo.model.SearchContentsModel
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
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

    @BindView(R.id.topic_list)
    lateinit var searchView: RecyclerView

    private lateinit var contents: SearchContentsModel

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_forum_contents, parent, false)
        ButterKnife.bind(this, view)

        searchView.layoutManager = LinearLayoutManager(context)

        searchViewRefresher.setOnRefreshListener { refreshContent() }

        contents = ViewModelProviders.of(this).get(SearchContentsModel::class.java)
        contents.page.observe(this, Observer { searchView.adapter = SearchPageContentsAdapter(it) })
        contents.page.observe(this, Observer { refreshViews() })

        refreshContent()

        return view
    }

    private fun refreshContent() {
        lifecycleScope.launchWhenResumed {
            searchViewRefresher.isRefreshing = true

            try {
                val page = requireArguments().getSerializable(SEARCH_ARG) as SearchTopicResults
                val loaded = withContext(Dispatchers.IO) { Network.loadSearchResults(page) }
                contents.page.value = loaded
            } catch (ex: Exception) {
                context?.let { Network.reportErrors(it, ex) }
            }

            searchViewRefresher.isRefreshing = false
        }
    }

    override fun refreshViews() {
        (activity as? MainActivity)?.toolbar?.apply {
            title = contents.page.value?.name
            subtitle = "${contents.page.value?.currentPage}"
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