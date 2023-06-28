package com.example.socialnetwork.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.Adapter.PostAdapter
import com.example.socialnetwork.Modal.Post
import com.example.socialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Suppress("DEPRECATION")
class DashboardFragment : Fragment() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private var imageUri: Uri? = null
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: MutableList<Post>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        recyclerView = view.findViewById(R.id.postRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        postList = mutableListOf()
        postAdapter = PostAdapter(requireContext(), postList)
        recyclerView.adapter = postAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPosts()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add){
            showOptionsDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showOptionsDialog() {
        val options = arrayOf("Photo", "Gallery")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Post")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        val resolver = requireActivity().contentResolver
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, 2)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println(Activity.RESULT_OK)

        println("Galerija" +  REQUEST_IMAGE_CAPTURE)
        Log.d("gallery",Activity.RESULT_OK.toString())
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.d("gallery",Activity.RESULT_OK.toString())

            // Obrada slike sa kamere
            handleImage(imageUri)
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            // Obrada slike iz galerije
            val selectedImageUri: Uri? = data?.data
            Log.d("gallery",Activity.RESULT_OK.toString())

            handleImage(selectedImageUri)
        }
    }

    private fun handleImage(imageUri: Uri?) {
        val filename = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().reference.child("images/$filename")

        val uploadTask = storageRef.putFile(imageUri!!)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    saveImageToDatabase(imageUrl, userId)
                }
            }
        }
    }

    private fun saveImageToDatabase(imageUrl: String, userId: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("images")
        val newImageRef = databaseRef.push()

        val postId = newImageRef.key ?: "" // Get the unique ID generated by push()

        val post = Post(postId, imageUrl, userId, 0)
        newImageRef.setValue(post).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Successfully posted image", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed: $e", Toast.LENGTH_LONG).show()
        }
    }


    private fun loadPosts() {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("images")
        databaseRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()

                for (dataSnapshot in snapshot.children) {
                    val post: Post? = dataSnapshot.getValue(Post::class.java)
                    post?.let {
                        if(userId != it.userId)
                            postList.add(it)
                    }
                }

                if (postList.isEmpty()) {
                    // Prikazivanje poruke "Nema postova" kada lista postova nije popunjena
                    Toast.makeText(requireContext(), "Nema postova", Toast.LENGTH_SHORT).show()
                }

                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Obrada greške
            }
        })
    }
}
