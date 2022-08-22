package com.jcarrasco96.socialnet.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.jcarrasco96.socialnet.databinding.FragmentPostBinding
import com.jcarrasco96.socialnet.interfaces.IFragmentInterface
import com.jcarrasco96.socialnet.models.Post
import com.jcarrasco96.socialnet.utils.Time
import com.jcarrasco96.socialnet.utils.loadImageProfile

class PostFragment(var iFragmentInterface: IFragmentInterface, var post: Post) : Fragment() {

    private lateinit var binding: FragmentPostBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgCircularProfile.loadImageProfile(post.avatar)
        binding.itemName.text = post.name
        binding.itemImageStar.isVisible = post.isVerified()
        binding.itemDate.text = Time.timeAgo(post.date())
        binding.itemContent.text = post.content
        binding.btnComentPost.isVisible = false

    }

}