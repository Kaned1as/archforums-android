package com.kanedias.holywarsoo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.ForumTopicDesc
import com.kanedias.holywarsoo.dto.SearchResults
import com.kanedias.holywarsoo.model.SearchTopicsContentsModel
import com.kanedias.holywarsoo.service.Network

/**
 * Fragment representing topic search content.
 * Shows a list of topics this search results contain.
 * Can be navigated with paging controls.
 *
 * @author Kanedias
 *
 * Created on 2019-12-19
 */
class SearchTopicsContentFragment: FullscreenContentFragment() {

    companion object {
        const val URL_ARG = "URL_ARG"
    }

    lateinit var contents: SearchTopicsContentsModel

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_contents, parent, false)
        ButterKnife.bind(this, view)

        contents = ViewModelProviders.of(this).get(SearchTopicsContentsModel::class.java)
        contents.search.observe(this, Observer { contentView.adapter = SearchPageContentsAdapter(it) })
        contents.search.observe(this, Observer { refreshViews() })

        setupUI(contents)
        refreshContent()

        return view
    }

    override fun refreshContent() {
        Log.d("SearchFrag", "Refreshing content, search ${contents.search.value?.name}, page ${contents.currentPage.value}")

        lifecycleScope.launchWhenResumed {
            viewRefresher.isRefreshing = true

            val url = requireArguments().getString(URL_ARG, "")

            Network.perform(
                networkAction = { Network.loadSearchTopicResults(url, page = contents.currentPage.value!!) },
                uiAction = { loaded ->
                    contents.search.value = loaded
                    contents.pageCount.value = loaded.pageCount
                    contents.currentPage.value = loaded.currentPage
                }
            )

            viewRefresher.isRefreshing = false
        }
    }

    override fun refreshViews() {
        super.refreshViews()

        val searchResults = contents.search.value ?: return

        toolbar.apply {
            title = searchResults.name
            subtitle = "${getString(R.string.page)} ${searchResults.currentPage}"
        }

        when (searchResults.pageCount) {
            1 -> pageNavigation.visibility = View.GONE
            else -> pageNavigation.visibility = View.VISIBLE
        }
    }

    class SearchPageContentsAdapter(results: SearchResults<ForumTopicDesc>) : RecyclerView.Adapter<TopicViewHolder>() {

        val topics = results.results

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