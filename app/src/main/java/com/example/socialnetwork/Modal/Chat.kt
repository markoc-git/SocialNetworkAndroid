package com.example.socialnetwork.Modal

data class Chat(
    val message : String,
    val receiver : String,
    val sender : String
){
    constructor() : this("","","")
}
