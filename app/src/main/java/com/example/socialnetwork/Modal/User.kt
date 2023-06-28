package com.example.socialnetwork.Modal

data class User(
    val username : String,
    val email : String,
    val imgUrl : String,
    val status : String,
    val id : String){
    constructor() : this("","","","","")
}


