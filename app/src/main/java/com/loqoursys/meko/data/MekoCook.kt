package com.loqoursys.meko.data

/**
 * Created by root on 4/11/18 for LoqourSys
 */
@Suppress("unused")
data class MekoCook(var cookId: String = "", var full_name: String = "", var photoUrl: String = "",
                    var rating: Float = 0F, var info: String = "") {
    constructor() : this("")
}