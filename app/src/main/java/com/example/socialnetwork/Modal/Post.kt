package com.example.socialnetwork.Modal

data class Post(
    val postId: String,
    val postUrl: String,
    val userId: String,
    val likes: Int
) {
    val isLiked: Boolean = false

    constructor(postUrl: String, userId: String, likes: Int) : this("", postUrl, userId, likes)
    constructor() : this("", "", "",0)
}
