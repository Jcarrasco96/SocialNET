package com.jcarrasco96.socialnet.adapters

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.jcarrasco96.socialnet.R
import com.jcarrasco96.socialnet.databinding.ItemPostBinding
import com.jcarrasco96.socialnet.interfaces.IAdapterIterface
import com.jcarrasco96.socialnet.models.Post
import com.jcarrasco96.socialnet.utils.Preferences
import com.jcarrasco96.socialnet.utils.Time
import com.jcarrasco96.socialnet.utils.inflate
import com.jcarrasco96.socialnet.utils.loadImageProfile

class PostAdapter(private val adapterIterface: IAdapterIterface) :
    RecyclerView.Adapter<PostAdapter.AdapterHolder>() {

    var posts = ArrayList<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterHolder {
        return AdapterHolder(parent.inflate(R.layout.item_post, false))
    }

    override fun onBindViewHolder(holder: AdapterHolder, position: Int) {
        val post = posts[position]

        holder.binding.itemContent.text = post.content
        holder.binding.itemDate.text = Time.timeAgo(post.date())
        holder.binding.itemName.text = post.name
        holder.binding.itemImageStar.isVisible = post.isVerified()
        holder.binding.btnDelete.isVisible = Preferences.userId() == post.user_id
        holder.binding.btnReport.isVisible = Preferences.userId() != post.user_id
        holder.binding.btnMailPost.isVisible =
            !(post.email.isEmpty() || Preferences.userId() == post.user_id)
        holder.binding.btnCallPost.isVisible =
            !(post.phone.isNullOrEmpty() || Preferences.userId() == post.user_id)
        holder.binding.imgCircularProfile.loadImageProfile(post.avatar)

        holder.binding.run {
            this.btnCallPost.setOnClickListener {
                adapterIterface.buttonPressedCall(post)
            }
            this.btnMailPost.setOnClickListener {
                adapterIterface.buttonPressedMail(post)
            }
            this.btnReplyPost.setOnClickListener {
                adapterIterface.buttonPressedReply(post)
            }
            this.btnReport.setOnClickListener {
                adapterIterface.buttonPressedReport(post, this.progressDialog)
            }
            this.btnDelete.setOnClickListener {
                adapterIterface.buttonPressedDelete(post)
            }
            this.layoutCard.setOnClickListener {
                adapterIterface.onCLick(post)
            }
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun updateList(posts: ArrayList<Post>, oldCount: Int, showListSize: Int) {
        this.posts = posts
        notifyItemRangeChanged(oldCount, showListSize)
    }

    fun add(post: Post) {
        this.posts.add(0, post)
        notifyItemInserted(0)
    }

    class AdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemPostBinding.bind(itemView)
    }

}