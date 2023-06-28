@file:Suppress("DEPRECATION")

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialnetwork.Adapter.ImageAdapter
import com.example.socialnetwork.EditProfile
import com.example.socialnetwork.LoginActivity
import com.example.socialnetwork.Modal.Post
import com.example.socialnetwork.Modal.User
import com.example.socialnetwork.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class ProfileFragment : Fragment() {

    private lateinit var name: TextView
    private lateinit var image: ImageView
    private lateinit var storage: StorageReference
    private lateinit var reference: DatabaseReference
    private lateinit var editBtn: FloatingActionButton
    private var fuser: FirebaseUser? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var imageList: MutableList<String>
    val REQUEST_IMAGE_CAPTURE = 1
    var imageUri: Uri? = null
    companion object {
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        // Initialize image list and adapter

        image = view.findViewById(R.id.imgProfile)
        name = view.findViewById(R.id.nameProfile)
        editBtn = view.findViewById(R.id.editB)

        editBtn.setOnClickListener{
            startActivity(Intent(context, EditProfile::class.java))
        }

        storage = FirebaseStorage.getInstance().getReference("uploads")

        fuser = FirebaseAuth.getInstance().currentUser

        fun loadImagesForCurrentUser(userId: String) {
            val imagesRef = FirebaseDatabase.getInstance().getReference("images")
            imagesRef.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    imageList.clear()

                    for (dataSnapshot in snapshot.children) {
                        val post: Post? = dataSnapshot.getValue(Post::class.java)
                        post?.let {
                            val imageUrl = it.postUrl
                            imageList.add(imageUrl)
                        }
                    }

                    if (imageList.isEmpty()) {
                        // Prikazivanje poruke "Nema slika" kada lista slika nije popunjena
                        recyclerView.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                    }

                    imageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Obrada greške
                }
            })
        }


        reference = FirebaseDatabase.getInstance().getReference("myUsers").child(fuser?.uid!!)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java)

                user?.let {
                    name.text = it.username

                    if (it.imgUrl == "default") {
                        image.setImageResource(R.mipmap.ic_launcher)
                    } else {
                        Picasso.get().load(it.imgUrl).into(image)
                    }

                    // Check if the currently logged-in user is the profile owner
                    if (fuser?.uid == it.id) {
                        // Load images only for the currently logged-in user
                        loadImagesForCurrentUser(it.id)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

        imageList = mutableListOf()
        imageAdapter = ImageAdapter(requireContext(), imageList)
        recyclerView.adapter = imageAdapter

        image.setOnClickListener { showOptionsDialog() }

        return view
    }


    private fun uploadImage(userId: String, imageUrl: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Uploading")
        progressDialog.show()

        reference = FirebaseDatabase.getInstance().getReference("myUsers").child(userId)
        val map: HashMap<String, Any> = HashMap()
        map["imgUrl"] = imageUrl
        reference.updateChildren(map)
            .addOnSuccessListener {
                progressDialog.dismiss()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.logOut){
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(context, LoginActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.logout, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun checkStatus(status : String){
        val map: HashMap<String, Any> = hashMapOf("status" to status)
        reference.updateChildren(map)
    }

    override fun onResume() {
        super.onResume()
        checkStatus("online")
    }

    override fun onPause() {
        super.onPause()
        checkStatus("offline")
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Obrada slike sa kamere
            handleImage(imageUri)
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            // Obrada slike iz galerije
            val selectedImageUri: Uri? = data?.data
            Log.d("gallery", Activity.RESULT_OK.toString())

            handleImage(selectedImageUri)
        }
    }

    private fun handleImage(imageUri: Uri?) {
        val filename = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().reference.child("images/$filename")

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Uploading")
        progressDialog.setCancelable(false) // Onemogućava korisniku da otkaže dijalog

        val uploadTask = storageRef.putFile(imageUri!!)
        uploadTask.addOnSuccessListener { _ ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    uploadImage(userId, imageUrl)
                }
                progressDialog.dismiss() // Zatvara ProgressDialog nakon završetka učitavanja slike
            }
        }.addOnFailureListener {
            progressDialog.dismiss() // Zatvara ProgressDialog u slučaju neuspjelog učitavanja slike
            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
        }

        progressDialog.show() // Prikazuje ProgressDialog prije početka učitavanja slike
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
        builder.show()
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





}
