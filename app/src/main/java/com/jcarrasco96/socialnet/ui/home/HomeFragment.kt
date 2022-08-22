package com.jcarrasco96.socialnet.ui.home

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jcarrasco96.socialnet.adapters.PostAdapter
import com.jcarrasco96.socialnet.databinding.DialogAddPostBinding
import com.jcarrasco96.socialnet.databinding.FragmentHomeBinding
import com.jcarrasco96.socialnet.interfaces.IAdapterIterface
import com.jcarrasco96.socialnet.interfaces.IFragmentInterface
import com.jcarrasco96.socialnet.models.ApiError
import com.jcarrasco96.socialnet.models.Post
import com.jcarrasco96.socialnet.models.Posts
import com.jcarrasco96.socialnet.services.API
import com.jcarrasco96.socialnet.ui.post.PostFragment
import com.jcarrasco96.socialnet.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment(var iFragmentInterface: IFragmentInterface) : Fragment(), IAdapterIterface {

    private lateinit var binding: FragmentHomeBinding
    lateinit var adapter: PostAdapter
    var currentPage = 1
    var totalAvailablePages = 1
    lateinit var posts: ArrayList<Post>

    private var isFabHide: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.text.observe(viewLifecycleOwner) {
//            binding..text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerview.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.VERTICAL, false
        )

        init()

        binding.fabCreatePost.setOnClickListener {
            showAddPostDialog()
        }
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!binding.recyclerview.canScrollVertically(1)) {
                    if (currentPage < totalAvailablePages) {
                        currentPage++
                        loadPageList()
                    }
                }
            }
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.recyclerview.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY < oldScrollY) {
                    animateFab(false)
                }
                if (scrollY > oldScrollY) {
                    animateFab(true)
                }
            }
        }
        binding.swipRefresh.setOnRefreshListener {
            init()
        }
    }

    private fun init() {
        currentPage = 1
        totalAvailablePages = 1
        posts = ArrayList()
        adapter = PostAdapter(this)
        binding.recyclerview.adapter = adapter
        loadPageList()
    }

    private fun loadPageList() {
        binding.swipRefresh.isRefreshing = true

        API.postsIndex(currentPage, object : Callback<Posts> {
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
                binding.swipRefresh.isRefreshing = false
            }

            override fun onFailure(call: Call<Posts>, t: Throwable) {
                Log.e("ERROR", "API.postsService().posts", t)
                binding.swipRefresh.isRefreshing = false
            }
        })
    }

    private fun animateFab(hide: Boolean) {
        if (isFabHide && hide || !isFabHide && !hide) return
        isFabHide = hide
        val moveY = if (hide) 2 * binding.fabCreatePost.height else 0
        binding.fabCreatePost.animate().translationY(moveY.toFloat()).setStartDelay(100)
            .setDuration(500)
            .start()
    }

    private fun showAddPostDialog(postUsername: String? = null, postContent: String? = null) {
        val dialogBinding = DialogAddPostBinding.inflate(layoutInflater)
        val dialog = Utils.showDialog(requireContext(), dialogBinding.root)

        if (!postUsername.isNullOrEmpty() && !postContent.isNullOrEmpty()) {
            dialogBinding.etPost.setText("Reply from ${postUsername}\n\n${postContent}")
        }

        dialogBinding.btCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btSubmit.setOnClickListener {
            val post = dialogBinding.etPost.text.toString()
                .trim()

            if (post.isEmpty()) {
                Utils.showSnack(it, layoutInflater, "Campos vacios")
            } else {
                addPost(post, dialogBinding, dialog)
            }
        }

        dialog.show()
    }

    private fun addPost(postContent: String, bindingDialog: DialogAddPostBinding, dialog: Dialog) {
        bindingDialog.progressDialog.visibility = View.VISIBLE

        API.postsCreate(postContent, object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    val postResult = response.body()
                    if (postResult != null) {
                        adapter.add(postResult)
                        binding.recyclerview.scrollToPosition(0)
                    }
                    Utils.showSnack(requireActivity(), "Publicado")
                    dialog.dismiss()
                } else {
                    val apiError = ApiError.fromResponseBody(response.errorBody())
                    if (apiError != null) {
                        Log.e("APIERROR", apiError.message)
                        Utils.showSnack(requireActivity(), apiError.message)
                    }
                }
                bindingDialog.progressDialog.visibility = View.GONE
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.e("ERROR", "API.postsService().create", t)
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                bindingDialog.progressDialog.visibility = View.GONE
            }
        })
    }

    override fun buttonPressedCall(post: Post) {
        startActivity(Intent("android.intent.action.DIAL", Uri.parse("tel:" + post.phone)))
    }

    override fun buttonPressedDelete(post: Post) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Eliminar publicación")
        dialog.setMessage("¿Está seguro que desea eliminar la publicación?")
        dialog.setPositiveButton("Eliminar") { _: DialogInterface, _: Int ->
            API.postsDelete(post.id, object : Callback<ApiError> {
                override fun onResponse(call: Call<ApiError>, response: Response<ApiError>) {
                    if (response.isSuccessful) {
                        val showModel = response.body()
                        if (showModel != null) {
                            Utils.showSnack(requireActivity(), showModel.message)

                            val position = adapter.posts.indexOf(post)
                            adapter.posts.removeAt(position)
                            adapter.notifyItemRemoved(position)
                        }
                    } else {
                        val apiError = ApiError.fromResponseBody(response.errorBody())
                        if (apiError != null) {
                            Log.e("APIERROR", apiError.message)
                            Utils.showSnack(requireActivity(), apiError.message)
                        }
                    }
                }

                override fun onFailure(call: Call<ApiError>, t: Throwable) {
                    Log.e("ERROR", "API.postsService().deletePost", t)
                }
            })
        }
        dialog.setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        dialog.show()
    }

    override fun buttonPressedMail(post: Post) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(post.email))
        intent.putExtra(Intent.EXTRA_SUBJECT, post.content)
        startActivity(intent)
    }

    override fun buttonPressedReport(post: Post, view: View) {
        lifecycleScope.launch {
            view.isVisible = true
            delay(1000)
            Utils.showSnack(requireActivity(), "Reportado a los administradores!")
            view.isVisible = false
        }
    }

    override fun buttonPressedReply(post: Post) {
        showAddPostDialog(post.name, post.content)
    }

    override fun onCLick(post: Post) {
        iFragmentInterface.showFragment(PostFragment(iFragmentInterface, post), "Detalles del post: ${post.id}", false)
    }

}