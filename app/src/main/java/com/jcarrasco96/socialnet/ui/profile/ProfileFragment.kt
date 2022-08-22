package com.jcarrasco96.socialnet.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jcarrasco96.socialnet.adapters.PostAdapter
import com.jcarrasco96.socialnet.databinding.FragmentProfileBinding
import com.jcarrasco96.socialnet.interfaces.IAdapterIterface
import com.jcarrasco96.socialnet.interfaces.IFragmentInterface
import com.jcarrasco96.socialnet.models.ApiError
import com.jcarrasco96.socialnet.models.Post
import com.jcarrasco96.socialnet.models.Posts
import com.jcarrasco96.socialnet.models.User
import com.jcarrasco96.socialnet.services.API
import com.jcarrasco96.socialnet.ui.home.HomeFragment
import com.jcarrasco96.socialnet.ui.post.PostFragment
import com.jcarrasco96.socialnet.utils.Utils
import com.jcarrasco96.socialnet.utils.loadImageProfile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment(var iFragmentInterface: IFragmentInterface) : Fragment(), IAdapterIterface {

    lateinit var binding: FragmentProfileBinding
    lateinit var adapter: PostAdapter
    var currentPage = 1
    var totalAvailablePages = 0
    lateinit var posts: ArrayList<Post>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        init()

        binding.recyclerview.isNestedScrollingEnabled = false

        binding.nestedContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, _, _, _ ->
            if (!v.canScrollVertically(1)) {
                if (currentPage < totalAvailablePages) {
                    currentPage++
                    loadPageList()
                }
            }
        })
        binding.imgCircularProfile.setOnClickListener {
            iFragmentInterface.showFragment(HomeFragment(iFragmentInterface), "Inicio 2", true)
        }
    }

    private fun init() {
        currentPage = 1
        totalAvailablePages = 1
        posts = ArrayList()
        adapter = PostAdapter(this)
        binding.recyclerview.adapter = adapter
        loadProfile()
        loadPageList()
    }

    private fun loadProfile() {
        binding.progress.isVisible = true

        API.userCurrent(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        binding.txtUsername.text = userResponse.name()
                        binding.txtPosts.text = "${userResponse.posts}"
                        binding.txtComments.text = "${userResponse.comments}"
                        binding.imgCircularProfile.loadImageProfile(userResponse.avatar500())
                    }
                } else {
                    val apiError = ApiError.fromResponseBody(response.errorBody())
                    if (apiError != null) {
                        Log.e("APIERROR", apiError.message)
                        Utils.showSnack(requireActivity(), apiError.message)
                    }
                }
                binding.progress.isVisible = false
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("ERROR", "API.usersService().current", t)
                Utils.showSnack(requireActivity(), t.message.toString())
                binding.progress.isVisible = false
            }
        })
    }

    private fun loadPageList() {
        binding.progress.isVisible = true

        API.postsOwner(currentPage, object : Callback<Posts> {
            override fun onResponse(call: Call<Posts>, response: Response<Posts>) {
                if (response.isSuccessful) {
                    val showModel = response.body()
                    if (showModel?.posts != null) {
                        val oldCount = posts.size
                        totalAvailablePages = showModel.pages
                        posts.addAll(showModel.posts)
                        adapter.updateList(posts, oldCount, posts.size)
                    }
                } else {
                    val apiError = ApiError.fromResponseBody(response.errorBody())
                    if (apiError != null) {
                        Log.e("APIERROR", apiError.message)
                        Utils.showSnack(requireActivity(), apiError.message)
                    }
                }
                binding.progress.isVisible = false
            }

            override fun onFailure(call: Call<Posts>, t: Throwable) {
                Log.e("ERROR", "exception", t)
                binding.progress.isVisible = false
            }
        })
    }

    override fun buttonPressedCall(post: Post) {

    }

    override fun buttonPressedDelete(post: Post) {

    }

    override fun buttonPressedMail(post: Post) {

    }

    override fun buttonPressedReply(post: Post) {

    }

    override fun buttonPressedReport(post: Post, view: View) {

    }

    override fun onCLick(post: Post) {
        iFragmentInterface.showFragment(
            PostFragment(iFragmentInterface, post),
            "Detalles del post: ${post.id}",
            false
        )
    }


}