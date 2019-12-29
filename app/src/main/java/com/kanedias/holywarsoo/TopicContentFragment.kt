package com.kanedias.holywarsoo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.kanedias.holywarsoo.dto.ForumTopic
import com.kanedias.holywarsoo.model.TopicContentsModel
import com.kanedias.holywarsoo.service.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import java.lang.Exception

/**
 * Fragment for showing contents of the topic, i.e. list of messages it contains.
 * Also shows post button if the topic itself is writable.
 *
 * @author Kanedias
 *
 * Created on 22.12.19
 */
class TopicContentFragment: ContentFragment() {

    companion object {
        const val TOPIC_ARG = "TOPIC_ARG"
        const val URL_ARG = "URL_ARG"
    }

    @BindView(R.id.message_list_scroll_area)
    lateinit var topicViewRefresher: SwipeRefreshLayout

    @BindView(R.id.message_list)
    lateinit var topicView: RecyclerView

    private lateinit var contents: TopicContentsModel

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_topic_contents, parent, false)
        ButterKnife.bind(this, view)

        topicView.layoutManager = LinearLayoutManager(context)

        topicViewRefresher.setOnRefreshListener { refreshContent() }

        contents = ViewModelProviders.of(this).get(TopicContentsModel::class.java)
        contents.topic.observe(this, Observer { topicView.adapter = TopicContentsAdapter(it) })
        contents.topic.observe(this, Observer { refreshViews() })


        refreshContent()

        return view
    }

    override fun refreshViews() {
        (activity as? MainActivity)?.toolbar?.apply {
            title = contents.topic.value?.name
            subtitle = "${contents.topic.value?.currentPage}"
        }
    }

    private fun refreshContent() {
        lifecycleScope.launchWhenResumed {
            topicViewRefresher.isRefreshing = true

            try {
                val topic = requireArguments().getSerializable(TOPIC_ARG) as ForumTopic
                val customUrl = HttpUrl.parse(requireArguments().getString(URL_ARG, ""))
                val loaded = withContext(Dispatchers.IO) { Network.loadTopicContents(topic, link = customUrl) }
                contents.topic.value = loaded
            } catch (ex: Exception) {
                context?.let { Network.reportErrors(it, ex) }
            }

            topicViewRefresher.isRefreshing = false
        }
    }

    class TopicContentsAdapter(topic: ForumTopic) : RecyclerView.Adapter<MessageViewHolder>() {

        val messages = topic.messages

        override fun getItemCount() = messages.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.fragment_topic_message_item, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            holder.setup(message)
        }

    }
}