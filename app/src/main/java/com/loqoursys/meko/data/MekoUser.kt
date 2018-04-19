package com.loqoursys.meko.data

/**
 * Created by root on 4/11/18 for LoqourSys
 */
@Suppress("unused")
data class MekoUser(var user_id: String = "", var full_name: String = "",
                    var phone_number: String = "", var photo_url: String = "") {
    constructor() : this("")
}