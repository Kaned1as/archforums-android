package com.kanedias.archforums

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

/**
 * Abstract fragment containing list content
 *
 * @see ForumContentFragment
 * @see TopicContentFragment
 * @see SearchTopicsContentFragment
 *
 * @author Kanedias
 *
 * Created on 2019-12-29
 */
abstract class ContentFragment: Fragment() {

    abstract fun refreshViews()

    abstract fun refreshContent()

    class ErrorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount() = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.view_error_image, parent, false)
            return object: RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // not needed
        }

    }
}